package com.dpower.pub.dp2700.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.dpower.pub.dp2700.R;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * 全键盘
 */
public class KeyboardWhiteFragment extends BaseFragment implements OnClickListener {

	public static final String OK = "ok";
	public static final String DELETE = "-1";
	private View mView;
	private Boolean mIsUpper = false; // 是大写
	private List<Button> mButtons = new ArrayList<Button>();
	private OnKeyboardListener mKeyListener;
	private Handler mHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_keyboard_white, container, false);
		// 数字键
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
		setClickListener(R.id.btn_dot, ".");
		// 字母键
		setClickListener(R.id.btn_lowercase_a, "a");
		setClickListener(R.id.btn_lowercase_b, "b");
		setClickListener(R.id.btn_lowercase_c, "c");
		setClickListener(R.id.btn_lowercase_d, "d");
		setClickListener(R.id.btn_lowercase_e, "e");
		setClickListener(R.id.btn_lowercase_f, "f");
		setClickListener(R.id.btn_lowercase_g, "g");
		setClickListener(R.id.btn_lowercase_h, "h");
		setClickListener(R.id.btn_lowercase_i, "i");
		setClickListener(R.id.btn_lowercase_j, "j");
		// 11-20
		setClickListener(R.id.btn_lowercase_k, "k");
		setClickListener(R.id.btn_lowercase_l, "l");
		setClickListener(R.id.btn_lowercase_m, "m");
		setClickListener(R.id.btn_lowercase_n, "n");
		setClickListener(R.id.btn_lowercase_o, "o");
		setClickListener(R.id.btn_lowercase_p, "p");
		setClickListener(R.id.btn_lowercase_q, "q");
		setClickListener(R.id.btn_lowercase_r, "r");
		setClickListener(R.id.btn_lowercase_s, "s");
		setClickListener(R.id.btn_lowercase_t, "t");
		// 21-26
		setClickListener(R.id.btn_lowercase_u, "u");
		setClickListener(R.id.btn_lowercase_v, "v");
		setClickListener(R.id.btn_lowercase_w, "w");
		setClickListener(R.id.btn_lowercase_x, "x");
		setClickListener(R.id.btn_lowercase_y, "y");
		setClickListener(R.id.btn_lowercase_z, "z");
		setClickListener(R.id.btn_submit, OK);
		setClickListener(R.id.btn_language_switch, "");
		setClickListener(R.id.btn_letter_switch, "");
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

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_letter_switch) {
			mIsUpper = !mIsUpper;
			changeKey();
		} else if (v.getId() == R.id.btn_language_switch) {

		} else if (mKeyListener != null && v.getTag() != null) {
			mKeyListener.onKeyboardClick(v.getTag().toString());
		}
	}

	private void changeKey() {
		if (mIsUpper) {
			for (Button button : mButtons) {
				button.setText(button.getTag().toString().toUpperCase(Locale.US));
				button.setTag(button.getText().toString());
			}
		} else {
			for (Button button : mButtons) {
				button.setText(button.getTag().toString().toLowerCase(Locale.US));
				button.setTag(button.getText().toString());
			}
		}
	}

	private void setClickListener(int resId, String tag) {
		View button;
		button = mView.findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
		if (tag.length() == 1 && isWord(tag)) {
			mButtons.add((Button) button);
		}
	}

	private boolean isWord(String str) {
		String wordstr = "abcdefghijklmnopqrstuvwxyz";
		if (wordstr.indexOf(str.toLowerCase(Locale.US)) > -1) {
			return true;
		}
		return false;
	}

	public interface OnKeyboardListener {
		public void onKeyboardClick(String key);
	}

	public void setOnKeyboardListener(OnKeyboardListener keyListener) {
		mKeyListener = keyListener;
	}
}
