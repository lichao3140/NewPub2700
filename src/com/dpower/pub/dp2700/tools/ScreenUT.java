package com.dpower.pub.dp2700.tools;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;

/**
 * 屏幕休眠工具
 */
public class ScreenUT {

	public static boolean isAlwaysScreenOn = false;
	private static ScreenUT mScreenUT;
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
	private Context mContext;

	private ScreenUT() {

	}

	@SuppressWarnings("deprecation")
	public void init(Context context) {
		mContext = context;
		mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE | PowerManager.SCREEN_DIM_WAKE_LOCK,
				"ScreenUT");
	}

	public static ScreenUT getInstance() {
		if (mScreenUT == null) {
			mScreenUT = new ScreenUT();
		}
		return mScreenUT;
	}

	/**
	 * 屏幕当前是否亮屏
	 */
	public boolean isScreenOn() {
		return mPowerManager.isScreenOn();
	}

	/**
	 * 唤醒屏幕,保持屏幕亮屏
	 */
	public void acquireWakeLock() {
		wakeUpScreen();
		mWakeLock.acquire();
	}

	/**
	 * 释放,允许屏幕休眠
	 */
	public void releaseWakeLock() {
		if (isAlwaysScreenOn) {
			return;
		}
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	/**
	 * 唤醒屏幕
	 */
	public void wakeUpScreen() {
		if (!mPowerManager.isScreenOn()) {
			mWakeLock.acquire();
			mWakeLock.release();
		}
	}

	/**
	 * 关闭屏幕
	 */
	public void goToSleep() {
		// android 4.4以下
		mPowerManager.goToSleep(SystemClock.uptimeMillis());
		// android 5.0以上
		// try {
		// Class<?> powerManager = Class.forName("android.os.PowerManager");
		// Method goToSleep = powerManager.getMethod("goToSleep", long.class);
		// goToSleep.invoke(mPowerManager, SystemClock.uptimeMillis());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}
}
