package com.dpower.pub.dp2700.activity;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.QRCodeUtils;
import com.google.zxing.WriterException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 绑定设备
 */
public class BindDeviceActivity extends BaseFragmentActivity implements OnClickListener {

	private ImageView mImageQRCode;
	private TextView mTextQRCode;
	private CloudLoginChangeReceiver mCloudLoginReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cloud_intercom);
		mImageQRCode = (ImageView) findViewById(R.id.image_qr_code);
		mTextQRCode = (TextView) findViewById(R.id.tv_qrcode);
		findViewById(R.id.btn_back).setOnClickListener(this);
		String QRString = DPFunction.getAccount();
		if (QRString == null) {
			QRString = "disabled";
		}
		updateLoginStatus();
		showQRCode(QRString);
		IntentFilter filter = new IntentFilter(DPFunction.ACTION_CLOUD_LOGIN_CHANGED);
		mCloudLoginReceiver = new CloudLoginChangeReceiver();
		App.getInstance().getContext().registerReceiver(mCloudLoginReceiver, filter);
	}

	private void updateLoginStatus() {
		String msg;
		String account = DPFunction.getAccount();
		if (account == null) {
			account = "disabled";
		}
		msg = getString(R.string.cloud_intercom_account) + account + "\n" + getString(R.string.account_is_online)
				+ (DPFunction.isOnline() ? "Yes" : "No");
		mTextQRCode.setText(msg);
	}

	@Override
	protected void onResume() {
		findViewById(R.id.root_view).setBackground(SPreferences.getInstance().getWallpaper());
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mCloudLoginReceiver != null) {
			App.getInstance().getContext().unregisterReceiver(mCloudLoginReceiver);
			mCloudLoginReceiver = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		default:
			break;
		}
	}

	private void showQRCode(String QRString) {
		try {
			Bitmap bm = QRCodeUtils.createQRCode(QRString, getResources().getDimensionPixelSize(R.dimen.x140),
					getResources().getDimensionPixelSize(R.dimen.x140), 1);
			mImageQRCode.setImageBitmap(bm);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	private class CloudLoginChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateLoginStatus();
		}
	}
}
