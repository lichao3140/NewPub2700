package com.dpower.pub.dp2700.tools;

import java.util.Locale;

import org.example.language.LanguageManager;

import com.dpower.util.MyLog;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Language {
	private static final String TAG = "Language";

	private static LanguageManager mLanguageManager;

	public static void updateLanguage(Context context, int flag) {
		Resources resources = context.getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		mLanguageManager = new LanguageManager(context);

		if (flag == 1) {
			config.locale = Locale.SIMPLIFIED_CHINESE;
			mLanguageManager.setLanguage(LanguageManager.CH_EASY);
			MyLog.print(TAG, "保存中文语言");
		} else {
			config.locale = Locale.ENGLISH;
			mLanguageManager.setLanguage(LanguageManager.English_UnitedStates);
			MyLog.print(TAG, "保存英文语言");
		}
		resources.updateConfiguration(config, dm);
	}

	public static void updateLanguage(int flag) {
		IActivityManager iActivityManager = ActivityManagerNative.getDefault();
		Configuration config = iActivityManager.getConfiguration();
		if (flag == 1) {
			config.locale = Locale.SIMPLIFIED_CHINESE;
			MyLog.print(TAG, "保存中文语言");
		} else {
			config.locale = Locale.ENGLISH;
			MyLog.print(TAG, "保存英文语言");
		}
		iActivityManager.updateConfiguration(config);
	}
}
