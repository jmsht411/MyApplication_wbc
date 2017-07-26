package com.example.administrator.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    public static final String ONCLICK = "com.app.onclick";
    NotificationDatabaseHelper notificationDatabaseHelper;
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setLatestNotification();
        }
    };
    private Timer timer = new Timer();
    public static final int SEARCH_WEATHER = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEARCH_WEATHER:
                    handleWeatherInfo((String)msg.obj);
            }
        }
    };
    private String mName;
    private String mDate;
    private String mTime;
    private String mTemperature;
    private String mWeatherType;


    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.administrator.broadcast.receiver.DatabaseChanged");
        registerReceiver(myReceiver,intentFilter);
        setLatestNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    private void setLatestNotification() {
        notificationDatabaseHelper = new NotificationDatabaseHelper(this, "forecast1.db3" ,null , 1);
        Cursor cursor = notificationDatabaseHelper.getReadableDatabase().rawQuery("select * from notification order by date,time",null);
        if(cursor.moveToNext()) {
            mName = cursor.getString(1);
            mDate = cursor.getString(2);
            mTime = cursor.getString(3);
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendHttpRequest();
                }
            }, getDateTime(mDate +" " + mTime));
        }
        cursor.close();
        notificationDatabaseHelper.close();


    }

    private Date getDateTime(String dateString) {
        Date date = null;
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            date = sdf.parse(dateString);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return date;
    }

    private void sendHttpRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    StringBuilder urlStr = new StringBuilder();
                    urlStr.append("http://wthrcdn.etouch.cn/weather_mini?city=")
                            .append(URLEncoder.encode(mName,"utf-8"));
                    String urlString = urlStr.toString();
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println(response.toString());
                    Message message = new Message();
                    message.what = SEARCH_WEATHER;
                    message.obj = response.toString();
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void handleWeatherInfo(String jsonString) {
        try {
            System.out.println(jsonString);
            JSONObject object = new JSONObject(jsonString);
            JSONObject jsonObject1 = object.getJSONObject("data");
            mTemperature = jsonObject1.getString("wendu");
            JSONArray jsonArray = jsonObject1.getJSONArray("forecast");
            JSONObject weatherObject = jsonArray.getJSONObject(0);
            mWeatherType = weatherObject.getString("type");

            NotificationManager mn = (NotificationManager) NotificationService.this
                    .getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(
                    NotificationService.this);

            Intent notificationIntent = new Intent(NotificationService.this,
                    WeatherInfoActivity.class);// 点击跳转位置
            notificationIntent.putExtra("newName",mName);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    NotificationService.this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentIntent(contentIntent);
            builder.setSmallIcon(android.R.drawable.ic_dialog_info);// 设置通知图标
            builder.setTicker("天气提醒"); // 测试通知栏标题
            builder.setContentText(mTemperature + " " + "℃" + "\n" + mWeatherType); // 下拉通知啦内容
            builder.setContentTitle(mName + "天气");// 下拉通知栏标题
            builder.setAutoCancel(true);// 点击弹出的通知后,让通知将自动取消
            builder.setVibrate(new long[] { 0, 2000, 1000, 4000 }); // 震动需要真机测试-延迟0秒震动2秒延迟1秒震动4秒
            // builder.setSound(Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI,
            // "5"));//获取Android多媒体库内的铃声
            // builder.setSound(Uri.parse("file:///sdcard/xx/xx.mp3"))
            // ;//自定义铃声
            builder.setDefaults(Notification.DEFAULT_ALL);// 设置使用系统默认声音
            // builder.addAction("图标", title, intent); //此处可设置点击后 打开某个页面
            Notification notification = builder.build();
            notification.flags = notification.FLAG_AUTO_CANCEL;// 声音无限循环
            mn.notify((int) System.currentTimeMillis(), notification);
            deleteCurrentNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteCurrentNotification() {
        if(notificationDatabaseHelper == null) {
            notificationDatabaseHelper = new NotificationDatabaseHelper(this, "forecast1.db3" ,null , 1);
        }
        notificationDatabaseHelper.getReadableDatabase().execSQL("delete from notification where name=? and date=? and time=?",new String[]{mName,mDate,mTime});
        notificationDatabaseHelper.close();
        Intent intent = new Intent("com.example.administrator.broadcast.receiver.DatabaseChanged");
        sendBroadcast(intent);
    }
}
