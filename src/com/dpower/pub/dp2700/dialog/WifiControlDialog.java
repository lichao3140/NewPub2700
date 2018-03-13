package com.dpower.pub.dp2700.dialog;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.WifiActivity;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 控制已保存但未连接的热点
 * 
 * @author misakayaho
 */
public class WifiControlDialog extends BaseDialogFragment implements View.OnClickListener {
	private static final String TAG = "WifiControlDialog";

	public String ssid;
	public int netId;
	public int security;
	public int ipType;
	private View mView;
	private TextView mTextSSID;
	private Button mButtonForget;
	private Button mButtonModify;
	private Button mButtonDisconnect;
	private OnControlDialogClickListener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.dialog_wifi, container, false);
		removeTitleBar();
		setLayout(1.0f, 1.0f);
		mView.findViewById(R.id.root_view).setOnClickListener(this);
		LinearLayout contentWindow = (LinearLayout) mView.findViewById(R.id.content_window);
		contentWindow.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "contentWindow onTouch");
				return true;
			}
		});
		mTextSSID = (TextView) mView.findViewById(R.id.tv_ssid);
		mButtonForget = (Button) mView.findViewById(R.id.btn_forget);
		mButtonModify = (Button) mView.findViewById(R.id.btn_modify);
		mButtonModify.setVisibility(View.VISIBLE);
		mButtonDisconnect = (Button) mView.findViewById(R.id.btn_disconnect);
		mButtonDisconnect.setText(R.string.wifi_connect);

		mButtonForget.setOnClickListener(this);
		mButtonModify.setOnClickListener(this);
		mButtonDisconnect.setOnClickListener(this);

		return mView;
	}

	@Override
	public void onResume() {
		MyLog.print(MyLog.ERROR, TAG, "onResume");
		if (WifiActivity.wifiSettings != null) {
			WifiActivity.wifiSettings.pauseWifiHandleEvent();
		}
		if (ssid != null) {
			mTextSSID.setText(ssid);
		}
		super.onResume();
	}

	@Override
	public void dismiss() {
		MyLog.print(MyLog.ERROR, TAG, "dismiss");
		if (WifiActivity.wifiSettings != null) {
			WifiActivity.wifiSettings.resumeWifiHandleEvent();
		}
		super.dismiss();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_forget:
			if (mListener != null) {
				mListener.onRemoveNetwork(netId);
				dismiss();
			}
			break;
		case R.id.btn_modify:
			if (mListener != null) {
				mListener.onModifyNetwork(ssid, security, netId, ipType);
				dismiss();
			}
			break;
		case R.id.btn_disconnect:
			if (mListener != null) {
				mListener.onConnectNetwork(ssid);
				dismiss();
			}
			break;
		case R.id.root_view:
			dismiss();
			break;
		default:
			break;
		}
	}

	public interface OnControlDialogClickListener {

		public void onRemoveNetwork(int netId);

		public void onModifyNetwork(String ssid, int security, int netId, int ipType);

		public void onConnectNetwork(String ssid);
	}

	public void setControlDialogClickListener(OnControlDialogClickListener listener) {
		mListener = listener;
	}
}
