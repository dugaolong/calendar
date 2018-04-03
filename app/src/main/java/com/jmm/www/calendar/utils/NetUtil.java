package com.jmm.www.calendar.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.jmm.www.calendar.R;


/**
 * 网络工具类
 * @author liufuning
 *
 *  create time:2013-11-21上午8:06:48
 */
public class NetUtil {

	private static final String TAG  = "NetUtil";
	
	public static boolean flag = false;	//判断是否已经弹出设置网络对话框
	
	public static final boolean detectNetStatus(Context context ){
		
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	
		if(manager == null){
			Log.e("NetUtil ", "当前网络不可用");
			return false;
		}
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		if(networkInfo == null || !networkInfo.isAvailable()){
			Log.e("NetUtil ", "当前网络不可用");
			return false;
		}
		return true;
		
	}
	
	/**
	 * 判断是否有网络连接
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity == null) {
			Log.e(TAG, "couldn't get connectivity manager");
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].isAvailable()) {
						Log.e(TAG, "network is available");
						return true;
					}
				}
			}
		}
		Log.e(TAG, "network is not available");
		return false;
	}
	
	public static boolean checkNetState(Context context){
    	boolean netstate = false;
		ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivity != null)
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++)
				{
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						netstate = true;
						break;
					}
				}
			}
		}
		return netstate;
    }

	/**
	 * 判断网络是否为漫游
	 */
	public static boolean isNetworkRoaming(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.e(TAG, "couldn't get connectivity manager");
		} else {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null
					&& info.getType() == ConnectivityManager.TYPE_MOBILE) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm != null && tm.isNetworkRoaming()) {
					Log.e(TAG, "network is roaming");
					return true;
				} else {
					Log.e(TAG, "network is not roaming");
				}
			} else {
				Log.e(TAG, "not using mobile network");
			}
		}
		return false;
	}

	/**
	 * 判断MOBILE网络是否可用
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static boolean isMobileDataEnable(Context context) throws Exception {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isMobileDataEnable = false;

		isMobileDataEnable = connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();

		return isMobileDataEnable;
	}

	
	/**
	 * 判断wifi 是否可用
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static boolean isWifiDataEnable(Context context) throws Exception {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isWifiDataEnable = false;
		isWifiDataEnable = connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
		return isWifiDataEnable;
	}

	/**
	 * 设置网络
	 * @param context
	 */
	public static final void setNetwork(final Context context){
		
		if(flag){
			return;
		}
		AlertDialog alert = null;
		Builder builder = new Builder(context);
		builder.setIcon(R.mipmap.calendar);
		builder.setTitle("提示");
		builder.setMessage("网络不可用，是否现在设置网络？ ");
		builder.setPositiveButton("立刻设置", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				flag = false;
			}
		});
		
		builder.setNegativeButton("取消", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				flag = false;
				dialog.cancel();
			}
		});
		
		flag = true;
		alert = builder.create();
//		alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alert.show();

	}
}
