package com.dpower.pub.dp2700.activity.dialog;

import org.android.talkserversdk.TalkManager;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseFragmentActivity;
import com.dpower.pub.dp2700.activity.SystemSettingActivity;
import com.dpower.pub.dp2700.fragment.KeyboardNumberWhiteFragment;
import com.dpower.pub.dp2700.fragment.KeyboardNumberWhiteFragment.OnNumberKeyboardListener;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * 检查密码窗口
 */
public class CheckPasswordDialog extends BaseFragmentActivity implements OnClickListener {

	public static final String DISALARM = "disalarm";
	public static final String SYSTEM_SETTING = "systemSetting";
	private LinearLayout mInfoWindow;
	private EditText mEditPassword;
	private KeyboardNumberWhiteFragment mKeyboard;
	private EditTextTool mEditTool;
	private String mAction;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_password);
		init();
	}

	private void init() {
		mContext = this;
		mAction = getIntent().getAction();
		if (mAction == null) {
			finish();
			return;
		}
		mEditPassword = (EditText) findViewById(R.id.et_password);
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (LinearLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		mEditTool = EditTextTool.getInstance();
		mKeyboard = new KeyboardNumberWhiteFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.frame_keyboard, mKeyboard)
				.commitAllowingStateLoss();
		setKeyboardListener();
	}

	private void setKeyboardListener() {
		mKeyboard.setOnKeyboardListener(new OnNumberKeyboardListener() {

			@Override
			public void onKeyboardClick(String key) {
				if (key.equals(KeyboardNumberWhiteFragment.OK)) {
					if (TextUtils.isEmpty(mEditPassword.getText().toString().trim())) {
						MyToast.show(R.string.wifi_password_input);
						return;
					}
					if (mAction.equals(DISALARM)) {
						String safePassword = DPFunction.getSafePassword(false);
						String holdingPassword = DPFunction.getSafePassword(true);
						if (mEditPassword.getText().toString().equals(safePassword)) {
							if (DPFunction.getAlarming()) {
								DPFunction.disalarm(false);
							} else {
								DPFunction.changeSafeMode(ConstConf.UNSAFE_MODE, false);
							}
							Intent intent = new Intent(mContext, HomeSecurityModeDelayDialog.class);
							intent.putExtra(ConstConf.SAFE_MODE, ConstConf.UNSAFE_MODE);
							startActivity(intent);
							finish();
						} else if (mEditPassword.getText().toString().equals(holdingPassword)) {
							if (DPFunction.getAlarming()) {
								DPFunction.disalarm(true);
							} else {
								TalkManager.toManageAlarm(
										CommonUT.formatTime(System.currentTimeMillis()), 99, 19, 0);
								DPFunction.changeSafeMode(ConstConf.UNSAFE_MODE, false);
							}
							Intent intent = new Intent(mContext, HomeSecurityModeDelayDialog.class);
							intent.putExtra(ConstConf.SAFE_MODE, ConstConf.UNSAFE_MODE);
							startActivity(intent);
							finish();
						} else {
							MyToast.show(R.string.password_error);
						}
					} else if (mAction.equals(SYSTEM_SETTING)) {
						String password = DPFunction.getPsdProjectSetting();
						if (mEditPassword.getText().toString().trim().equals(password)) {
							startActivity(new Intent(mContext, SystemSettingActivity.class));
							finish();
						} else {
							MyToast.show(R.string.password_error);
						}
					}
				} else if (mEditPassword.hasFocus()) {
					editText(mEditPassword, key);
				}
			}
		});
	}

	private void editText(EditText editText, String key) {
		mEditTool.setEditText(editText);
		if (key.equals(KeyboardNumberWhiteFragment.DELETE)) {
			mEditTool.deleteText();
		} else {
			if (editText.getText().toString().length() < 6) {
				mEditTool.appendTextTo(key);
			}
		}
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

	@Override
	protected void onDestroy() {
		getSupportFragmentManager().beginTransaction().remove(mKeyboard).commitAllowingStateLoss();
		super.onDestroy();
	}
}
