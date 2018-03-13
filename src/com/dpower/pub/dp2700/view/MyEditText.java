package com.dpower.pub.dp2700.view;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MyEditText extends EditText {

	public MyEditText(Context context) {
		super(context);
		setLongClick();
		setShowSoftInputOnFocus();
	}

	public MyEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLongClick();
		setShowSoftInputOnFocus();
	}

	/**
	 * 去掉长按编辑功能
	 */
	private void setLongClick() {

		this.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {

			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {

				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				return false;
			}
		});
		this.setLongClickable(false);
	}

	/**
	 * 禁止获取焦点时弹出系统键盘
	 */
	private void setShowSoftInputOnFocus() {
		Class<EditText> cls = EditText.class;
		Method setShowSoftInputOnFocus;
		try {
			setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
			setShowSoftInputOnFocus.setAccessible(true);
			setShowSoftInputOnFocus.invoke(this, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
