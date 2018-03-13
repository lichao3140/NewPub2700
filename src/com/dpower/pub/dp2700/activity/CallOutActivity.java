package com.dpower.pub.dp2700.activity;

import java.io.File;

import com.dpower.callback.IntercomCallback;
import com.dpower.domain.CallInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.broadcastreceiver.AlarmFinishCallBroadcast;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.service.PhysicsKeyService.KeyCallback;
import com.dpower.pub.dp2700.tools.MediaPlayerTools;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.RoomInfoUT;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.pub.dp2700.tools.VolumePopupWindow;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;
import com.example.dpservice.CallReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ethernet.EthernetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 呼出界面
 */
public class CallOutActivity extends BaseActivity implements OnClickListener, IntercomCallback {
	private static final String TAG = "CallOutActivity";

	public static final String INTENT_EXTRA = "CallOutActivity_RoomCode";
	private final int KEY_HANG_UP = 100;
	private ImageView mVideoView;
	private TextView mCallInfo;
	private Button mVideoSwitch;
	private Button mButtonHangUp;
	private CallReceiver mCallOutReceiver;
	private int mCallSessionID = 0;
	private boolean mVideoShow = false;
	private ImageButton mVolumeUp, mVolumeDown;
	private int mCurrentVolume;
	/** 临时保存通话开始之前的音量，在通话结束时恢复 */
	private int mCurrentSystemVolume = -1;
	private VolumePopupWindow mVolumePopupWindow;
	private int mExitTips;
	private MediaPlayerTools mPlayerTools;
	private AlarmFinishCallBroadcast mAlarmBroadcast;
	private NetworkBroadcastReceiver mNetworkReceiver;
	private boolean mIsHangUp = false;
	private boolean[] mKeySwitch;
	private boolean[] mMeeYiKeySwitch;
	private Context mContext;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case KEY_HANG_UP:
				if (mButtonHangUp != null) {
					mButtonHangUp.performClick();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private KeyCallback mKeyCallback = new KeyCallback() {

		@Override
		public void onKey(int keyIO) {
			switch (keyIO) {
			case PhysicsKeyService.MESSAGE:
				break;
			case PhysicsKeyService.VOLUME:
				break;
			case PhysicsKeyService.MONITOR:
				break;
			case PhysicsKeyService.UNLOCK:
				break;
			case PhysicsKeyService.HANGUP:
				mHandler.sendEmptyMessage(KEY_HANG_UP);
				break;
			default:
				break;
			}
		}
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case 134:
			MyLog.print("接听键被按下");
			if (event.getAction() == KeyEvent.ACTION_UP) {
				mButtonHangUp.performClick();
			}
			break;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		init();
	}

	private void init() {
		if (!CommonUT.getLanConnectState(ConstConf.LAN_NETWORK_CARD)) {
			MyToast.show(R.string.check_network_connection);
			finish();
			return;
		}
		mContext = this;
		mExitTips = R.string.call_end;
		mPlayerTools = new MediaPlayerTools(true);
		mVideoView = (ImageView) findViewById(R.id.image_video);
		mCallInfo = (TextView) findViewById(R.id.call_info);
		mVideoSwitch = (Button) findViewById(R.id.btn_open_video);
		mVideoSwitch.setOnClickListener(this);
		mVideoSwitch.setClickable(false);
		mButtonHangUp = (Button) findViewById(R.id.btn_hang_up);
		mVolumePopupWindow = new VolumePopupWindow(mContext,
				LayoutInflater.from(mContext).inflate(R.layout.activity_call, null));
		mButtonHangUp.setOnClickListener(this);
		mVolumeUp = (ImageButton) findViewById(R.id.btn_volume_up);
		mVolumeUp.setOnClickListener(this);
		mVolumeUp.setClickable(false);
		mVolumeDown = (ImageButton) findViewById(R.id.btn_volume_down);
		mVolumeDown.setOnClickListener(this);
		mVolumeDown.setClickable(false);
		mCurrentVolume = SPreferences.getInstance().getTalkingVolume();
		mCallOutReceiver = new CallReceiver(this, CallReceiver.CALL_OUT_ACTION);
		registerReceiver(mCallOutReceiver, mCallOutReceiver.getFilter());
		mAlarmBroadcast = new AlarmFinishCallBroadcast();
		registerReceiver(mAlarmBroadcast, new IntentFilter(DPFunction.ACTION_ALARMING));
		mNetworkReceiver = new NetworkBroadcastReceiver();
		registerReceiver(mNetworkReceiver, new IntentFilter(EthernetManager.NETWORK_STATE_CHANGED_ACTION));
		PhysicsKeyService.registerKeyCallback(mKeyCallback);
		String roomCode = getIntent().getStringExtra(INTENT_EXTRA);
		if (roomCode != null) {
			if (DPFunction.callOut(roomCode) != 0) {
				MyToast.show(R.string.call_error);
				hangUp();
				finish();
				return;
			}
		}
		int count = DPFunction.getCallOutSize();
		if (count > 0) {
			CallInfo info = DPFunction.findCallOutIndex(0);
			mCallInfo.setText(getRoomName(info.getRemoteCode()));
		} else {
			MyToast.show(R.string.busy_line);
			hangUp();
			finish();
			return;
		}

		String path = SPreferences.getInstance().getRingAbsolutePath();
		if (TextUtils.isEmpty(path)) {
			mPlayerTools.initDefaultRingFile(getApplicationContext());
			path = SPreferences.getInstance().getRingAbsolutePath();
		}
		// 当铃声设置为SD卡的音频时，拔出SD卡后要恢复默认
		if (!new File(path).exists()) {
			SPreferences.getInstance().setRingAbsolutePath(ConstConf.RING_PATH + ConstConf.RING_MP3);
			path = SPreferences.getInstance().getRingAbsolutePath();
		}
		mPlayerTools.playMusic(path);
	}

	private CharSequence getRoomName(String roomCode) {
		StringBuffer result = new StringBuffer();
		result.append(getString(R.string.call_from_info));
		RoomInfoUT infoUT = new RoomInfoUT(roomCode);
		result.append(infoUT.getRoomName(mContext));
		return result.toString();
	}

	private void hangUp() {
		if (mPlayerTools != null) {
			mPlayerTools.release();
			mPlayerTools = null;
		}
		if (mCallSessionID != 0) {
			DPFunction.setLocalVideoVisable(mCallSessionID, false);
			DPFunction.setVideoDisplayArea(mCallSessionID, 0, 0, 0, 0);
			DPFunction.callHangUp(mCallSessionID);
			mCallSessionID = 0;
		}
		DPFunction.callOutHangUp();
		if (mCallOutReceiver != null) {
			unregisterReceiver(mCallOutReceiver);
			mCallOutReceiver = null;
		}
		if (mAlarmBroadcast != null) {
			unregisterReceiver(mAlarmBroadcast);
			mAlarmBroadcast = null;
		}
		mIsHangUp = true;
		if (mCurrentSystemVolume != -1) {
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentSystemVolume, 0);
			mCurrentSystemVolume = -1;
		}
	}

	@Override
	protected void onResume() {
		MyLog.print(TAG, "onResume");
		ScreenUT.getInstance().acquireWakeLock();
		mKeySwitch = PhysicsKeyService.getKeySwitch();
		PhysicsKeyService.setKeySwitch(new boolean[] { false, false, false, false, true });
		mMeeYiKeySwitch = MainActivity.getKeySwitch();
		MainActivity.setKeySwitch(new boolean[] { false, false, false, true });
		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.print(TAG, "onPause");
		ScreenUT.getInstance().releaseWakeLock();
		PhysicsKeyService.setKeySwitch(mKeySwitch);
		MainActivity.setKeySwitch(mMeeYiKeySwitch);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		MyToast.show(mExitTips);
		PhysicsKeyService.unregisterKeyCallback(mKeyCallback);
		if (!mIsHangUp) {
			hangUp();
		}
		if (mNetworkReceiver != null) {
			unregisterReceiver(mNetworkReceiver);
			mNetworkReceiver = null;
		}
		if (mVolumePopupWindow != null) {
			mVolumePopupWindow.cancelPopupWindow();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_open_video:
			if (mCallSessionID != 0) {
				mVideoShow = !mVideoShow;
				if (mVideoShow) {
					mVideoSwitch.setText(R.string.text_close_video);
				} else {
					mVideoSwitch.setText(R.string.text_open_video);
				}
				DPFunction.setLocalVideoVisable(mCallSessionID, mVideoShow);
			}
			break;
		case R.id.btn_hang_up:
			mExitTips = R.string.call_end;
			hangUp();
			finish();
			break;
		case R.id.btn_volume_up:
			if (mCurrentVolume < 5) {
				mCurrentVolume++;
				SPreferences.getInstance().setTalkingVolume(mCurrentVolume);
				DPFunction.setAudioVolume(mCallSessionID, mCurrentVolume * 20);
			}
			mVolumePopupWindow.show(mCurrentVolume);
			break;
		case R.id.btn_volume_down:
			if (mCurrentVolume > 0) {
				mCurrentVolume--;
				SPreferences.getInstance().setTalkingVolume(mCurrentVolume);
				DPFunction.setAudioVolume(mCallSessionID, mCurrentVolume * 20);
			}
			mVolumePopupWindow.show(mCurrentVolume);
			break;
		default:
			break;
		}
	}

	@Override
	public void onRingTimeOut(int CallSessionID, int MsgType, String MsgContent) {
		mExitTips = R.string.ring_time_out;
		hangUp();
		finish();
	}

	@Override
	public void onTalkTimeOut(int CallSessionID, int MsgType, String MsgContent) {
		mExitTips = R.string.call_time_out;
		hangUp();
		finish();
	}

	@Override
	public void onMonitorTimeOut(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onAckRing(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onAckBusy(int CallSessionID, int MsgType, String MsgContent) {
		if (DPFunction.getCallOutSize() == 0) {
			mExitTips = R.string.busy_line;
			hangUp();
			finish();
		}
	}

	@Override
	public void onAckNoMeia(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onAckHold(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onCallOutAck(int CallSessionID, int MsgType, String MsgContent) {
		MyLog.print(TAG, "onCallOutAck ");
		if (MsgContent.equals("ring")) {

		}
	}

	@Override
	public void onNewCallIn(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onRemoteHangUp(int CallSessionID, int MsgType, String MsgContent) {
		mExitTips = R.string.be_cutoff;
		hangUp();
		finish();
	}

	@Override
	public void onRemoteAccept(int CallSessionID, int MsgType, String MsgContent) {
		// 对方接听后需停止铃声，设置图像是否可见（默认不可见），设置对方图像在本机的显示区域
		mVideoSwitch.setClickable(true);
		if (mPlayerTools != null) {
			mPlayerTools.release();
			mPlayerTools = null;
		}
		mCallSessionID = CallSessionID;
		DPFunction.setAudioVolume(mCallSessionID, mCurrentVolume * 20);
		DPFunction.setLocalVideoVisable(mCallSessionID, mVideoShow);
		DPFunction.setVideoDisplayArea(mCallSessionID, 0, getResources().getInteger(R.integer.title_bar_height),
				getResources().getInteger(R.integer.call_management_width),
				getResources().getInteger(R.integer.call_management_height));
		mVolumeDown.setClickable(true);
		mVolumeUp.setClickable(true);
		mVideoView.setBackgroundColor(Color.BLACK);
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mCurrentSystemVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
	}

	@Override
	public void onRemoteHold(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onRemoteWake(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onError(int CallSessionID, int MsgType, String MsgContent) {
		if (DPFunction.getCallOutSize() == 0) {
			if (mCallInfo != null) {
				mExitTips = R.string.no_online;
				mCallInfo = null;
			}
			hangUp();
			finish();
		}
	}

	@Override
	public void onMessage(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onMessageError(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onPhoneAccept() {

	}

	@Override
	public void onPhoneHangUp() {

	}

	private class NetworkBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(EthernetManager.NETWORK_STATE_CHANGED_ACTION)) {
				if (!CommonUT.getLanConnectState(ConstConf.LAN_NETWORK_CARD)) {
					MyToast.show(R.string.network_disconnect);
					hangUp();
					finish();
				}
			}
		}
	}
}
