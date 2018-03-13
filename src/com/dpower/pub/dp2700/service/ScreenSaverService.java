package com.dpower.pub.dp2700.service;

import java.util.ArrayList;

import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.util.MyLog;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * 屏幕休眠监控服务
 */
@SuppressWarnings("deprecation")
public class ScreenSaverService extends Service {
	private static final String TAG = "ScreenSaverService";

	public static final int BLACK = 0;
	public static final int CALENDAR = 1;
	public static final int PICTURE = 2;
	private static ArrayList<ScreenOffCallback> mCallbacks = new ArrayList<ScreenOffCallback>();
	private KeyguardManager mKeyguardManager = null;
	private KeyguardLock mKeyguardLock = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock = mKeyguardManager.newKeyguardLock("screen");
		mKeyguardLock.disableKeyguard();

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenSaverReciever, filter);
		MyLog.print(TAG, "注册监听 屏幕休眠");
	}

	@Override
	public void onDestroy() {
		MyLog.print(TAG, "onDestroy 解除注册监听 屏幕休眠");
		unregisterReceiver(mScreenSaverReciever);
		super.onDestroy();
	};

	BroadcastReceiver mScreenSaverReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			MyLog.print(TAG, "监听屏幕休眠 ");
			App.getInstance().onHomeKey();
			if (SPreferences.getInstance().getScreenSaverMode() == CALENDAR
					|| SPreferences.getInstance().getScreenSaverMode() == PICTURE) {
				ScreenUT.getInstance().acquireWakeLock();
			}
			if (!mCallbacks.isEmpty()) {
				for (ScreenOffCallback screenOffCallback : mCallbacks) {
					screenOffCallback.onScreenOff(SPreferences.getInstance().getScreenSaverMode());
					MyLog.print(TAG, "onScreenOff");
				}
			}
		}
	};

	public interface ScreenOffCallback {
		public void onScreenOff(int flag);
	}

	public static void registerScreenOffCallback(ScreenOffCallback callback) {
		MyLog.print(TAG, "registerScreenOffCallback");
		mCallbacks.add(callback);
	}

	public static void unRegisterScreenOffCallback(ScreenOffCallback callback) {
		MyLog.print(TAG, "unRegisterScreenOffCallback");
		if (mCallbacks.contains(callback)) {
			mCallbacks.remove(callback);
		}
	}
}
