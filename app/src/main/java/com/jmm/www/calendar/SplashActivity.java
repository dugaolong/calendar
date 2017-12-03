package com.jmm.www.calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jmm.www.calendar.utils.ApiService;
import com.jmm.www.calendar.utils.GlobalContants;
import com.jmm.www.calendar.utils.PermissionHelper;
import com.jmm.www.calendar.utils.StreamFormat;

import net.youmi.android.AdManager;
import net.youmi.android.nm.cm.ErrorCode;
import net.youmi.android.nm.sp.SplashViewSettings;
import net.youmi.android.nm.sp.SpotListener;
import net.youmi.android.nm.sp.SpotManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;


/**
 * 闪屏页
 */

public class SplashActivity extends AppCompatActivity {

    @Bind(R.id.version_code)
    TextView tvVersionCode;

    private String mVersionName;//版本名
    private int mVersionCode;//版本号
    private String mDesc;//版本描述
    private String mdownloadUrl;//新版本下载网站

    private static final String TAG = "SplashActivity";
//    private ViewGroup mContainer;

    private PermissionHelper mPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 移除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);//为使用注解进行注册

        mVersionName= getVersionName();
        mVersionCode= getVersionCode();

        tvVersionCode.setText("版本号:" + mVersionName);


//        mContainer = (ViewGroup) findViewById(R.id.splash_ad_container);
        //1.5S的延迟
        Timer timer =new Timer();
        TimerTask task =new TimerTask() {
            @Override
            public void run() {
                jumpToNextPage();
            }
        };
        timer.schedule(task,1500);



        // 当系统为6.0以上时，需要申请权限
        mPermissionHelper = new PermissionHelper(this);
        mPermissionHelper.setOnApplyPermissionListener(new PermissionHelper.OnApplyPermissionListener() {
            @Override
            public void onAfterApplyAllPermission() {
                Log.i(TAG, "All of requested permissions has been granted, so run app logic.");
                runApp();
            }
        });
        if (Build.VERSION.SDK_INT < 23) {
            // 如果系统版本低于23，直接跑应用的逻辑
            Log.d(TAG, "The api level of system is lower than 23, so run app logic directly.");
            runApp();
        } else {
            // 如果权限全部申请了，那就直接跑应用逻辑
            if (mPermissionHelper.isAllRequestedPermissionGranted()) {
                Log.d(TAG, "All of requested permissions has been granted, so run app logic directly.");
                runApp();
            } else {
                // 如果还有权限为申请，而且系统版本大于23，执行申请权限逻辑
                Log.i(TAG, "Some of requested permissions hasn't been granted, so apply permissions first.");
                mPermissionHelper.applyPermissions();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPermissionHelper.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 跑应用的逻辑
     */
    private void runApp() {
        //初始化SDK
        AdManager.getInstance(getApplicationContext()).init("b66ec3cc5ebab7ef", "1241a416184eaf7b", true);
        SpotManager.getInstance(getApplicationContext()).requestSpot(new SpotRequestListener(){});
//        preloadAd();
        setupSplashAd(); // 如果需要首次展示开屏，请注释掉本句代码
    }

	/**
	 * 预加载广告
	 */
	private void preloadAd() {
		// 注意：不必每次展示插播广告前都请求，只需在应用启动时请求一次
		SpotManager.getInstance(getApplicationContext()).showSpot(this,new SpotListener() {
            @Override
            public void onShowSuccess() {
                logInfo("请求插播广告成功");
            }

            @Override
            public void onShowFailed(int errorCode) {
                logError("请求插播广告失败，errorCode: %s", errorCode);
                switch (errorCode) {
                    case ErrorCode.NON_NETWORK:
                        showShortToast("网络异常");
                        break;
                    case ErrorCode.NON_AD:
                        showShortToast("暂无视频广告");
                        break;
                    default:
                        showShortToast("请稍后再试");
                        break;
                }
            }

            @Override
            public void onSpotClosed() {
                logInfo("关闭");
            }

            @Override
            public void onSpotClicked(boolean b) {
                logInfo("点击");
            }

//            @Override
//			public void onRequestSuccess() {
//				logInfo("请求插播广告成功");
//				//				// 应用安装后首次展示开屏会因为本地没有数据而跳过
//				//              // 如果开发者需要在首次也能展示开屏，可以在请求广告成功之前展示应用的logo，请求成功后再加载开屏
//				//				setupSplashAd();
//			}
//
//			@Override
//			public void onRequestFailed(int errorCode) {
//				logError("请求插播广告失败，errorCode: %s", errorCode);
//				switch (errorCode) {
//				case ErrorCode.NON_NETWORK:
//					showShortToast("网络异常");
//					break;
//				case ErrorCode.NON_AD:
//					showShortToast("暂无视频广告");
//					break;
//				default:
//					showShortToast("请稍后再试");
//					break;
//				}
//			}
		});
	}

    /**
     * 设置开屏广告
     */
    private void setupSplashAd() {
        // 创建开屏容器
        final RelativeLayout splashLayout = (RelativeLayout) findViewById(R.id.rl_splash);
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, R.id.view_divider);

        // 对开屏进行设置
        SplashViewSettings splashViewSettings = new SplashViewSettings();
        //		// 设置是否展示失败自动跳转，默认自动跳转
        //		splashViewSettings.setAutoJumpToTargetWhenShowFailed(false);
        // 设置跳转的窗口类
        splashViewSettings.setTargetClass(MainActivity.class);
        // 设置开屏的容器
        splashViewSettings.setSplashViewContainer(splashLayout);

        // 展示开屏广告
        SpotManager.getInstance(getApplicationContext())
                .showSplash(getApplicationContext(), splashViewSettings, new SpotListener() {

                    @Override
                    public void onShowSuccess() {
                        logInfo("开屏展示成功");
                    }

                    @Override
                    public void onShowFailed(int errorCode) {
                        logError("开屏展示失败");
                        switch (errorCode) {
                            case ErrorCode.NON_NETWORK:
                                logError("网络异常");
                                break;
                            case ErrorCode.NON_AD:
                                logError("暂无开屏广告");
                                break;
                            case ErrorCode.RESOURCE_NOT_READY:
                                logError("开屏资源还没准备好");
                                break;
                            case ErrorCode.SHOW_INTERVAL_LIMITED:
                                logError("开屏展示间隔限制");
                                break;
                            case ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE:
                                logError("开屏控件处在不可见状态");
                                break;
                            default:
                                logError("errorCode: %d", errorCode);
                                break;
                        }
                    }

                    @Override
                    public void onSpotClosed() {
                        logDebug("开屏被关闭");
                    }

                    @Override
                    public void onSpotClicked(boolean isWebPage) {
                        logDebug("开屏被点击");
                        logInfo("是否是网页广告？%s", isWebPage ? "是" : "不是");
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 开屏展示界面的 onDestroy() 回调方法中调用
        SpotManager.getInstance(getApplicationContext()).onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // 捕获back键，在展示广告期间按back键，不跳过广告
//            if (mContainer.getVisibility() == View.VISIBLE) {
//                return true;
//            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 从获取服务器获取版本信息进行校验
     */
    public void checkVersion() {
        //利用volley框架进行http请求
        RequestQueue requestQueue= Volley.newRequestQueue(SplashActivity.this);
        JsonObjectRequest request=new JsonObjectRequest(GlobalContants.SERVER_URL+"update.json", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    mVersionName = response.getString("versionName");
                    mVersionCode = response.getInt("versionCode");
                    mDesc = response.getString("description");
                    mdownloadUrl = response.getString("downloadUrl");

//                    System.out.println("versionName="+mVersionName+"versionCode"+mVersionCode);
//                    System.out.println("mdownloadUrl------------"+mdownloadUrl);

                    if(mVersionCode>getVersionCode()){

                        //如果服务器返回的versionCode大于本地的versionCode,说明有更新
                        showUpdateDialog();
                    }else {
                        //1.5S的延迟
                        Timer timer =new Timer();
                        TimerTask task =new TimerTask() {
                            @Override
                            public void run() {
                                jumpToNextPage();
                            }
                        };
                        timer.schedule(task,1500);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //1.5S的延迟
                Timer timer =new Timer();
                TimerTask task =new TimerTask() {
                    @Override
                    public void run() {
                        jumpToNextPage();
                    }
                };
                timer.schedule(task,1500);
            }
        });
        requestQueue.add(request);
    }
    /**
     * 获取版本名称
     *
     * @return
     */
    public String getVersionName() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public int getVersionCode() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 升级对话框
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最新版本:" + mVersionName);
        builder.setMessage(mDesc);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                System.out.println("立即更新");
                //下载APK
                downLoadAPK();

            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //不更新,直接进入主界面
                jumpToNextPage();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                jumpToNextPage();
            }
        });
        builder.show();
    }

    /**
     * 下载APK文件
     */
    private void downLoadAPK() {
        //OkHttp

        //先判断sdcard是否存在
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            final String target= Environment.getExternalStorageDirectory()+"/app-release.apk";

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.SERVICE_URL)
                    .build();

            ApiService apiService=retrofit.create(ApiService.class);

            apiService.dowmApk(mdownloadUrl)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                            InputStream inputStream = response.body().byteStream();
                            FileOutputStream fos = null;
                            File file=null;
                            //获取字节数组
                            try {
                                byte[] getData= StreamFormat.readInputStream(inputStream);
                                file = new File(target);
                                fos=new FileOutputStream(file);
                                fos.write(getData);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    fos.close();
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

//                            System.out.println("下载成功!");
                            //跳转至系统安装界面
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setDataAndType(Uri.fromFile(file),
                                    "application/vnd.android.package-archive");
//                    startActivity(intent);如果用户点取消同样返回主界面
                            startActivityForResult(intent,0);


                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(SplashActivity.this,"下载失败", Toast.LENGTH_SHORT).show();
                        }
                    });


        }else {
            Toast.makeText(SplashActivity.this,"sdcard不存在", Toast.LENGTH_SHORT).show();
        }

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        jumpToNextPage();
//
//    }

    /**
     * 跳转下一个页面
     */
    private void jumpToNextPage(){
//        Boolean isFirstIn= PrefUtils.getBoolean(this, "isFirstIn", true);
//        if(isFirstIn){
//            startActivity(new Intent(SplashActivity.this,GuideActivity.class));
//        }else {
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
//        }
        finish();
    }


    /**
     * 打印调试级别日志
     *
     * @param format
     * @param args
     */
    protected void logDebug(String format, Object... args) {
        logMessage(Log.DEBUG, format, args);
    }

    /**
     * 打印信息级别日志
     *
     * @param format
     * @param args
     */
    protected void logInfo(String format, Object... args) {
        logMessage(Log.INFO, format, args);
    }

    /**
     * 打印错误级别日志
     *
     * @param format
     * @param args
     */
    protected void logError(String format, Object... args) {
        logMessage(Log.ERROR, format, args);
    }

    /**
     * 打印日志
     *
     * @param level
     * @param format
     * @param args
     */
    private void logMessage(int level, String format, Object... args) {
        String formattedString = String.format(format, args);
        switch (level) {
            case Log.DEBUG:
                Log.d(TAG, formattedString);
                break;
            case Log.INFO:
                Log.i(TAG, formattedString);
                break;
            case Log.ERROR:
                Log.e(TAG, formattedString);
                break;
        }
    }
    /**
     * 展示短时Toast
     *
     * @param format
     * @param args
     */
    protected void showShortToast(String format, Object... args) {
        showToast(Toast.LENGTH_SHORT, format, args);
    }
    /**
     * 展示Toast
     *
     * @param duration
     * @param format
     * @param args
     */
    private void showToast(int duration, String format, Object... args) {
        Toast.makeText(getApplicationContext(), String.format(format, args), duration).show();
    }

}
