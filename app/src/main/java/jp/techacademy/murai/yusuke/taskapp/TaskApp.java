package jp.techacademy.murai.yusuke.taskapp;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by my99workmac on 2017/01/09.
 */

public class TaskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
