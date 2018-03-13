package com.dpower.pub.dp2700.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.dpower.pub.dp2700.broadcastreceiver.RebootAlarmReceiver;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * 重启工具类
 */
public class RebootUT {
	private static final String TAG = "RebootUT";

	public static int REBOOT_TIME = 8;//重启时间
	private static RebootUT mRebootUT;
	private Context mContext;
	private AlarmManager mAlarmManager;

	private RebootUT(Context context) {
		mContext = context;
	}

	public static RebootUT getInstance(Context context) {
		if (mRebootUT == null) {
			mRebootUT = new RebootUT(context);
		}
		return mRebootUT;
	}

	/**
	 * 设置设备重启时间
	 * @param hour
	 */
	public void rebootAtTime(int hour) {
		if (ProjectConfigure.project == 1) {
			hour = 8;
			REBOOT_TIME = hour;
		}
		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext, RebootAlarmReceiver.class);
		intent.putExtra("reboot", "to reboot");
		PendingIntent pi;
		pi = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		long time = getTime(hour);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		MyLog.print(TAG,
				"重启闹钟设置：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(cal.getTime()));
		mAlarmManager.cancel(pi);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi); // 设置闹钟，当前时间就唤醒
	}

	private long getTime(int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		if (c.get(Calendar.HOUR_OF_DAY) >= hour) {
			c.add(Calendar.DAY_OF_MONTH, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTimeInMillis();
	}
}
