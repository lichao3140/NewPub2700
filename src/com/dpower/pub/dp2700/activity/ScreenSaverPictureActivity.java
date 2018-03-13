package com.dpower.pub.dp2700.activity;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.service.PhysicsKeyService.KeyCallback;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

/**
 * 图片屏保
 */
public class ScreenSaverPictureActivity extends BaseActivity {
	private static final String TAG = "ScreenSaverPictureActivity";

	private AnimationSet mAnimationSet;
	private AlphaAnimation mAlphaAnimation;
	private ImageView mPicture;
	private int[] mWallpaperID = { R.drawable.test_wallpaper1, R.drawable.test_wallpaper2, R.drawable.test_wallpaper3,
			R.drawable.test_wallpaper4 };
	private int mWallpaperCount = 0;
	private int mOnPauseCount = 0;
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
		setContentView(R.layout.activity_screen_saver_picture);
		init();
	}

	private void init() {
		mPicture = (ImageView) findViewById(R.id.image_picture);
		mAnimationSet = new AnimationSet(true);
		mAlphaAnimation = new AlphaAnimation(1.0f, 0.2f);
		mAlphaAnimation.setDuration(15 * 1000);
		mAnimationSet.addAnimation(mAlphaAnimation);
		mPicture.startAnimation(mAnimationSet);
		mPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if (mWallpaperCount < mWallpaperID.length - 1) {
					mWallpaperCount++;
				} else {
					mWallpaperCount = 0;
				}
				mPicture.setBackgroundResource(mWallpaperID[mWallpaperCount]);
				mPicture.startAnimation(mAnimationSet);
				handler.postDelayed(this, 15 * 1000);
			}
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
