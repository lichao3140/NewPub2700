package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseFragmentActivity;
import com.dpower.pub.dp2700.fragment.KeyboardWhiteFragment;
import com.dpower.pub.dp2700.fragment.KeyboardWhiteFragment.OnKeyboardListener;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.view.MyEditText;
import com.dpower.util.CommonUT;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * 网络摄像头设置窗口
 */
public class WebCameraSettingDialog extends BaseFragmentActivity implements OnClickListener {

	private KeyboardWhiteFragment mKeyboard;
	private FrameLayout mFrameKeyboard;
	private EditTextTool mEditTool;
	private MyEditText mEditIpAddress;
	private MyEditText mEditUsername;
	private MyEditText mEditPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_camera_setting);
		init();
	}

	private void init() {
		findViewById(R.id.root_view).setOnClickListener(this);
		LinearLayout contentWindow = (LinearLayout) findViewById(R.id.content_window);
		contentWindow.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				clearFocus();
				return true;
			}
		});
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		mEditIpAddress = (MyEditText) findViewById(R.id.et_ip_address);
		mEditUsername = (MyEditText) findViewById(R.id.et_username);
		mEditPassword = (MyEditText) findViewById(R.id.et_password);
		mFrameKeyboard = (FrameLayout) findViewById(R.id.frame_keyboard);
		SPreferences sp = SPreferences.getInstance();
		mEditIpAddress.setText(sp.getWebCameraIP());
		mEditUsername.setText(sp.getWebCameraUserName());
		mEditPassword.setText(sp.getWebCameraPassword());
		mEditIpAddress.setSelection(mEditIpAddress.getText().length());
		mEditUsername.setSelection(mEditUsername.getText().length());
		mEditPassword.setSelection(mEditPassword.getText().length());
		mEditTool = EditTextTool.getInstance();
		mKeyboard = new KeyboardWhiteFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.frame_keyboard, mKeyboard)
				.commitAllowingStateLoss();
		setKeyboardListener();
		mEditIpAddress.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mFrameKeyboard.setVisibility(View.VISIBLE);
				} else {
					mFrameKeyboard.setVisibility(View.GONE);
				}

			}
		});
		mEditUsername.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mFrameKeyboard.setVisibility(View.VISIBLE);
				} else {
					mFrameKeyboard.setVisibility(View.GONE);
				}

			}
		});
		mEditPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mFrameKeyboard.setVisibility(View.VISIBLE);
				} else {
					mFrameKeyboard.setVisibility(View.GONE);
				}

			}
		});
	}

	private void clearFocus() {
		mEditIpAddress.clearFocus();
		mEditUsername.clearFocus();
		mEditPassword.clearFocus();
	}

	private void setKeyboardListener() {
		mKeyboard.setOnKeyboardListener(new OnKeyboardListener() {

			@Override
			public void onKeyboardClick(String key) {
				if (key.equals(KeyboardWhiteFragment.OK)) {
					clearFocus();
					return;
				}
				if (mEditUsername.hasFocus()) {
					editText(mEditUsername, key);
				} else if (mEditPassword.hasFocus()) {
					editText(mEditPassword, key);
				} else if (mEditIpAddress.hasFocus()) {
					if (key.equals(KeyboardWhiteFragment.DELETE) || key.equals(".")
							|| Character.isDigit(key.charAt(0))) {
						editText(mEditIpAddress, key);
					}
				}
			}
		});
	}

	private void editText(MyEditText editText, String key) {
		mEditTool.setEditText(editText);
		if (key.equals(KeyboardWhiteFragment.DELETE)) {
			mEditTool.deleteText();
		} else {
			mEditTool.appendTextTo(key);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_confirm:
			String ip = mEditIpAddress.getText().toString().trim();
			String username = mEditUsername.getText().toString().trim();
			String password = mEditPassword.getText().toString().trim();
			if (TextUtils.isEmpty(ip)) {
				MyToast.show(R.string.ip_address_input);
				return;
			}
			if (TextUtils.isEmpty(username)) {
				MyToast.show(R.string.username_input);
				return;
			}
			if (TextUtils.isEmpty(password)) {
				MyToast.show(R.string.password_input);
				return;
			}
			if (!CommonUT.isIp(ip)) {
				MyToast.show(R.string.ip_address_error);
				return;
			}
			SPreferences sp = SPreferences.getInstance();
			sp.saveWebCameraIP(ip);
			sp.saveWebCameraUserName(username);
			sp.saveWebCameraPassword(password);
			MyToast.show(R.string.set_success);
			finish();
			break;
		case R.id.root_view:
		case R.id.btn_cancel:
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
