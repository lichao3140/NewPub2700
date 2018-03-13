package com.dpower.pub.dp2700.activity.dialog;

import java.io.File;
import java.io.IOException;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;
import com.dpower.wifi.WifiSettings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 系统复位窗口
 */
public class SystemResetDialog extends BaseActivity implements OnClickListener {
	private static final String TAG = "SystemResetDialog";

	private RelativeLayout mInfoWindow;
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system_reset);
		init();
	}

	private void init() {
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (RelativeLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		mTitle = (TextView) findViewById(R.id.tv_title);
		mTitle.setText(R.string.reset_all_data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_confirm:
			reset(SystemResetDialog.this);
			RelativeLayout contentWindow = (RelativeLayout) findViewById(R.id.info_window);
			contentWindow.setVisibility(View.GONE);
			LayoutInflater inflater = getLayoutInflater();
			View popupView = inflater.inflate(R.layout.popup_progress_bar, null);
			TextView title = (TextView) popupView.findViewById(R.id.tv_title);
			title.setText(R.string.resetting_all_data);
			PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
					true);
			popupWindow.setTouchable(true);
			popupWindow.setFocusable(true);
			popupWindow.showAtLocation(LayoutInflater.from(this).inflate(R.layout.activity_main, null), Gravity.CENTER,
					0, 0);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					String cmd = "su -c reboot";
					try {
						Runtime.getRuntime().exec(cmd);
					} catch (IOException e) {
						MyLog.print("cmd", "error" + e);
					}
					finish();
				}
			}, 4000);
			break;
		case R.id.screen_window:
		case R.id.btn_cancel:
			finish();
			break;
		default:
			break;
		}
	}

	public static void reset(Context context) {
		WifiSettings wifiSettings = new WifiSettings(context, null);
		wifiSettings.removeAllNetwork();
		if (wifiSettings.isWifiEnabled()) {
			wifiSettings.setWifiEnabled(false);
		}
		wifiSettings.unInit();
		CommonUT.setSystemTime("2017-01-01 00:00:00");
		// 删除数据库
		String cmd = "rm -rf /data/data/com.dpower.pub.dp2700/files";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			MyLog.print("cmd", "error" + e);
		}
		// 删除SharedPreferences文件
		cmd = "rm -rf /data/data/com.dpower.pub.dp2700/shared_prefs";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			MyLog.print("cmd", "error" + e);
		}
		// 删除电话铃声
		File root = new File(ConstConf.RING_PATH);
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		// 删除网络配置表
		root = new File(ConstConf.NET_CFG_PATH);
		if (root.exists()) {
			root.delete();
		}
		// 删除防区配置文件
		root = new File(ConstConf.SAFE_ALARM_PATH);
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		// 删除业主留言
		root = new File(ConstConf.SD_DIR + "leavemessage");
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		cmd = "rm -rf /system/media/backup/userleave.wav";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			MyLog.print("cmd", "error" + e);
		}
		// 删除小区信息
		root = new File(ConstConf.MESSAGE_PATH);
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		// 删除留言留影
		root = new File(ConstConf.VISIT_PATH);
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		// 删除报警录像
		root = new File(ConstConf.ALARM_VIDEO_PATH);
		if (!root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
	}
}
