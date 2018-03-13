package com.dpower.pub.dp2700.activity;

import com.dpower.pub.dp2700.application.App;
import com.dpower.util.MyLog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getInstance().addActivity(this);
		// 禁止弹出系统键盘
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case 131:
			MyLog.print(getClass().getSimpleName(), "呼叫管理中心键被按下");
			if (event.getAction() == KeyEvent.ACTION_UP && MainActivity.getKeySwitch()[0]) {
				startActivity(new Intent(this, CallManagementActivity.class));
			}
			break;
		case 133:
			MyLog.print(getClass().getSimpleName(), "监视键被按下");
			if (event.getAction() == KeyEvent.ACTION_UP && MainActivity.getKeySwitch()[1]) {
				startActivity(new Intent(this, MonitorActivity.class));
			}
			break;
		case 134:
			MyLog.print(getClass().getSimpleName(), "接听键被按下");
			break;
		case 135:
			MyLog.print(getClass().getSimpleName(), "开锁键被按下");
			break;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onDestroy() {
		App.getInstance().removeActivity(this);
		super.onDestroy();
	}
}
