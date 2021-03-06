package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.util.List;

import com.dpower.domain.AlarmInfo;
import com.dpower.domain.AlarmVideo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.dialog.DisalarmDialog;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 报警界面
 */
public class AlarmActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "AlarmActivity";

	/** 用来存放报警区域 */
	private int[] mAlarmingArea = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
	private AlarmAdapter mAdapter;
	private List<AlarmInfo> mInfoList;
	private ListView mListView;
	private Receiver mReceiver;
	private AlarmVideo mAlarmVideo;// 录像保存到数据库
	private SurfaceView mSurfaceView;// 录制视频
	private SurfaceHolder mSurfaceHolder;
	private MediaRecorder mMediaRecorder;// 录制视频的类
	private boolean mIsRecording = false;
	private Runnable mVideoRunnable; // 录制视频的线程
	private Handler mVideoHandler;
	private boolean[] mKeySwitch;
	private boolean[] mMeeYiKeySwitch;
	private int mWidth;
	private int mHeight;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		init();
	}

	private void init() {
		mContext = this;
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mWidth = displayMetrics.widthPixels;
		mHeight = displayMetrics.heightPixels;
		mReceiver = new Receiver();
		registerReceiver(mReceiver, new IntentFilter(DPFunction.ACTION_ALARMING));
		int i = getIntent().getIntExtra(DPFunction.ALARM_ID, 0);
		if (i == DPFunction.DISALARMING) {
			finish();
			return;
		}
		mInfoList = DPFunction.getAlarmInfoList(DPFunction.getSafeMode());
		if (mInfoList == null) {
			finish();
			return;
		}
		mListView = (ListView) findViewById(R.id.disalarm_list);
		checkAlarm(i);
		mAdapter = new AlarmAdapter();
		mListView.setAdapter(mAdapter);
		findViewById(R.id.btn_disalarm).setOnClickListener(this);
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
			audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		}
		startVideoRecord();
	}

	/**
	 * 检查报警区域是否已经显示在list中
	 */
	private void checkAlarm(int location) {
		int alarm = location + 1;
		if (mAlarmingArea[location] == 0) {
			MyLog.print(MyLog.ERROR, TAG, "新报警防区 = " + alarm);
			mAlarmingArea[location] = 1;
		} else {
			MyLog.print(MyLog.ERROR, TAG, "防区已经在报警 = " + alarm);
		}
	}

	/**
	 * 启动安防录像
	 */
	private void startVideoRecord() {
		mVideoRunnable = new Runnable() {

			@Override
			public void run() {
				// 最长录制5分钟
				if (mIsRecording) {
					stopVideoRecord();
				}
			}
		};
		mVideoHandler = new Handler();
		mVideoHandler.postDelayed(mVideoRunnable, 5 * 60 * 1000);
		mAlarmVideo = new AlarmVideo();
		mAlarmVideo.time = CommonUT.formatTime(System.currentTimeMillis());
		mAlarmVideo.area = 1001;
		mAlarmVideo.type = 2001;
		mAlarmVideo.path = ConstConf.ALARM_VIDEO_PATH + System.currentTimeMillis() + ".mp4";
		File file = new File(ConstConf.ALARM_VIDEO_PATH);
		if (!file.exists()) {
			file.mkdir();
		}
		DPFunction.addAlarmVideo(mAlarmVideo);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		initVideoRecord();
		mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (mIsRecording) {
					stopVideoRecord();
				}
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				mSurfaceHolder = holder;
				try {
					// 准备录制
					mMediaRecorder.prepare();
					// 开始录制
					mMediaRecorder.start();
					mIsRecording = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				mSurfaceHolder = holder;
			}
		});
	}

	/**
	 * 结束录像
	 */
	private void stopVideoRecord() {
		if (mMediaRecorder != null) {
			mMediaRecorder.stop();
			// 释放资源
			mMediaRecorder.release();
			mSurfaceView = null;
			mSurfaceHolder = null;
			mMediaRecorder = null;
		}
		mIsRecording = false;
	}

	@SuppressWarnings("deprecation")
	private void initVideoRecord() {
		// setType必须设置，要不出错.
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setOnErrorListener(null);
		// 设置录制视频源为Camera(相机)
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// 直接调用系统的配置
		mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
		mMediaRecorder.setVideoSize(mWidth, mHeight);
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		mMediaRecorder.setOutputFile(mAlarmVideo.path);
		mMediaRecorder.setOnInfoListener(null);
	}

	@Override
	protected void onResume() {
		ScreenUT.getInstance().acquireWakeLock();
		mKeySwitch = PhysicsKeyService.getKeySwitch();
		PhysicsKeyService.setKeySwitch(new boolean[] { false, false, false, false, false });
		mMeeYiKeySwitch = MainActivity.getKeySwitch();
		MainActivity.setKeySwitch(new boolean[] { false, false, false, false });
		MyLog.print(TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		ScreenUT.getInstance().releaseWakeLock();
		MyLog.print(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.print(TAG, "onStop");
		PhysicsKeyService.setKeySwitch(mKeySwitch);
		MainActivity.setKeySwitch(mMeeYiKeySwitch);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		MyLog.print(TAG, "onDestroy");
		destroy();
		super.onDestroy();
	}

	private void destroy() {
		if (mVideoHandler != null) {
			mVideoHandler.removeCallbacks(mVideoRunnable);
		}
		// 停止录像
		if (mIsRecording) {
			stopVideoRecord();
		}
		if (mInfoList != null) {
			mInfoList.clear();
			mInfoList = null;
		}
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		DPFunction.setAlarmActivityState(false);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_disalarm) {
			findViewById(R.id.btn_disalarm).setEnabled(false);
			startActivityForResult(new Intent(mContext, DisalarmDialog.class), 100);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 100 && resultCode == RESULT_OK) {
			// 修改为撤防模式
			findViewById(R.id.btn_disalarm).setEnabled(false);
			DPFunction.disalarm(data != null);
			Intent intent = new Intent();
			intent.setAction(DPFunction.ACTION_SAFE_MODE);
			sendBroadcast(intent);
			destroy();
			finish();
		} else {
			findViewById(R.id.btn_disalarm).setEnabled(true);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class AlarmAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return mInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_alarm_list, null);
				holder = new ItemHolder();
				holder.id = (TextView) convertView.findViewById(R.id.tv_id);
				holder.areaName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.areaType = (TextView) convertView.findViewById(R.id.tv_type);
				holder.connection = (TextView) convertView.findViewById(R.id.tv_connection);
				holder.isAbnormal = (ImageView) convertView.findViewById(R.id.image_abnormal);
				convertView.setTag(holder);
			} else {
				holder = (ItemHolder) convertView.getTag();
			}

			holder.id.setText("" + (position + 1));
			holder.areaName.setText(mInfoList.get(position).areaName);
			holder.areaType.setText(mInfoList.get(position).areaType);
			holder.connection.setText(mInfoList.get(position).delayTime + "");
			if (mAlarmingArea[position] == 1) {
				holder.isAbnormal.setBackgroundColor(getResources().getColor(R.color.DarkRed));
			} else {
				holder.isAbnormal.setBackgroundColor(getResources().getColor(R.color.DarkGreen));
			}
			return convertView;
		}

		class ItemHolder {
			public TextView id;
			public TextView areaName;
			public TextView areaType;
			public TextView connection;
			public ImageView isAbnormal;
		}
	}

	private class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int i = intent.getIntExtra(DPFunction.ALARM_ID, 0);
			MyLog.print(TAG, "i = " + i);
			if (i == DPFunction.DISALARMING) {
				destroy();
				finish();
				return;
			}
			checkAlarm(i);
			mAdapter.notifyDataSetChanged();
		}
	}
}
