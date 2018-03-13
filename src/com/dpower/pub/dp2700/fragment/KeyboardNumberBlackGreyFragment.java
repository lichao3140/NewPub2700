package com.dpower.pub.dp2700.fragment;

import com.dpower.pub.dp2700.R;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class KeyboardNumberBlackGreyFragment extends BaseFragment implements OnClickListener {

	public static final String OK = "ok";
	public static final String DELETE = "-1";
	private View mView;
	private OnNumberKeyboardListener mKeyListener;
	private Handler mHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_keyboard_number_black_grey, container, false);
		setClickListener(R.id.btn_num1, "1");
		setClickListener(R.id.btn_num2, "2");
		setClickListener(R.id.btn_num3, "3");
		setClickListener(R.id.btn_num4, "4");
		setClickListener(R.id.btn_num5, "5");
		setClickListener(R.id.btn_num6, "6");
		setClickListener(R.id.btn_num7, "7");
		setClickListener(R.id.btn_num8, "8");
		setClickListener(R.id.btn_num9, "9");
		setClickListener(R.id.btn_num0, "0");
		setClickListener(R.id.btn_submit, OK);
		mHandler = new Handler();
		mView.findViewById(R.id.btn_delete).setOnClickListener(this);
		mView.findViewById(R.id.btn_delete).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mKeyListener.onKeyboardClick(DELETE);
							mHandler.postDelayed(this, 150);
						}
					});
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mHandler.removeCallbacksAndMessages(null);
				}
				return false;
			}
		});
		return mView;
	}

	private void setClickListener(int resId, String tag) {
		View button;
		button = mView.findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (mKeyListener != null && v.getTag() != null) {
			mKeyListener.onKeyboardClick(v.getTag().toString());
		}
	}

	public interface OnNumberKeyboardListener {
		public void onKeyboardClick(String key);
	}

	public void setOnKeyboardListener(OnNumberKeyboardListener keyListener) {
		mKeyListener = keyListener;
	}
}
