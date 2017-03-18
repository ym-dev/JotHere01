package com.gmail.devtech.ym.jothere;


import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;


import android.telephony.PhoneStateListener;

public class InputActivity extends AppCompatActivity {

    public static final String TAG = "JotHereLog";

    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button mDateButton, mTimeButton;
    private EditText mTitleEdit, mContentEdit;
    private TextView mCategoryEdit;
    private Task mTask;
    private String strCellId;
    public TelephonyManager TM;
    private PhoneStateListener PSL;
    private int event;
    private Realm mRealm;



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


    //Registerボタン選択時処理
    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTask();
            finish();
        }
    };

    //Deleteボタン選択時処理
    private View.OnClickListener mOnDelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            delTask();

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
        findViewById(R.id.del_button).setOnClickListener(mOnDelClickListener);

        mTitleEdit = (EditText)findViewById(R.id.title_edit_text);
        mContentEdit = (EditText)findViewById(R.id.content_edit_text);
        mCategoryEdit = (TextView)findViewById(R.id.category_edit_text);


        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        Intent intent = getIntent();
        int taskIdExtra = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        int fromNotiFlag = intent.getIntExtra("FromNotifiFlag", -1);
        Log.d(TAG, "Intentで飛んできたtaskIdExtra="+taskIdExtra+", NotifiFlag="+fromNotiFlag);
        Realm realm = Realm.getDefaultInstance();
        mTask = realm.where(Task.class).equalTo("id", taskIdExtra).findFirst();
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

        //Cell IDの取得
        if(fromNotiFlag != 1) {
            TM = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            PSL = new PhoneStateListener();
            int event = PSL.LISTEN_CELL_INFO | PSL.LISTEN_CELL_LOCATION;
            TM.listen(PSL, event);
            GsmCellLocation gsmCellLocation = (GsmCellLocation) TM.getCellLocation();

            if (gsmCellLocation != null) {
//        Integer cid = gsmCellLocation.getCid() & 0xffff;  //cell id
                Integer cid = gsmCellLocation.getCid();  //cell id
                strCellId = cid.toString();
                mCategoryEdit.setText(strCellId);       //Cell Idをcategory行に記載

                Log.d(TAG, "strCellID= " + strCellId);
            } else {
                String cidobterr = (String) getText(R.string.cid_obt_err);
                mCategoryEdit.setText(cidobterr);       //Cell Idが取れない旨をcategory行に記載
            }
        }else{
            Log.d(TAG, "Notificationからきたので再度CellId取得はしません");
        }




    }



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
        mTask.setCategory(strCellId);           //CellIdをRealmに追記
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


    private void delTask() {
        Log.d(TAG, "delボタンが押されました");
        // タスクを削除する

        // ダイアログを表示する
        AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
        String strDelNotiTitle = (String) getText(R.string.del_noti_title);
        String strDelNotiMsg = (String) getText(R.string.del_noti_body);

        builder.setTitle(strDelNotiTitle);
        builder.setMessage("["+mTask.getTitle()+"] "+ strDelNotiMsg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRealm = Realm.getDefaultInstance();

                RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", mTask.getId()).findAll();



                Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                        getApplicationContext(),
                        mTask.getId(),
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(resultPendingIntent);

                mRealm.beginTransaction();
                results.deleteAllFromRealm();
                mRealm.commitTransaction();


                finish();


            }
        });
        builder.setNegativeButton("CANCEL", null);

        AlertDialog dialog = builder.create();
        dialog.show();



    }
}
