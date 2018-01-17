package com.jmm.www.calendar.alarmremind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jmm on 2016/4/21.
 */
public class AlarmServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, AlarmService.class);
        context.startService(serviceIntent);
    }
}
