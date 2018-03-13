package com.dpower.pub.dp2700.broadcastreceiver;

import com.dpower.function.DPFunction;
import com.dpower.util.MyLog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmFinishCallBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (DPFunction.ACTION_ALARMING.equals(intent.getAction())) {
			MyLog.print(MyLog.ERROR, "安防相关广播");
			int i = intent.getIntExtra(DPFunction.ALARM_ID, 0);
			if (i == DPFunction.DISALARMING) {

			} else {
				MyLog.print(MyLog.ERROR, "发生报警关闭其他界面");
				((Activity) context).finish();
			}
		}
	}
}
