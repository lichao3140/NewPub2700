package com.dpower.pub.dp2700.activity;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.dialog.SmartServerIPSettingDialog;
import com.dpower.pub.dp2700.activity.dialog.SystemResetDialog;
import com.dpower.pub.dp2700.activity.dialog.WebCameraSettingDialog;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ProjectConfigure;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 系统时间设置界面
 */
public class SystemSettingActivity extends BaseActivity implements OnClickListener {

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ProjectConfigure.project == 1) {
			setContentView(R.layout.activity_system_setting1);
		} else if (ProjectConfigure.project == 2) {
			setContentView(R.layout.activity_system_setting2);
		} else {
			setContentView(R.layout.activity_system_setting);
		}
		mContext = this;
		findViewById(R.id.btn_room_num_set).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_defence_area_set).setOnClickListener(this);
		findViewById(R.id.btn_system_reset).setOnClickListener(this);
		findViewById(R.id.btn_project_password_set).setOnClickListener(this);
		findViewById(R.id.btn_unlock_set).setOnClickListener(this);
		findViewById(R.id.btn_unlock_psd).setOnClickListener(this);
		findViewById(R.id.btn_villa_extension_set).setOnClickListener(this);
		findViewById(R.id.btn_project_webcamera).setOnClickListener(this);
		findViewById(R.id.btn_project_smart_server).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		findViewById(R.id.root_view).setBackground(SPreferences.getInstance().getWallpaper());
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_room_num_set:
			Intent roomCodeSetting = new Intent(mContext, RoomCodeSettingActivity.class);
			roomCodeSetting.setAction(RoomCodeSettingActivity.ROOM_CODE_SETTING);
			startActivity(roomCodeSetting);
			break;
		case R.id.btn_defence_area_set:
			startActivity(new Intent(mContext, DefenceAreaSettingActivity.class));
			break;
		case R.id.btn_system_reset:
			startActivity(new Intent(mContext, SystemResetDialog.class));
			break;
		case R.id.btn_unlock_set:
			startActivity(new Intent(mContext, UnlockDelaySettingActivity.class));
			break;
		case R.id.btn_unlock_psd:
			startActivity(new Intent(mContext, UnlockPasswordSettingActivity.class));
			break;
		case R.id.btn_villa_extension_set:
			Intent villaSet = new Intent(mContext, RoomCodeSettingActivity.class);
			villaSet.setAction(RoomCodeSettingActivity.VILLA_SETTING);
			startActivity(villaSet);
			break;
		case R.id.btn_project_password_set:
			startActivity(new Intent(mContext, ProjectPasswordSettingActivity.class));
			break;
		case R.id.btn_project_webcamera:
			startActivity(new Intent(mContext, WebCameraSettingDialog.class));
			break;
		case R.id.btn_project_smart_server:
			startActivity(new Intent(mContext, SmartServerIPSettingDialog.class));
			break;
		default:
			break;
		}
	}
}
