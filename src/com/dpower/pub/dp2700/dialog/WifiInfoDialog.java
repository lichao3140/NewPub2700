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
 * 已连接的热点的信息
 * 
 * @author misakayaho
 */
public class WifiInfoDialog extends BaseDialogFragment implements View.OnClickListener {
	private static final String TAG = "WifiInfoDialog";

	public String ssid;
	public String status;
	public String speed;
	public String ip;
	public String subnetMask;
	public String gateway;
	public String dns;
	public int netId;
	public int security;
	public int ipType;
	private View mView;
	private TextView mTextSSID;
	private TextView mTextStatus;
	private TextView mTextSpeed;
	private TextView mTextIp;
	private TextView mTextSubnetMask;
	private TextView mTextGateway;
	private TextView mTextDns;
	private Button mButtonForget;
	private Button mButtonModify;
	private Button mButtonDisconnect;
	private OnInfoDialogClickListener mListener;

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
		mTextStatus = (TextView) mView.findViewById(R.id.tv_status);
		mTextSpeed = (TextView) mView.findViewById(R.id.tv_speed);
		mTextIp = (TextView) mView.findViewById(R.id.tv_ip);
		mTextSubnetMask = (TextView) mView.findViewById(R.id.tv_subnet_mask);
		mTextGateway = (TextView) mView.findViewById(R.id.tv_gateway);
		mTextDns = (TextView) mView.findViewById(R.id.tv_dns);
		mButtonForget = (Button) mView.findViewById(R.id.btn_forget);
		mButtonModify = (Button) mView.findViewById(R.id.btn_modify);
		mButtonModify.setVisibility(View.VISIBLE);
		mButtonDisconnect = (Button) mView.findViewById(R.id.btn_disconnect);
		LinearLayout showInfo = (LinearLayout) mView.findViewById(R.id.show_info);
		showInfo.setVisibility(View.VISIBLE);

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
			mTextStatus.setText(status);
			mTextSpeed.setText(speed);
			mTextIp.setText(ip);
			mTextSubnetMask.setText(subnetMask);
			mTextGateway.setText(gateway);
			mTextDns.setText(dns);
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
				mListener.onDisconnectNetwork();
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

	public interface OnInfoDialogClickListener {

		public void onRemoveNetwork(int netId);

		public void onModifyNetwork(String ssid, int security, int netId, int ipType);

		public void onDisconnectNetwork();
	}

	public void setInfoDialogClickListener(OnInfoDialogClickListener listener) {
		mListener = listener;
	}
}
