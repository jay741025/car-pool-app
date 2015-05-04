/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.carpool.dj.carpool.model;



import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager.BadTokenException;
import android.widget.Toast;

import com.carpool.dj.carpool.R;
import com.carpool.dj.carpool.activity.ContentActivity;
import com.carpool.dj.carpool.activity.MainActivity;
import com.carpool.dj.carpool.activity.MapFragment;
import com.carpool.dj.carpool.activity.MsgFragment;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * IntentService responsible for handling GCM messages.
 */
@SuppressLint({ "HandlerLeak", "NewApi" })
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private static JSONObject car_pool_json =new JSONObject();
    private static final String TAG = "GCMIntentService";
    private Context context;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                /*
                for (int i=0; i<5; i++) {
                    Log.i("", "Working... " + (i+1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }*/
                //Log.i("", "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                //sendNotification("Received: " + extras.toString());
                onMessage(Utils.nowActivity,intent);
                Log.i("", "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
    private static void generateNotification(Context context, String message,
                                             Bundle data, String type) {

        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        Uri alarmSound = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Notification notification = new Notification(icon, message, when);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(Utils.nowActivity.getString(R.string.app_name)).setContentText(message)
                .setSmallIcon(icon).setWhen(when).setSound(alarmSound).build();

        // String title = context.getString(R.string.app_name);

        Intent notificationIntent = null;
       if ("CarEvent".equals(type)) {
            notificationIntent = new Intent(context, ContentActivity.class);
        } else{
            return ;
        }
        notificationIntent.putExtras(data);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(context, Utils.nowActivity.getString(R.string.app_name), message,
                intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);

    }

    @SuppressLint("HandlerLeak")
    private Handler NotificationHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {

                if (Utils.getNowActivity() == null) {
                    return;
                }
                Toast.makeText(Utils.getNowActivity(), "Have Message!",
                        Toast.LENGTH_LONG).show();

            } catch (BadTokenException e) {

            }
        }

    };

    @SuppressLint("HandlerLeak")
    private Handler CarPoolNotificationHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);


            try {
                if(MapFragment.onStatus != 0) {
                    MapFragment.showEvent(car_pool_json.getInt("eventType"),
                            car_pool_json.getString("accountId"), car_pool_json.getString("accountName")
                            , car_pool_json.getString("accountPic"), car_pool_json.getString("message"));
                }
            } catch (JSONException e) {
                Utils.ExceptionHandler(e, ContentActivity.tracker, Utils.nowActivity);
            }
        }

    };




    protected void onMessage(Context context, Intent intent) {

        HashMap<String, String> NotificationMap = new HashMap<String, String>();
        try {
            this.context = context;
            String Message = URLDecoder.decode(
                    intent.getStringExtra("message"), "UTF-8");
            Log.i("NotificationMap", "message->" + Message);
            Log.i("NotificationMap",
                    "messageType->"
                            + URLDecoder.decode(
                            intent.getStringExtra("messageType"),
                            "UTF-8"));
            // if (!message.equals(Message)) {
            // message = Message;
            NotificationMap.put("message", Message);
            NotificationMap.put("timestamp", URLDecoder.decode(
                    intent.getStringExtra("timestamp"), "UTF-8"));
            NotificationMap.put("accountId", URLDecoder.decode(
                    intent.getStringExtra("accountId"), "UTF-8"));
            NotificationMap.put("messageType", URLDecoder.decode(
                    intent.getStringExtra("messageType"), "UTF-8"));
            showGCMMassage(NotificationMap);

            NotificationHandler.sendMessage(new Message());

            if("CarEvent".equals(intent.getStringExtra("messageType"))){
                CarPoolNotificationHandler.sendMessage(new Message());
            }

            // }
            // }
        } catch (Exception e) {
            Utils.ExceptionHandler(e, ContentActivity.tracker, Utils.nowActivity);
            Log.e(TAG, e.getMessage());
        }
    }

    private void showGCMMassage(HashMap<String, String> NotificationMap) {

        StringBuffer sb = new StringBuffer();

        sb = new StringBuffer();
        Bundle data = new Bundle();

        if("CarEvent".equals(NotificationMap.get("messageType"))){

            String msg ="" ;
            try {
                car_pool_json = new JSONObject(URLDecoder.decode(
                        NotificationMap.get("message"), "UTF-8"));
                if(car_pool_json.has("message")){
                    msg =car_pool_json.getString("message");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            sb.append(msg + "\n");
            sb.append("於" + NotificationMap.get("timestamp") + "\n");

            if(MapFragment.onStatus != 0) {
                MsgFragment.putList(Utils.UserPic, Utils.UserName, Utils.getSimSerialNumber(Utils.getNowActivity()),
                        msg);
            }


        }else{
            return;
        }
        //displayMessage(context, sb.toString());
        // notifies user

        // data.putString("MESSAGE", NotificationMap.get("MESSAGE"));
        generateNotification(context, sb.toString(), data,
                NotificationMap.get("messageType"));

    }

}
