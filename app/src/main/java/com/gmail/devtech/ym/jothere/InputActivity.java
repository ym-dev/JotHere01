package com.gmail.devtech.ym.jothere;


import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

import android.telephony.PhoneStateListener;

public class InputActivity extends AppCompatActivity {

    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button mDateButton, mTimeButton;
    private EditText mTitleEdit, mContentEdit, mCategoryEdit;
    private Task mTask;
    private String strCellId;
    public TelephonyManager TM;
    private PhoneStateListener PSL;
    private int event;



    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTask();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mDateButton = (Button)findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button)findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        mTitleEdit = (EditText)findViewById(R.id.title_edit_text);
        mContentEdit = (EditText)findViewById(R.id.content_edit_text);
        mCategoryEdit = (EditText)findViewById(R.id.category_edit_text);


        //Cell IDの取得
        TM = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        PSL = new PhoneStateListener();
        int event = PSL.LISTEN_CELL_INFO | PSL.LISTEN_CELL_LOCATION;
        TM.listen(PSL, event);
        GsmCellLocation gsmCellLocation = (GsmCellLocation)TM.getCellLocation();

        if (gsmCellLocation != null) {
//        Integer cid = gsmCellLocation.getCid() & 0xffff;  //cell id
            Integer cid = gsmCellLocation.getCid();  //cell id
            strCellId = cid.toString();
            mCategoryEdit.setText(strCellId);       //Cell Idをcategory行に記載

            Log.d("jothere", "strCellID= " + strCellId);
        }else{
            String cidobterr = (String) getText(R.string.cidobterr);
            mCategoryEdit.setText(cidobterr);       //Cell Idをcategory行に記載
        }

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        Intent intent = getIntent();
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        Realm realm = Realm.getDefaultInstance();
        mTask = realm.where(Task.class).equalTo("id", taskId).findFirst();
        realm.close();
//        mTask = (Task) intent.getSerializableExtra(MainActivity.EXTRA_TASK);

        if (mTask == null) {
            // 新規作成の場合
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            // 更新の場合
            mTitleEdit.setText(mTask.getTitle());
            mContentEdit.setText(mTask.getContents());
            mCategoryEdit.setText(mTask.getCategory());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }



    }



/*

    //電話情報を受信するためのリスナー
    public PhoneStateListener phoneStateListener=new PhoneStateListener() {

        //基地局の変化時に呼ばれる
        @Override
        public void onCellLocationChanged(CellLocation location) {
            String str="";
            //GSMの基地局情報
            if (location instanceof GsmCellLocation) {
                GsmCellLocation loc=(GsmCellLocation)location;
                Integer cellId = loc.getCid();
                strCellId = cellId.toString();
                Log.d("jothere", strCellId);
//                str+="CID:"+loc.getCid()+"\n";
//                str+="LAC:"+loc.getLac()+"\n";
                mCategoryEdit = (EditText)findViewById(R.id.category_edit_text);
                mCategoryEdit.setText(strCellId);
            }
            //CDMAの基地局情報
            else if(location instanceof CdmaCellLocation) {
                CdmaCellLocation loc=(CdmaCellLocation)location;
                str+="BaseStationId:"+loc.getBaseStationId()+"\n";
                str+="BaseStationLatitude:"+loc.getBaseStationLatitude()+"\n";
                str+="BaseStationLongitude:"+loc.getBaseStationLongitude()+"\n";
                str+="NetworkId:"+loc.getNetworkId()+"\n";
                str+="SystemId:"+loc.getSystemId()+"\n";
            }
        }

    };
*/


    private void addTask() {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();     //161行目からここに移動"Changing Realm data can only be done from inside a transaction."を回避

        if (mTask == null) {
            // 新規作成の場合
            mTask = new Task();

            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();

            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            mTask.setId(identifier);
        }

        String title = mTitleEdit.getText().toString();
        String content = mContentEdit.getText().toString();
//        String category = mCategoryEdit.getText().toString();


        mTask.setTitle(title);
        mTask.setContents(content);
        mTask.setCategory(strCellId);
        GregorianCalendar calendar = new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
        Date date = calendar.getTime();
        mTask.setDate(date);

//        realm.beginTransaction();
        realm.copyToRealmOrUpdate(mTask);
        realm.commitTransaction();

        realm.close();

        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
        resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask.getId());
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                this,
                mTask.getId(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);

    }
}
