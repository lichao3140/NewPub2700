package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;

import com.dpower.domain.AddrInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.RoomInfoUT;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.view.MyEditText;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * 用户设置---开锁密码
 */
public class UnlockPasswordSettingActivity extends BaseFragmentActivity implements OnClickListener {

	private EditTextTool mEditTool;
	private MyEditText mEditPassword;
	private MyEditText mEditPasswordAgain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unlock_password);
		init();
	}

	private void init() {
		mEditTool = EditTextTool.getInstance();
		mEditPassword = (MyEditText) findViewById(R.id.et_password);
		mEditPasswordAgain = (MyEditText) findViewById(R.id.et_password_again);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		setKeyboardClickListener(R.id.bt_num_1, "1");
		setKeyboardClickListener(R.id.bt_num_2, "2");
		setKeyboardClickListener(R.id.bt_num_3, "3");
		setKeyboardClickListener(R.id.bt_num_4, "4");
		setKeyboardClickListener(R.id.bt_num_5, "5");
		setKeyboardClickListener(R.id.bt_num_6, "6");
		setKeyboardClickListener(R.id.bt_num_7, "7");
		setKeyboardClickListener(R.id.bt_num_8, "8");
		setKeyboardClickListener(R.id.bt_num_9, "9");
		setKeyboardClickListener(R.id.bt_num_0, "0");
		setKeyboardClickListener(R.id.btn_delete, "-1");
	}

	private void setKeyboardClickListener(int resId, String tag) {
		View button;
		button = findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		findViewById(R.id.root_view).setBackground(SPreferences.getInstance().getWallpaper());
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			String roomCode = DPFunction.getRoomCode();
			RoomInfoUT infoUT = new RoomInfoUT(roomCode);
			if (!infoUT.getExt().equals("01")) {
				MyToast.show(R.string.unlock_password_change_error);
				return;
			}
			onKeyboardClick(v.getTag().toString());
		}
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_confirm:
			String roomCode = DPFunction.getRoomCode();
			RoomInfoUT infoUT = new RoomInfoUT(roomCode);
			if (!infoUT.getExt().equals("01")) {
				MyToast.show(R.string.unlock_password_change_error);
				return;
			}
			checkInput();
			break;
		default:
			break;
		}
	}

	private void onKeyboardClick(String key) {
		if (mEditPassword.hasFocus()) {
			if (key.equals("-1") || mEditPassword.getText().toString().length() < 6) {
				editText(mEditPassword, key);
			}
		} else if (mEditPasswordAgain.hasFocus()) {
			if (key.equals("-1") || mEditPasswordAgain.getText().toString().length() < 6) {
				editText(mEditPasswordAgain, key);
			}
		}
	}

	private void editText(EditText editText, String key) {
		mEditTool.setEditText(editText);
		if (key.equals("-1")) {
			mEditTool.deleteText();
		} else {
			mEditTool.appendTextTo(key);
		}
	}

	private void checkInput() {
		if (mEditPassword.getText().toString().equals("")) {
			MyToast.show(R.string.password_input);
			return;
		}
		if (mEditPassword.getText().toString().length() != 6) {
			MyToast.show(R.string.input_six_password);
			return;
		}
		if (!mEditPassword.getText().toString().equals(mEditPasswordAgain.getText().toString())) {
			MyToast.show(R.string.password_not_same);
			return;
		}
		String password = mEditPassword.getText().toString();
		if (password.equals(new StringBuffer(password).reverse().toString())) {
			MyToast.show(R.string.unlock_password_input_error);
			return;
		}
		ArrayList<AddrInfo> monitorLists = DPFunction.getCellSeeList();
		String doorIpAddr = monitorLists.get(0).getIp();
		int result = DPFunction.toDoorModifyPassWord(doorIpAddr, mEditPassword.getText().toString());
		if (result == 0) {
			MyToast.show(R.string.change_succeeded);
			finish();
		} else {
			MyToast.show(R.string.change_failed);
		}
	}
}
