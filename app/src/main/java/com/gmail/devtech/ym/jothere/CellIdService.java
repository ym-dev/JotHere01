package com.gmail.devtech.ym.jothere;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.DateFormat;
import android.util.Log;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class CellIdService extends Service {
    public static final String TAG = "JotHereLog";
    public Integer cellId;
    public TelephonyManager TM;
    private Task mTaskService;

    public CellIdService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "CellIdCervice onStartCommandが呼ばれました");

/*
        //sendNotifi実験用コード。サービス起動５秒後に発動
        try {
            Thread.sleep(5000);
            Integer testId = 111111;
            sendNotification(testId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/

        //電話情報の受信開始
        TelephonyManager telManager=(TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CELL_LOCATION);

        return START_STICKY;
    }

    //Cell IDを受信するためのリスナー
    public PhoneStateListener phoneStateListener=new PhoneStateListener() {

        //基地局の変化時に呼ばれる
        @Override
        public void onCellLocationChanged(CellLocation location) {
            String str = "";
            //GSMの基地局情報
            if (location instanceof GsmCellLocation) {
                GsmCellLocation loc = (GsmCellLocation) location;
                cellId = loc.getCid();
                str += "CID:" + loc.getCid() + "\n";



//                str += "LAC:" + loc.getLac() + "\n";
            }
            //CDMAの基地局情報
            else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation loc = (CdmaCellLocation) location;
                str += "BaseStationId:" + loc.getBaseStationId() + "\n";
                str += "BaseStationLatitude:" + loc.getBaseStationLatitude() + "\n";
                str += "BaseStationLongitude:" + loc.getBaseStationLongitude() + "\n";
                str += "NetworkId:" + loc.getNetworkId() + "\n";
                str += "SystemId:" + loc.getSystemId() + "\n";
            }
            Log.d(TAG, str);

//            sendNotification(cellId);
            cellIdMatching(cellId);
            
        }
    };


    //PhoneStateListenerで取得したCellIDとRealmに保存しているTaskのCellIDを比較
    private void cellIdMatching(Integer cellId) {
        Log.d(TAG, "cellIdMatching() cellid="+cellId);

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Task> cidRealmResults = realm.where(Task.class).findAll();
        Log.d(TAG, "cidRealmResults.size="+cidRealmResults.size());

        for (int i = 0; i < cidRealmResults.size(); i++) {
            if (!cidRealmResults.get(i).isValid()) continue;
            String taskCid = cidRealmResults.get(i).getCategory();
            int intTaskCid = Integer.parseInt(taskCid);
            Log.d(TAG, "i= "+i+" taskCid="+taskCid);


            if(intTaskCid == cellId){
                Log.d(TAG, "i="+i+"cellId Match!");
                sendNotification(cellId, i);
            }else{
                Log.d(TAG, "i="+i+"cellId UnMatch");
            }


        }

        realm.close();



    }

/*
    private void sendNotification(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(
                "message", "CID:"+cellId);
        broadcastIntent.setAction("MY_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);


    }

    */

    private void sendNotification(Integer cellid, int number) {
        Log.d(TAG, "sendNotification() cellid="+cellid);

        String string = "";
        //Intent intent = new Intent();       //引数なしのIntentを作成。通知をタップしても何もしない。
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE);
        String nowText = formatter.format(now);
        Log.d(TAG, "nowTime="+nowText);
        Notification notification;

        string = cellid.toString();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setAutoCancel(true);
        builder.setContentTitle("Cell ID Changed!");
        builder.setContentText("i= "+number+" CID="+string);
//        builder.setSubText(nowText);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info);
//        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(pendingIntent);
//        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(false);
        notification = builder.build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;


        manager.notify(number, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
