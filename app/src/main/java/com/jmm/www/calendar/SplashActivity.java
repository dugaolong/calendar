package com.jmm.www.calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jmm.www.calendar.utils.ApiService;
import com.jmm.www.calendar.utils.GlobalContants;
import com.jmm.www.calendar.utils.StreamFormat;
import com.xiaomi.ad.SplashAdListener;
import com.xiaomi.ad.adView.SplashAd;

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

    private static final String TAG = "VerticalSplash";
    //以下的POSITION_ID 需要使用您申请的值替换下面内容
    private static final String POSITION_ID = "2e1cb179b87a7b0475215cdd30543d5a";
    private ViewGroup mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);//为使用注解进行注册

        mVersionName= getVersionName();
        mVersionCode= getVersionCode();

        tvVersionCode.setText("版本号:" + mVersionName);

        mContainer = (ViewGroup) findViewById(R.id.splash_ad_container);
        SplashAd splashAd = new SplashAd(this, mContainer, R.mipmap.design, new SplashAdListener() {
            @Override
            public void onAdPresent() {
                // 开屏广告展示
                Log.d(TAG, "onAdPresent");
            }

            @Override
            public void onAdClick() {
                //用户点击了开屏广告
                Log.d(TAG, "onAdClick");
                finish();
                jumpToNextPage();
            }

            @Override
            public void onAdDismissed() {
                //这个方法被调用时，表示从开屏广告消失。
                Log.d(TAG, "onAdDismissed");
                finish();
                jumpToNextPage();
            }

            @Override
            public void onAdFailed(String s) {
                Log.d(TAG, "onAdFailed, message: " + s);
                finish();
                jumpToNextPage();
            }
        });
        splashAd.requestAd(POSITION_ID);


//        checkVersion();
        //1.5S的延迟
//        Timer timer =new Timer();
//        TimerTask task =new TimerTask() {
//            @Override
//            public void run() {
//                jumpToNextPage();
//            }
//        };
//        timer.schedule(task,1500);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // 捕获back键，在展示广告期间按back键，不跳过广告
            if (mContainer.getVisibility() == View.VISIBLE) {
                return true;
            }
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

                    System.out.println("versionName="+mVersionName+"versionCode"+mVersionCode);
                    System.out.println("mdownloadUrl------------"+mdownloadUrl);

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
                System.out.println("立即更新");
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

                            System.out.println("下载成功!");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        jumpToNextPage();

    }

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


}
