package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import com.dpower.domain.AddrInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.InstallPackage;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.view.MyEditText;
import com.dpower.util.ConstConf;
import com.dpower.util.DPDBHelper;
import com.dpower.util.MyLog;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 房号设置
 */
public class RoomCodeSettingActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "RoomNumSetActivity";

	public static final String ROOM_CODE_SETTING = "roomCodeSetting";
	public static final String VILLA_SETTING = "villaSetting";
	/** 外置SD卡升级包 */
	private final String APK_EXTSD = "mnt/extsd/Pub2700.apk";
	private final int ROOM_CODE_NOT_EXIST = 100;
	private final int ROOM_CODE_EXIST = 101;
	private final int SET_SUCCESS = 102;
	private final int SET_FAILED = 103;
	private EditTextTool mEditTool;
	private MyEditText mEditArea; // 区
	private MyEditText mEditBuilding; // 栋
	private MyEditText mEditUnit; // 单元
	private MyEditText mEditRoom; // 室
	private MyEditText mEditExtension; // 分机
	private PopupWindow mPopupWindow;
	private String mAction;
	private boolean mIsUpdateNetCfgOK = false;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ROOM_CODE_NOT_EXIST:
				mPopupWindow.dismiss();
				MyToast.show(R.string.room_code_not_exist);
				break;
			case ROOM_CODE_EXIST:
				mPopupWindow.dismiss();
				MyToast.show(R.string.roomcode_exist);
				break;
			case SET_SUCCESS:
				DPDBHelper.clearAccount();
				mPopupWindow.dismiss();
				MyToast.show(R.string.set_roomcode_success);
				finish();
				break;
			case SET_FAILED:
				mPopupWindow.dismiss();
				MyToast.show(R.string.set_roomcode_fail);
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_num_set);
		init();
	}

	private void init() {
		mAction = getIntent().getAction();
		if (mAction == null) {
			finish();
			return;
		}
		mEditTool = EditTextTool.getInstance();
		mEditArea = (MyEditText) findViewById(R.id.et_area);
		mEditBuilding = (MyEditText) findViewById(R.id.et_building);
		mEditUnit = (MyEditText) findViewById(R.id.et_unit);
		mEditRoom = (MyEditText) findViewById(R.id.et_room);
		mEditExtension = (MyEditText) findViewById(R.id.et_extension);
		findViewById(R.id.btn_back).setOnClickListener(this);
		if (mAction.equals(VILLA_SETTING)) {
			findViewById(R.id.btn_sd_update).setVisibility(View.GONE);
			findViewById(R.id.btn_netcfg_update).setVisibility(View.GONE);
			((TextView) findViewById(R.id.tv_title)).setText(R.string.text_villa_extension_set);
		} else if (mAction.equals(ROOM_CODE_SETTING)) {
			findViewById(R.id.btn_sd_update).setOnClickListener(this);
			findViewById(R.id.btn_netcfg_update).setOnClickListener(this);
			String roomNum = DPFunction.getRoomCode();
			MyLog.print(TAG, "本机房号: " + DPFunction.getRoomCode());
			String areaStr = roomNum.substring(1, 3);
			String mewsStr = roomNum.substring(3, 5);
			String unitStr = roomNum.substring(5, 7);
			String roomStr = roomNum.substring(7, 11);
			String extensionStr = roomNum.substring(11, 13);
			MyLog.print(TAG,
					"本机房号: " + areaStr + "区" + mewsStr + "栋" + unitStr + "单元" + roomStr + "室" + extensionStr + "分机");
			mEditArea.setText(areaStr);
			mEditBuilding.setText(mewsStr);
			mEditUnit.setText(unitStr);
			mEditRoom.setText(roomStr);
			mEditExtension.setText(extensionStr);
			mEditArea.setSelection(mEditArea.getText().toString().length());
		}
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		setKeyboardClickListener(R.id.btn_num_1, "1");
		setKeyboardClickListener(R.id.btn_num_2, "2");
		setKeyboardClickListener(R.id.btn_num_3, "3");
		setKeyboardClickListener(R.id.btn_num_4, "4");
		setKeyboardClickListener(R.id.btn_num_5, "5");
		setKeyboardClickListener(R.id.btn_num_6, "6");
		setKeyboardClickListener(R.id.btn_num_7, "7");
		setKeyboardClickListener(R.id.btn_num_8, "8");
		setKeyboardClickListener(R.id.btn_num_9, "9");
		setKeyboardClickListener(R.id.btn_num_0, "0");
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
			onKeyboardClick(v.getTag().toString());
		}
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_confirm:
			String roomNum = getRoomCode();
			MyLog.print(TAG, "房号是: " + roomNum);
			if (roomNum != null && roomNum.length() == DPFunction.getRoomCode().length()) {
				if (mAction.equals(VILLA_SETTING)) {
					checkLittleGateCodeSet(roomNum);
				} else {
					setRoomCode(roomNum);
				}
			}
			break;
		case R.id.btn_sd_update:
			updataAPK();
			break;
		case R.id.btn_netcfg_update:
			updateNetCfg();
			break;
		default:
			break;
		}
	}

	private void onKeyboardClick(String key) {
		if (mEditArea.hasFocus()) {
			if (key.equals("-1") || mEditArea.getText().toString().length() < 2) {
				editText(mEditArea, key);
			}
		} else if (mEditBuilding.hasFocus()) {
			if (key.equals("-1") || mEditBuilding.getText().toString().length() < 2) {
				editText(mEditBuilding, key);
			}
		} else if (mEditUnit.hasFocus()) {
			if (key.equals("-1") || mEditUnit.getText().toString().length() < 2) {
				editText(mEditUnit, key);
			}
		} else if (mEditRoom.hasFocus()) {
			if (key.equals("-1") || mEditRoom.getText().toString().length() < 4) {
				editText(mEditRoom, key);
			}
		} else if (mEditExtension.hasFocus()) {
			if (key.equals("-1") || mEditExtension.getText().toString().length() < 2) {
				editText(mEditExtension, key);
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

	/**
	 * 读取设置的房号
	 */
	private String getRoomCode() {
		if (TextUtils.isEmpty(mEditArea.getText().toString())) {
			MyToast.show(R.string.area_input);
			return null;
		} else if (mEditArea.getText().toString().length() < 2) {
			MyToast.show(R.string.area_input_error);
			return null;
		}
		if (TextUtils.isEmpty(mEditBuilding.getText().toString())) {
			MyToast.show(R.string.building_input);
			return null;
		} else if (mEditBuilding.getText().toString().length() < 2) {
			MyToast.show(R.string.building_input_error);
			return null;
		}
		if (TextUtils.isEmpty(mEditUnit.getText().toString())) {
			MyToast.show(R.string.unit_input);
			return null;
		} else if (mEditUnit.getText().toString().length() < 2) {
			MyToast.show(R.string.unit_input_error);
			return null;
		}
		if (TextUtils.isEmpty(mEditRoom.getText().toString())) {
			MyToast.show(R.string.room_input);
			return null;
		} else if (mEditRoom.getText().toString().length() < 4) {
			MyToast.show(R.string.room_input_error);
			return null;
		}
		if (TextUtils.isEmpty(mEditExtension.getText().toString())) {
			MyToast.show(R.string.extension_input);
			return null;
		} else if (mEditExtension.getText().toString().length() < 2) {
			MyToast.show(R.string.extension_input_error);
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		if (mAction.equals(VILLA_SETTING)) {
			buffer.append("3");
		} else {
			buffer.append("1");
		}
		buffer.append(mEditArea.getText().toString());
		buffer.append(mEditBuilding.getText().toString());
		buffer.append(mEditUnit.getText().toString());
		buffer.append(mEditRoom.getText().toString());
		buffer.append(mEditExtension.getText().toString());
		return buffer.toString();
	}

	/**
	 * 当设置小门口房号点击确定时，调用该方法进行检查
	 * 
	 * @param doorCode
	 *            格式：3010201010101
	 * @return 成功返回true
	 */
	private void checkLittleGateCodeSet(String doorCode) {
		int result = DPFunction.toDoorSetNum(doorCode);
		if (result == 0) {
			MyToast.show(R.string.little_door_roomcode_alter_success);
			finish();
		} else if (result == -1) {
			MyToast.show(R.string.room_code_not_exist);
		} else {
			MyToast.show(R.string.little_door_roomcode_alter_error);
		}
	}

	/**
	 * 设置房号
	 * 
	 * @param roomNum
	 */
	private void setRoomCode(final String roomNum) {
		LayoutInflater inflater = getLayoutInflater();
		View popupView = inflater.inflate(R.layout.popup_progress_bar, null);
		mPopupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.showAtLocation(findViewById(R.id.root_view), Gravity.CENTER, 0, 0);
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean isSetLegal = false;
				AddrInfo addrInfo = DPFunction.getAddrInfo(roomNum);
				if (addrInfo != null) {
					String ip = addrInfo.getIp();
					isSetLegal = !ping(ip);
				} else {
					mHandler.sendEmptyMessageDelayed(ROOM_CODE_NOT_EXIST, 1000);
					return;
				}
				if (isSetLegal) {
					int result = DPFunction.changeRoomCode(roomNum);
					if (result == 0) {
						mHandler.sendEmptyMessage(SET_SUCCESS);
					} else {
						mHandler.sendEmptyMessage(SET_FAILED);
					}

				} else {
					mHandler.sendEmptyMessageDelayed(ROOM_CODE_EXIST, 1000);
				}
			}
		}).start();
	}

	/**
	 * @param ip
	 * @return 成功-true/失败-false
	 */
	private boolean ping(String ip) {
		boolean result = false;
		try {
			boolean status = InetAddress.getByName(ip).isReachable(5000);
			if (status) { // 访问成功
				MyLog.print(TAG, "访问成功，已经存在该IP在网络中");
				result = true;
			} else {
				MyLog.print(TAG, "访问不成功，不存在该IP在网络中");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void updataAPK() {
		File file = new File(APK_EXTSD);
		if (!file.exists()) {
			MyToast.show(R.string.no_update_file);
			return;
		}
		// 检查版本号
		PackageManager pm = getPackageManager();
		PackageInfo packageInfo = pm.getPackageArchiveInfo(APK_EXTSD, PackageManager.GET_ACTIVITIES);
		int versionCode = packageInfo.versionCode;
		try {
			PackageInfo apkInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			if (versionCode <= apkInfo.versionCode) {
				MyToast.show(R.string.cannot_update_to_lower);
				return;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return;
		}
		if (null != InstallPackage.getPackageName(this, "com.dpower.updateapk")) {
			MyToast.show(R.string.start_update_apk);
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(new ComponentName("com.dpower.updateapk", "com.dpower.updateapk.ApkUpdateActivity"));
			startActivity(intent);
		}
	}

	private void updateNetCfg() {
		File file = new File("mnt/extsd/netcfg.dat");
		if (!file.exists()) {
			MyToast.show(R.string.no_update_file);
			return;
		}
		LayoutInflater inflater = getLayoutInflater();
		View popupView = inflater.inflate(R.layout.popup_progress_bar, null);
		TextView title = (TextView) popupView.findViewById(R.id.tv_title);
		title.setText(R.string.update_netcfg_keep_power);
		final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				true);
		popupWindow.setTouchable(false);
		popupWindow.setFocusable(true);
		popupWindow.showAtLocation(findViewById(R.id.root_view), Gravity.CENTER, 0, 0);

		try {
			FileOperate fileOperate = new FileOperate();
			fileOperate.from("mnt/extsd/netcfg.dat").to(ConstConf.NET_CFG_PATH);
			mIsUpdateNetCfgOK = true;
		} catch (Exception e) {
			mIsUpdateNetCfgOK = false;
			e.printStackTrace();
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mIsUpdateNetCfgOK) {
					LayoutInflater inflater = getLayoutInflater();
					View popupView = inflater.inflate(R.layout.popup_progress_bar, null);
					TextView title = (TextView) popupView.findViewById(R.id.tv_title);
					title.setText(R.string.update_net_cfg_success);
					PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, true);
					popupWindow.setTouchable(true);
					popupWindow.setFocusable(true);
					popupWindow.showAtLocation(findViewById(R.id.root_view), Gravity.CENTER, 0, 0);
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							String cmd = "su -c reboot";
							try {
								Runtime.getRuntime().exec(cmd);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}, 6 * 1000);
				} else {
					popupWindow.dismiss();
					MyToast.show(R.string.update_netcfg_fail);
				}
			}
		}, 4 * 1000);
	}
}
