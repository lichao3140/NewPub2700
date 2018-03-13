package com.dpower.pub.dp2700.application;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.MainActivity;
import com.dpower.pub.dp2700.service.SmartHomeService;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.InstallRoot;
import com.dpower.pub.dp2700.tools.Language;
import com.dpower.pub.dp2700.tools.RebootUT;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.util.ConstConf;
import com.dpower.util.DPDBHelper;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;
import com.dpower.wifi.WifiSettings;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.github.anrwatchdog.ANRWatchDog.ANRListener;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

public class App extends Application {
	private static final String TAG = "App";

	private static App mApp;
	private Stack<Activity> mActivities;
	private Context mContext;

	public static App getInstance() {
		return mApp;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MyLog.print(TAG, "****************************");
		MyLog.print(TAG, "********应用程序启动********");
		MyLog.print(TAG, "****************************");
		mActivities = new Stack<Activity>();
		mApp = this;
		mContext = getApplicationContext();
		DPDBHelper.DBInstance(mContext);
		SPreferences.getInstance().setContext(mContext);
		ScreenUT.getInstance().init(mContext);
		RebootUT.getInstance(mContext).rebootAtTime(RebootUT.REBOOT_TIME);
		if (ProjectConfigure.needSmartHome) {
			startService(new Intent(mContext, SmartHomeService.class));
		}
		if (!checkForFileExist()) {
			// 如果运行所需文件不存在则返回
			return;
		}
		if (DPDBHelper.isInitApp()) {
			DPDBHelper.setInitApp(false);
			MyLog.print(TAG, "初始化APP");
			WifiSettings wifiSettings = new WifiSettings(mContext, null);
			if (wifiSettings.isWifiEnabled()) {
				wifiSettings.setWifiEnabled(false);
			}
			wifiSettings.unInit();
			Language.updateLanguage(1);
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
			max = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
			audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, max, 0);
			int[] defaultAlarmArea;
			int[] defaultAlarmType;
			if (ProjectConfigure.project == 2) {
				defaultAlarmArea = new int[] { 15, 0, 11, 16, 13, 2, 9, 17 };
				defaultAlarmType = new int[] { 3, 3, 3, 3, 3, 3, 3, 0 };
				DPFunction.setSefeConnection(0);
				if (!ProjectConfigure.isSimple) {
					InstallRoot.getInstance().init(mContext);
				}
			} else {
				defaultAlarmArea = new int[DPFunction.getTanTouNum()];
				defaultAlarmType = new int[DPFunction.getTanTouNum()];
			}
			DPFunction.setDefaultAlarmArea(defaultAlarmArea);
			DPFunction.setDefaultAlarmType(defaultAlarmType);
		}
		if (DPFunction.init(mContext) != 0) {
			MyLog.print(MyLog.ERROR, TAG, "初始化失败");
			try {
				String cmd = "su -c reboot";
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60 * 1000);
		// 单元门口机或者小门口机呼入需要立即显示图像,其他呼入需要在相应的界面里做设置
		DPFunction.videoAreaCallInUnit[0] = 0; // 起始点 x
		DPFunction.videoAreaCallInUnit[1] = getResources().getInteger(R.integer.title_bar_height); // 起始点 y
		DPFunction.videoAreaCallInUnit[2] = getResources().getInteger(R.integer.door_call_in_width);// 宽;
		DPFunction.videoAreaCallInUnit[3] = getResources().getInteger(R.integer.door_call_in_height);// 高;
		if (!ProjectConfigure.isDebug) {
			CrashHandler.getInstance().init(mContext);
			new ANRWatchDog().setANRListener(new ANRListener() {
				@Override
				public void onAppNotResponding(ANRError arg0) {
					try {
						MyLog.print(MyLog.ERROR, TAG, "ANR");
						String cmd = "su -c reboot";
						Runtime.getRuntime().exec(cmd);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	/**
	 * 检查运行环境是否正确
	 */
	private boolean checkForFileExist() {
		// 判断安防配置文件
		File safe = new File(ConstConf.SAFE_ALARM_PATH);
		if (!safe.exists()) {
			safe.mkdirs();
		}
		File safeFile = new File(ConstConf.SAFE_ALARM_PATH + ConstConf.SAFE_ALARM_TXT);
		if (!safeFile.exists() || (int) safeFile.length() == 0) {
			MyLog.print(MyLog.ERROR, TAG, "alarm.txt 不存在");
			String fileName;
			if (ProjectConfigure.project == 2) {
				fileName = "alarm_cnc.txt";
			} else {
				fileName = "alarm.txt";
			}
			boolean result = FileOperate.copyToSD(mContext, ConstConf.SAFE_ALARM_PATH + ConstConf.SAFE_ALARM_TXT,
					fileName);
			if (!result)
				return false;
		}

		// 检查网络配置表
		MyLog.print(TAG, "sdcardPath = " + ConstConf.SD_DIR);
		File file = new File(ConstConf.SD_DIR + ConstConf.NET_CFG_DAT);
		if (!file.exists() || (int) file.length() == 0) {
			MyLog.print(MyLog.ERROR, TAG, "netcfg.dat 不存在");
			String fileName;
			if (ProjectConfigure.project == 2) {
				fileName = "netcfg_cnc.dat";
			} else {
				fileName = "netcfg.dat";
			}
			boolean result = FileOperate.copyToSD(mContext, ConstConf.SD_DIR + ConstConf.NET_CFG_DAT, fileName);
			if (!result)
				return false;
		}

		// 检查铃声
		file = new File(ConstConf.RING_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(ConstConf.RING_PATH + ConstConf.RING_MP3);
		if (!file.exists() || (int) file.length() == 0) {
			MyLog.print(MyLog.ERROR, TAG, "ring_you.mp3  不存在");
			boolean result = FileOperate.copyToSD(mContext, ConstConf.RING_PATH + ConstConf.RING_MP3,
					ConstConf.RING_MP3);
			if (!result)
				return false;
		}

		// 检查声音文件
		file = new File(ConstConf.SD_DIR + ConstConf.VOLUME_INF);
		if (!file.exists() || (int) file.length() == 0) {
			String fileName;
			if (ProjectConfigure.project == 1 && ProjectConfigure.size == 10) {
				fileName = "AECpara_lqh10.inf";
			} else if (ProjectConfigure.project == 2 && ProjectConfigure.size == 10) {
				fileName = "AECpara_cnc10.inf";
			} else {
				fileName = "AECpara.inf";
			}
			boolean result = FileOperate.copyToSD(mContext, ConstConf.SD_DIR + ConstConf.VOLUME_INF, fileName);
			if (!result)
				return false;
			FileOperate fileOperate = new FileOperate();
			result = fileOperate.from(ConstConf.SD_DIR + ConstConf.VOLUME_INF)
					.toRomDir(ConstConf.SYSTEM_CONF + ConstConf.VOLUME_INF);
			if (!result)
				return false;
		}
		return true;
	}

	public Context getContext() {
		return mContext;
	}

	public void addActivity(Activity activity) {
		if (mActivities != null && mActivities.size() > 0) {
			removeActivity(activity);
		}
		mActivities.push(activity);
		MyLog.print(TAG, activity.getClass().getSimpleName() + "入栈");
		MyLog.print(TAG, "mActivities size = " + mActivities.size());
	}

	public void removeActivity(Activity activity) {
		if (mActivities != null && mActivities.size() > 0) {
			if (mActivities.contains(activity)) {
				mActivities.remove(activity);
				MyLog.print(TAG, activity.getClass().getSimpleName() + "出栈");
				MyLog.print(TAG, "mActivities size = " + mActivities.size());
			}
		}
	}

	public Activity getTopActivity() {
		if (mActivities != null && mActivities.size() > 0) {
			return mActivities.peek();
		}
		return null;
	}

	public void exit() {
		if (mActivities != null && mActivities.size() > 0) {
			MyLog.print(MyLog.ERROR, TAG, "退出应用");
			for (Activity activity : mActivities) {
				if (!activity.isDestroyed()) {
					activity.finish();
					MyLog.print(MyLog.ERROR, TAG, "销毁" + activity.getClass().getSimpleName());
				}
			}
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	public void onHomeKey() {
		MyLog.print(TAG, "onHomeKey");
		if (mActivities != null && mActivities.size() > 0) {
			for (int i = 0; i < mActivities.size(); i++) {
				Activity activity = mActivities.get(i);
				if (!activity.isDestroyed()) {
					if (!activity.getClass().equals(MainActivity.class)) {
						activity.finish();
						removeActivity(activity);
						i--;
						MyLog.print(MyLog.ERROR, TAG, "销毁" + activity.getClass().getSimpleName());
					}
				}
			}
		}
	}
}
