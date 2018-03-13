package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseFragmentActivity;
import com.dpower.pub.dp2700.fragment.KeyboardNumberBlackGreyFragment;
import com.dpower.pub.dp2700.fragment.KeyboardNumberBlackGreyFragment.OnNumberKeyboardListener;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.util.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * 解除报警窗口（报警时撤防）
 */
public class DisalarmDialog extends BaseFragmentActivity implements OnClickListener {
	private static final String TAG = "DisalarmDialog";

	private LinearLayout mInfoWindow;
	private EditText mEditPassword;
	private KeyboardNumberBlackGreyFragment mKeyboard;
	private EditTextTool mEditTool;
	private Receiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_password);
		init();
	}

	private void init() {
		mReceiver = new Receiver();
		registerReceiver(mReceiver, new IntentFilter(DPFunction.ACTION_ALARMING));
		mEditPassword = (EditText) findViewById(R.id.et_password);
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (LinearLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		mEditTool = EditTextTool.getInstance();
		mKeyboard = new KeyboardNumberBlackGreyFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.frame_keyboard, mKeyboard)
				.commitAllowingStateLoss();
		setKeyboardListener();
	}

	private void setKeyboardListener() {
		mKeyboard.setOnKeyboardListener(new OnNumberKeyboardListener() {

			@Override
			public void onKeyboardClick(String key) {
				if (key.equals(KeyboardNumberBlackGreyFragment.OK)) {
					if (TextUtils.isEmpty(mEditPassword.getText().toString().trim())) {
						MyToast.show(R.string.wifi_password_input);
						return;
					}
					String safePassword = DPFunction.getSafePassword(false);
					String holdingPassword = DPFunction.getSafePassword(true);
					if (mEditPassword.getText().toString().trim().equals(safePassword)) {
						setResult(RESULT_OK, null);
						finish();
					} else if (mEditPassword.getText().toString().trim().equals(holdingPassword)) {
						setResult(RESULT_OK, new Intent());
						finish();
					} else {
						MyToast.show(R.string.password_error);
					}
				}
				if (mEditPassword.hasFocus()) {
					editText(mEditPassword, key);
				}
			}
		});
	}

	private void editText(EditText editText, String key) {
		mEditTool.setEditText(editText);
		if (key.equals(KeyboardNumberBlackGreyFragment.DELETE)) {
			mEditTool.deleteText();
		} else {
			if (editText.getText().toString().length() < 6) {
				mEditTool.appendTextTo(key);
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		getSupportFragmentManager().beginTransaction().remove(mKeyboard).commitAllowingStateLoss();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_window:
			finish();
			break;
		default:
			break;
		}
	}

	public class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int i = intent.getIntExtra(DPFunction.ALARM_ID, 0);
			MyLog.print("i = " + i);
			if (i == DPFunction.DISALARMING) {
				finish();
				return;
			}
		}
	}
}
