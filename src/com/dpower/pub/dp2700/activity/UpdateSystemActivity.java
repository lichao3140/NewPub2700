package com.dpower.pub.dp2700.activity;

import java.io.File;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UpdateSystemActivity extends BaseFragmentActivity {

	private TextView mTip;
	private ProgressBar mProgressBar;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_system);
		initView();
		initData();
		copySystemFile();
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		mTip = (TextView) findViewById(R.id.tv_update_system_tip);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
	}

	private void initData() {
		mHandler = new Handler();
		File file = new File(ConstConf.UPDATE_SYSTEM_EXT_SD);
		int total = (int) file.length();
		mProgressBar.setMax(total);
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				File file = new File(ConstConf.UPDATE_SYSTEM_PATH);
				int progress = (int) file.length();
				mProgressBar.setProgress(progress);
				mHandler.postDelayed(this, 500);
			}
		});
	}

	private void copySystemFile() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				FileOperate fileOperate = new FileOperate();
				boolean result = fileOperate.from(ConstConf.UPDATE_SYSTEM_EXT_SD).to(ConstConf.UPDATE_SYSTEM_PATH);

				mHandler.removeCallbacksAndMessages(null);
				if (result) {
					MyLog.print("正在打开升级系统的程序");
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mTip.setText(R.string.update_system_copy_successfully);
						}
					});
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							sendBroadcast(new Intent("com.dpower.updatebroadcast"));
							Intent intent = new Intent();
							intent.setComponent(
									new ComponentName("com.example.hello", "com.example.hello.MainActivity"));
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					}, 3000);
				} else {
					MyLog.print(MyLog.ERROR, "拷贝失败");
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mTip.setText(R.string.update_system_copy_failed);
						}
					});
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							finish();
						}
					}, 2000);
				}
			}
		}).start();
	}

	@Override
	protected void onResume() {
		ScreenUT.getInstance().acquireWakeLock();
		super.onResume();
	}

	@Override
	protected void onPause() {
		ScreenUT.getInstance().releaseWakeLock();
		super.onPause();
	}
}
