package com.jmm.www.calendar.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jmm.www.calendar.R;

import java.io.File;



/**
 * 清除缓存
 */
public class ClearDataActivity extends Activity implements
		OnClickListener {
	private TextView cancle;
	private TextView pic;
	private TextView doc;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.clear_data_activity);
		mContext = this;
		// 占满屏
		Window window = getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		// 设置窗口的大小及透明度
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = layoutParams.WRAP_CONTENT;
		layoutParams.alpha = 1.0f;
		window.setAttributes(layoutParams);

		initView();
		initListener();
	}

	/**
	 * 实现onTouchEvent触屏函数但点击屏幕时销毁本Activity
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

	private void initListener() {
		pic.setOnClickListener(this);
		cancle.setOnClickListener(this);
		doc.setOnClickListener(this);
	}

	private void initView() {
		pic = (TextView) findViewById(R.id.clear_pic_data);
		cancle = (TextView) findViewById(R.id.clear_cancle);
		doc = (TextView) findViewById(R.id.clear_doc_data);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View arg0) {

		final int id = arg0.getId();
		if (id == R.id.clear_cancle) {
			onBackPressed();
			overridePendingTransition(R.anim.push_bottom_in,
					R.anim.push_bottom_out);
		} else if (id == R.id.clear_pic_data) {
			showDialogNotice("图片缓存数据");
		} else if (id == R.id.clear_doc_data) {
			showDelDocDialogNotice("文件缓存数据");
		}
	}

	private void showDialogNotice(final String message) {
		new AlertDialog.Builder(this).setTitle("温馨提示：")
				.setMessage("确定要清除" + message + "吗?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(mContext, message + "清除成功!",Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	private void showDelDocDialogNotice(final String message) {
		new AlertDialog.Builder(this).setTitle("温馨提示：")
				.setMessage("确定要清除" + message + "吗?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						File delFile = new File("/mnt/sdcard/xxt/");
						RecursionDeleteFile(delFile);
						delFile = new File("mnt/sdcard/xxtCache/");
						RecursionDeleteFile(delFile);
						delFile = new File("/mnt/sdcard/qtdownload/");
						RecursionDeleteFile(delFile);
						delFile = new File("/mnt/sdcard/qtoneDownloader/");
						RecursionDeleteFile(delFile);
						Toast.makeText(mContext, message + "清除成功!",Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	public static void RecursionDeleteFile(File file){
		if(null == file)
			return;

		if(!file.exists())
			return;

        if(file.isFile()){
            file.delete();
            return;
        }

        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                RecursionDeleteFile(f);
            }
//            file.delete();
        }
    }
}
