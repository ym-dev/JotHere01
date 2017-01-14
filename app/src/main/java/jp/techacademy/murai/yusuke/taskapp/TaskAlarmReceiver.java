package jp.techacademy.murai.yusuke.taskapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by my99workmac on 2017/01/15.
 */
public class TaskAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TaskApp", "onReceive");
    }
}