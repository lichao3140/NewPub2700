package com.dpower.pub.dp2700.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.service.PhysicsKeyService.KeyCallback;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 日历屏保
 */
public class ScreenSaverCalendarActivity extends BaseActivity {
	private static final String TAG = "ScreenSaverCalendarActivity";

	private TextView mTextHour;
	private TextView mTextColon;
	private TextView mTextMinute;
	private TextView mTextDate;
	private TextView mTextWeek;
	private ImageView mTimeFlag;
	private TextView mTextMeridiem;
	private Time mTime;
	private int mOnPauseCount = 0;
	private SimpleDateFormat mDateTextFormat;
	private SimpleDateFormat mWeekTextFormat;
	private boolean[] mKeySwitch;
	private boolean[] mMeeYiKeySwitch;

	private KeyCallback mKeyCallback = new KeyCallback() {

		@Override
		public void onKey(int keyIO) {
			switch (keyIO) {
			case PhysicsKeyService.MESSAGE:
			case PhysicsKeyService.VOLUME:
			case PhysicsKeyService.MONITOR:
			case PhysicsKeyService.UNLOCK:
			case PhysicsKeyService.HANGUP:
				finish();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case 131:
			MyLog.print(TAG, "呼叫管理中心键被按下");
			if (event.getAction() == KeyEvent.ACTION_UP) {
				finish();
			}
			break;
		case 133:
			MyLog.print(TAG, "监视键被按下");
			if (event.getAction() == KeyEvent.ACTION_UP) {
				finish();
			}
			break;
		case 134:
			MyLog.print(TAG, "接听键被按下");
			if (event.getAction() == KeyEvent.ACTION_UP) {
				finish();
			}
			break;
		case 135:
			MyLog.print(TAG, "开锁键被按下");
			if (event.getAction() == KeyEvent.ACTION_UP) {
				finish();
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
		setContentView(R.layout.activity_screen_saver_calendar);
		init();
	}

	private void init() {
		findViewById(R.id.root_view).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mDateTextFormat = new SimpleDateFormat(getString(R.string.format_date));
		mWeekTextFormat = new SimpleDateFormat("EEEE");
		mTextHour = (TextView) findViewById(R.id.tv_time_hour);
		mTextColon = (TextView) findViewById(R.id.tv_time_colon);
		mTextMinute = (TextView) findViewById(R.id.tv_time_minute);
		mTextDate = (TextView) findViewById(R.id.tv_date);
		mTextWeek = (TextView) findViewById(R.id.tv_week);
		mTimeFlag = (ImageView) findViewById(R.id.image_time_flag);
		mTextMeridiem = (TextView) findViewById(R.id.tv_meridiem);
		mTime = new Time();
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Date localDate = new Date();
				mTextDate.setText(mDateTextFormat.format(localDate));
				mTextWeek.setText(mWeekTextFormat.format(localDate));
				mTime.setToNow();
				if (mTime.hour < 10) {
					String hour = "0" + Integer.toString(mTime.hour);
					mTextHour.setText(hour);
				} else {
					mTextHour.setText(Integer.toString(mTime.hour));
				}
				if (mTime.minute < 10) {
					String minute = "0" + Integer.toString(mTime.minute);
					mTextMinute.setText(minute);
				} else {
					mTextMinute.setText(Integer.toString(mTime.minute));

				}
				if (mTextColon.getVisibility() == View.VISIBLE) {
					mTextColon.setVisibility(View.INVISIBLE);
				} else {
					mTextColon.setVisibility(View.VISIBLE);
				}
				if (mTime.hour > 7 && mTime.hour < 19) {
					mTimeFlag.setImageResource(R.drawable.bg_main_time_day);
				} else {
					mTimeFlag.setImageResource(R.drawable.bg_main_time_night);
				}
				if (mTime.hour < 12) {
					mTextMeridiem.setText("AM");
				} else {
					mTextMeridiem.setText("PM");
				}
				handler.postDelayed(this, 1000);
			};
		};
		handler.post(runnable);
		if (ProjectConfigure.project == 2) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					MainActivity.isStartScreenSaver = false;
					ScreenUT.getInstance().goToSleep();
				}
			}, 180000);
		}
		PhysicsKeyService.registerKeyCallback(mKeyCallback);
	}

	@Override
	protected void onStart() {
		MyLog.print(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		MyLog.print(TAG, "onResume");
		mKeySwitch = PhysicsKeyService.getKeySwitch();
		PhysicsKeyService.setKeySwitch(new boolean[] { true, true, true, true, true });
		mMeeYiKeySwitch = MainActivity.getKeySwitch();
		MainActivity.setKeySwitch(new boolean[] { false, false, false, false });
		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.print(TAG, "onPause");
		PhysicsKeyService.setKeySwitch(mKeySwitch);
		MainActivity.setKeySwitch(mMeeYiKeySwitch);
		// 来电、报警时关闭屏保界面
		mOnPauseCount++;
		MyLog.print(TAG, "mOnPauseCount = " + mOnPauseCount);
		if (mOnPauseCount > 1) {
			MyLog.print(TAG, "onPause finish");
			finish();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.print(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onRestart() {
		MyLog.print(TAG, "onRestart");
		finish();
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		PhysicsKeyService.unregisterKeyCallback(mKeyCallback);
		ScreenUT.getInstance().releaseWakeLock();
		MainActivity.isScreenOff = false;
		MyLog.print(TAG, "onDestroy");
		super.onDestroy();
	}
}
