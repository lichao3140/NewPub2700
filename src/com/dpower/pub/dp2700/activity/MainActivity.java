package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.io.IOException;

import com.dpower.callback.PCToolCallback;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.dialog.SystemResetDialog;
import com.dpower.pub.dp2700.adapter.MainPageAdapter;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.service.PhysicsKeyService.KeyCallback;
import com.dpower.pub.dp2700.service.ScreenSaverService;
import com.dpower.pub.dp2700.service.ScreenSaverService.ScreenOffCallback;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.InstallPackage;
import com.dpower.pub.dp2700.tools.RebootUT;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;
import com.example.dpservice.PCToolService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends BaseFragmentActivity implements OnClickListener {
	private static final String TAG = "MainActivity";

	public static boolean isScreenOff = false; // 针对屏幕关闭时按键的处理
	public static boolean isStartScreenSaver = true; // 针对日历屏保、图片屏保进行定时退出并关闭屏幕的处理
	public ViewPager mViewPaper;
	/** 美一的按键：呼叫管理中心，监视，开锁，接听或挂断 */
	private static boolean[] mMeeYiKeySwitch;
	private MainPageAdapter mAdapter;
	private Button mHome, mVisualIntercom, mCloudIntercom, mHomeSercurity, mIntelligentHome, mSetting, mEntertainment;
	private Button[] mButtonArray;
	private ImageView mNetworkEthernet, mNetworkWireless, mDefence;
	private int mFlag;
	private SafeModeChangeReceiver mSafeModeReceiver;
	private NetworkBroadcastReceiver mNetworkReceiver;
	private BroadcastReceiver mUpdateNetCfg;
	private BroadcastReceiver mUpdateAPK;
	private BroadcastReceiver mUpdateAlarm;
	private Handler mHandler;
	private BroadcastReceiver mMediaMountedReceiver;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ProjectConfigure.project == 2 && ProjectConfigure.isSimple) {
			setContentView(R.layout.activity_main2);
		} else {
			setContentView(R.layout.activity_main);
		}
		init();
	}

	private void init() {
		ScreenUT.getInstance().wakeUpScreen();
		mContext = this;
		mHome = (Button) findViewById(R.id.btn_home);
		mVisualIntercom = (Button) findViewById(R.id.btn_visual_intercom);
		mHomeSercurity = (Button) findViewById(R.id.btn_house_sercurity);
		mCloudIntercom = (Button) findViewById(R.id.btn_cloud_intercom);
		mIntelligentHome = (Button) findViewById(R.id.btn_intelligent_home);
		mSetting = (Button) findViewById(R.id.btn_setting);
		mEntertainment = (Button) findViewById(R.id.btn_entertainment);
		mNetworkEthernet = (ImageView) findViewById(R.id.image_network_ethernet);
		mNetworkWireless = (ImageView) findViewById(R.id.image_network_wireless);
		mDefence = (ImageView) findViewById(R.id.image_defence);

		if (ProjectConfigure.project == 2 && ProjectConfigure.isSimple) {
			mButtonArray = new Button[] { mHome, mHomeSercurity, mVisualIntercom, mSetting };
		} else {
			mButtonArray = new Button[] { mCloudIntercom, mHomeSercurity, mVisualIntercom, mHome, mIntelligentHome,
					mSetting, mEntertainment };
		}
		for (int i = 0; i < mButtonArray.length; i++) {
			mButtonArray[i].setOnClickListener(this);
		}
		mFlag = MainPageAdapter.getHomePage();
		setButtonBackground();
		mAdapter = new MainPageAdapter(getSupportFragmentManager());
		mViewPaper = (ViewPager) findViewById(R.id.view_pager);
		mViewPaper.setAdapter(mAdapter);
		mViewPaper.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				mFlag = arg0;
				setButtonBackground();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		mViewPaper.setCurrentItem(mFlag);
		startService(new Intent(mContext, ScreenSaverService.class));
		ScreenSaverService.registerScreenOffCallback(mScreenOffCallback);
		if (ProjectConfigure.project == 1 && ProjectConfigure.size == 7) {
			startService(new Intent(mContext, PhysicsKeyService.class));
			PhysicsKeyService.registerKeyCallback(mKeyCallback);
		}
		DPFunction.setDoorCallInActivity(CallInFromDoorActivity.class);
		DPFunction.setRoomCallInActivity(CallInActivity.class);
		DPFunction.setAlarmActivity(AlarmActivity.class);
		DPFunction.loadAlarmFile();
		registerBroadcast();
		mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				PCToolService.getInstance().registerCallback(mPCToolCallback);
			}
		}, 3000);
		MyLog.print(TAG, "onCreate");
	}

	private void setButtonBackground() {
		if (mFlag < 0) {
			return;
		}
		for (int i = 0; i < mButtonArray.length; i++) {
			if (i == mFlag) {
				if (ProjectConfigure.project == 1) {
					mButtonArray[i].setBackgroundColor(getResources().getColor(R.color.DarkGreen));
				} else {
					mButtonArray[i].setBackgroundColor(getResources().getColor(R.color.TransparencyBlue));
				}
			} else {
				mButtonArray[i].setBackgroundColor(getResources().getColor(R.color.Transparency));
			}
		}
	}

	private class SafeModeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateDefenceView();
		}
	}

	private void updateDefenceView() {
		switch (DPFunction.getSafeMode()) {
		case ConstConf.HOME_MODE:
			mDefence.setImageResource(R.drawable.ic_in_home_mode_status_bar);
			break;
		case ConstConf.LEAVE_HOME_MODE:
			mDefence.setImageResource(R.drawable.ic_leave_home_mode_status_bar);
			break;
		case ConstConf.NIGHT_MODE:
			mDefence.setImageResource(R.drawable.ic_in_night_mode_status_bar);
			break;
		case ConstConf.UNSAFE_MODE:
			mDefence.setImageResource(R.drawable.ic_cancel_defence_status_bar);
			break;
		default:
			break;
		}
	}

	private class NetworkBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(EthernetManager.NETWORK_STATE_CHANGED_ACTION)) {
				if (CommonUT.getLanConnectState(ConstConf.LAN_NETWORK_CARD)) {
					if (mNetworkEthernet.getVisibility() != View.VISIBLE) {
						mNetworkEthernet.setVisibility(View.VISIBLE);
					}
				} else {
					if (mNetworkEthernet.getVisibility() != View.GONE) {
						mNetworkEthernet.setVisibility(View.GONE);
					}
				}
				return;
			}
			if (CommonUT.getLanConnectState(ConstConf.WAN_NETWORK_CARD)) {
				if (mNetworkWireless.getVisibility() != View.VISIBLE) {
					mNetworkWireless.setVisibility(View.VISIBLE);
				}
				String cmd = "ifconfig eth0 down \n";
				try {
					Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						String cmd = "ifconfig eth0 up \n";
						try {
							Runtime.getRuntime().exec(cmd);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, 1000);
			} else {
				if (mNetworkWireless.getVisibility() != View.GONE) {
					mNetworkWireless.setVisibility(View.GONE);
				}
			}
		}
	}

	private BroadcastReceiver mHomeKeyReceiver = new BroadcastReceiver() {

		String SYSTEM_REASON = "reason";
		String SYSTEM_HOME_KEY = "homekey";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (reason.equals(SYSTEM_HOME_KEY)) {
					App.getInstance().onHomeKey();
				}
			}
		}
	};

	/**
	 * 更新重启闹钟、APK和网络配置表
	 */
	private void registerBroadcast() {
		IntentFilter filter;
		mSafeModeReceiver = new SafeModeChangeReceiver();
		registerReceiver(mSafeModeReceiver, new IntentFilter(DPFunction.ACTION_SAFE_MODE));
		filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(EthernetManager.NETWORK_STATE_CHANGED_ACTION);
		mNetworkReceiver = new NetworkBroadcastReceiver();
		registerReceiver(mNetworkReceiver, filter);
		registerReceiver(mHomeKeyReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		filter = new IntentFilter(DPFunction.ACTION_UPDATE_NETCFG);
		mUpdateNetCfg = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				startActivity(new Intent(mContext, UpdateNetCfgActivity.class));
			}
		};
		registerReceiver(mUpdateNetCfg, filter);
		filter = new IntentFilter(DPFunction.ACTION_UPDATE_APK);
		mUpdateAPK = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// 更新应用程序
				if (null != InstallPackage.getPackageName(context, "com.dpower.updateapk")) {
					Intent i = new Intent();
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.setComponent(new ComponentName("com.dpower.updateapk", "com.dpower.updateapk.ApkUpdateActivity"));
					context.startActivity(i);
				}
			}
		};
		registerReceiver(mUpdateAPK, filter);
		filter = new IntentFilter(DPFunction.ACTION_UPDATE_TIME);
		mUpdateAlarm = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				RebootUT.getInstance(mContext).rebootAtTime(RebootUT.REBOOT_TIME);
			}
		};
		registerReceiver(mUpdateAlarm, filter);
		mMediaMountedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				MyLog.print(TAG, "插入SD卡");
				updateSystem(MainActivity.this);
			}
		};
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addDataScheme("file");
		registerReceiver(mMediaMountedReceiver, filter);
	}

	/**
	 * 升级系统(从SD卡拷贝升级包到指定的目录下进行升级系统)
	 * 
	 * @param context
	 */
	private boolean updateSystem(Context context) {
		File file = new File(ConstConf.UPDATE_SYSTEM_EXT_SD);
		if (!file.exists()) {
			MyLog.print(TAG, "文件不存在");
			return false;
		}
		context.startActivity(new Intent(context, UpdateSystemActivity.class));
		return true;
	}

	private ScreenOffCallback mScreenOffCallback = new ScreenOffCallback() {

		@Override
		public void onScreenOff(int flag) {
			MyLog.print(TAG, "isStartScreenSaver = " + isStartScreenSaver);
			if (!isStartScreenSaver) {
				isStartScreenSaver = true;
				return;
			}
			switch (flag) {
			case ScreenSaverService.BLACK:
				isScreenOff = true;
				MyLog.print(TAG, "ScreenSaverBlackActivity");
				startActivity(new Intent(mContext, ScreenSaverBlackActivity.class));
				break;
			case ScreenSaverService.CALENDAR:
				isScreenOff = true;
				MyLog.print(TAG, "ScreenSaverCalendarActivity");
				startActivity(new Intent(mContext, ScreenSaverCalendarActivity.class));
				break;
			case ScreenSaverService.PICTURE:
				isScreenOff = true;
				MyLog.print(TAG, "ScreenSaverPictureActivity");
				startActivity(new Intent(mContext, ScreenSaverPictureActivity.class));
				break;
			default:
				break;
			}
		}
	};

	private PCToolCallback mPCToolCallback = new PCToolCallback() {

		@Override
		public void updateSystem(String filePath) {
			File temp = new File(filePath);
			if (temp.exists()) {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName("com.example.hello", "com.example.hello.MainActivity"));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}

		@Override
		public void updateNetCfg(String filePath) {
			File temp = new File(filePath);
			if (temp.exists()) {
				FileOperate fileOperate = new FileOperate();
				if (fileOperate.from(filePath).to(ConstConf.NET_CFG_PATH)) {
					Intent intent = new Intent(Intent.ACTION_REBOOT);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
				temp.delete();
			}
		}

		@Override
		public void updateApp(String filePath) {
			File temp = new File(filePath);
			if (temp.exists()) {
				FileOperate fileOperate = new FileOperate();
				if (fileOperate.from(filePath).to(ConstConf.SD_DIR + "Pub2700.apk")) {
					// 更新应用程序
					if (null != InstallPackage.getPackageName(mContext, "com.dpower.updateapk")) {
						Intent i = new Intent();
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						i.setComponent(
								new ComponentName("com.dpower.updateapk", "com.dpower.updateapk.ApkUpdateActivity"));
						startActivity(i);
					}
				}
				temp.delete();
			}
		}

		@Override
		public void reset() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					SystemResetDialog.reset(MainActivity.this);
					try {
						String cmd = "su -c reboot";
						Runtime.getRuntime().exec(cmd);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	};

	private KeyCallback mKeyCallback = new KeyCallback() {

		@Override
		public void onKey(int keyIO) {
			if (isScreenOff) {
				isScreenOff = false;
				return;
			}
			switch (keyIO) {//物理按键
			case PhysicsKeyService.MESSAGE:
				startActivity(new Intent(mContext, SMSActivity.class));
				break;
			case PhysicsKeyService.VOLUME:
				//startActivity(new Intent(mContext, CallGuardActivity.class));
				break;
			case PhysicsKeyService.MONITOR:
				startActivity(new Intent(mContext, MonitorActivity.class));
				break;
			case PhysicsKeyService.UNLOCK:
				break;
			case PhysicsKeyService.HANGUP:
				break;
			default:
				break;
			}
		}
	};

	/**
	 * @param keySwitch
	 *            美一的按键：呼叫管理中心，监视，开锁，接听或挂断
	 */
	public static void setKeySwitch(boolean[] keySwitch) {
		mMeeYiKeySwitch = keySwitch;
	}

	/**
	 * @return 美一的按键：呼叫管理中心，监视，开锁，接听或挂断
	 */
	public static boolean[] getKeySwitch() {
		return mMeeYiKeySwitch;
	}

	@Override
	protected void onResume() {
		findViewById(R.id.root_view).setBackground(SPreferences.getInstance().getWallpaper());
		updateDefenceView();
		PhysicsKeyService.startKeyListener(true);
		PhysicsKeyService.setKeySwitch(new boolean[] { true, true, true, true, true });
		setKeySwitch(new boolean[] { true, true, true, true });
		updateNetworkView();
		super.onResume();
	}

	private void updateNetworkView() {
		if (CommonUT.getLanConnectState(ConstConf.LAN_NETWORK_CARD)) {
			mNetworkEthernet.setVisibility(View.VISIBLE);
		} else {
			mNetworkEthernet.setVisibility(View.GONE);
		}
		if (CommonUT.getLanConnectState(ConstConf.WAN_NETWORK_CARD)) {
			mNetworkWireless.setVisibility(View.VISIBLE);
		} else {
			mNetworkWireless.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		stopService(new Intent(mContext, ScreenSaverService.class));
		ScreenSaverService.unRegisterScreenOffCallback(mScreenOffCallback);
		PCToolService.getInstance().unregisterCallback(mPCToolCallback);
		if (ProjectConfigure.project == 1 && ProjectConfigure.size == 7) {
			stopService(new Intent(mContext, PhysicsKeyService.class));
			PhysicsKeyService.unregisterKeyCallback(mKeyCallback);
			PhysicsKeyService.startKeyListener(false);
		}
		unregisterBroadcast();
		MyLog.print(TAG, "onDestroy");
		mButtonArray = null;
		super.onDestroy();
	}

	private void unregisterBroadcast() {
		if (mNetworkReceiver != null) {
			unregisterReceiver(mNetworkReceiver);
			mNetworkReceiver = null;
		}
		if (mSafeModeReceiver != null) {
			unregisterReceiver(mSafeModeReceiver);
			mSafeModeReceiver = null;
		}
		if (mHomeKeyReceiver != null) {
			unregisterReceiver(mHomeKeyReceiver);
			mHomeKeyReceiver = null;
		}
		if (mUpdateNetCfg != null) {
			unregisterReceiver(mUpdateNetCfg);
			mUpdateNetCfg = null;
		}
		if (mUpdateAPK != null) {
			unregisterReceiver(mUpdateAPK);
			mUpdateAPK = null;
		}
		if (mUpdateAlarm != null) {
			unregisterReceiver(mUpdateAlarm);
			mUpdateAlarm = null;
		}
		if (mMediaMountedReceiver != null) {
			unregisterReceiver(mMediaMountedReceiver);
			mMediaMountedReceiver = null;
		}
	}

	@Override
	public void onClick(View v) {
		if (ProjectConfigure.project == 2 && ProjectConfigure.isSimple) {
			switch (v.getId()) {
			case R.id.btn_home:
				mFlag = MainPageAdapter.getHomePage();
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_house_sercurity:
				mFlag = MainPageAdapter.getHomePage() + 1;
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_visual_intercom:
				mFlag = MainPageAdapter.getHomePage() + 2;
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_intelligent_home:
				mFlag = MainPageAdapter.getHomePage() + 3;
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_setting:
				mFlag = MainPageAdapter.getHomePage() + 4;
				mViewPaper.setCurrentItem(mFlag);
				break;
			default:
				break;
			}
		} else {
			switch (v.getId()) {
			case R.id.btn_cloud_intercom:
				mFlag = MainPageAdapter.getHomePage() - 3;
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_house_sercurity:
				mFlag = MainPageAdapter.getHomePage() - 2;
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_visual_intercom:
				mFlag = MainPageAdapter.getHomePage() - 1;
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_home:
				mFlag = MainPageAdapter.getHomePage();
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_intelligent_home:
				mFlag = MainPageAdapter.getHomePage() + 1;
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_setting:
				mFlag = MainPageAdapter.getHomePage() + 2;
				mViewPaper.setCurrentItem(mFlag);
				break;
			case R.id.btn_entertainment:
				mFlag = MainPageAdapter.getHomePage() + 3;
				mViewPaper.setCurrentItem(mFlag);
				break;
			default:
				break;
			}
		}
		setButtonBackground();
	}
}
