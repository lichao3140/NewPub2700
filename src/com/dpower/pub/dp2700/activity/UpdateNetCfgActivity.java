package com.dpower.pub.dp2700.activity;

import java.io.IOException;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.ScreenUT;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

public class UpdateNetCfgActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_net_cfg);
	}

	@Override
	protected void onResume() {
		super.onResume();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				LayoutInflater inflater = getLayoutInflater();
				View popupView = inflater.inflate(R.layout.popup_progress_bar, null);
				TextView title = (TextView) popupView.findViewById(R.id.tv_title);
				title.setText(R.string.update_net_cfg_success);
				PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, true);
				popupWindow.setTouchable(true);
				popupWindow.setFocusable(true);
				popupWindow.showAtLocation(
						LayoutInflater.from(UpdateNetCfgActivity.this).inflate(R.layout.activity_update_net_cfg, null),
						Gravity.CENTER, 0, 0);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						String cmd = "su -c reboot";
						try {
							Runtime.getRuntime().exec(cmd);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, 6 * 1000);
			}
		}, 4 * 1000);
		ScreenUT.getInstance().acquireWakeLock();
	}

	@Override
	protected void onPause() {
		ScreenUT.getInstance().releaseWakeLock();
		super.onPause();
	}
}
