package com.dpower.pub.dp2700.fragment;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BindDeviceActivity;
import com.dpower.pub.dp2700.activity.CallForwardingActivity;
import com.dpower.pub.dp2700.activity.WifiActivity;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.util.ProjectConfigure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * 云对讲
 */
public class CloudIntercomFragment extends Fragment implements OnClickListener {

	private View mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (ProjectConfigure.project == 1) {
			mRootView = inflater.inflate(R.layout.fragment_cloud_intercom1, container, false);
		} else if (ProjectConfigure.project == 2) {
			mRootView = inflater.inflate(R.layout.fragment_cloud_intercom2, container, false);
		} else {
			mRootView = inflater.inflate(R.layout.fragment_cloud_intercom, container, false);
		}
		mRootView.findViewById(R.id.btn_wifi_set).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_bind_device).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_call_forwarding).setOnClickListener(this);
		return mRootView;

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_wifi_set:
			//startActivity(new Intent(getActivity(), WifiActivity.class));
			MyToast.shortToast("该设备不支持该功能");
			break;
		case R.id.btn_bind_device:
			//startActivity(new Intent(getActivity(), BindDeviceActivity.class));
			MyToast.shortToast("该设备不支持该功能");
			break;
		case R.id.btn_call_forwarding:
			//startActivity(new Intent(getActivity(), CallForwardingActivity.class));
			MyToast.shortToast("该设备不支持该功能");
			break;
		default:
			break;
		}
	}
}
