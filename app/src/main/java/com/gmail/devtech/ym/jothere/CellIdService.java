package com.gmail.devtech.ym.jothere;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.DateFormat;
import android.util.Log;
import android.content.Context;

import java.util.Date;

public class CellIdService extends Service {
    public static final String TAG = "JotHereLog";
    private Integer cellId;


    public TelephonyManager TM;

    public CellIdService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "CellIdCervice onStartCommandが呼ばれました");

        //電話情報の受信開始
        TelephonyManager telManager=(TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CELL_LOCATION);

        return START_STICKY;
    }

    //Cell IDを受信するためのリスナー
    //電話情報を受信するためのリスナー
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

                sendNotification();
        }
    };


    private void sendNotification(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(
                "message", "CID:"+cellId);
        broadcastIntent.setAction("MY_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);


    }
/*    private void sendNotification() {
        Log.d(TAG, "sendNotification()");


        String string = "";
        //Intent intent = new Intent();       //引数なしのIntentを作成。通知をタップしても何もしない。

        // 通知の設定を行う
        NotificationCompat.Builder builder = new NotificationCompat.Builder();
        builder.setSmallIcon(R.drawable.small_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.large_icon));
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);

        // 通知をタップしたらアプリを起動するようにする
        Intent startAppIntent = new Intent(this, MainActivity.class);
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, 0);
        builder.setContentIntent(pendingIntent);


        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Date now = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH時mm分");
        String nowText = formatter.format(now);
        Notification notification;

        string = ;

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setAutoCancel(true);
        builder.setContentTitle("SAMP!");
        builder.setContentText(string);
        builder.setSubText(nowText);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(pendingIntent);
        //builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(false);
        notification = builder.build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;


        manager.notify(0, builder.build());

    }
*/
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
