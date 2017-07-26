package com.example.administrator.myapplication;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.myapplication.beans.NotificationInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private NotificationDatabaseHelper mNotificationDatabaseHelper;
    private Button mBackButton;
    private Button mNewButton;
    private ListView mListView;
    private TimeListAdapter mTimeListAdapter;
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initNotificationList();
        }
    };
    private int selectList = 0;

    private String mCityName;
    private List<NotificationInfo> mNotificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Intent intent = getIntent();
        mCityName = intent.getStringExtra("name");
        mBackButton = (Button) this.findViewById(R.id.button1);
        mNewButton = (Button) this.findViewById(R.id.button2);
        mListView = (ListView) this.findViewById(R.id.listView1);
        initNotificationList();

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationActivity.this,TimePickerActivity.class);
                intent.putExtra("name", mCityName);
                startActivity(intent);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.administrator.broadcast.receiver.DatabaseChanged");
        registerReceiver(myReceiver,intentFilter);
    }

    @Override
    protected void onResume() {
        initNotificationList();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    private void initNotificationList() {
        mNotificationDatabaseHelper = new NotificationDatabaseHelper(this, "forecast1.db3" ,null , 1);
        Cursor cursor = mNotificationDatabaseHelper.getReadableDatabase().rawQuery("select * from notification where name=? order by date,time",new String[]{mCityName});
        mNotificationList.clear();
        while(cursor.moveToNext()) {
            String date = cursor.getString(2);
            String time = cursor.getString(3);
            String week = cursor.getString(4);
            NotificationInfo notificationInfo = new NotificationInfo(date,time,week);
            mNotificationList.add(notificationInfo);
        }
        cursor.close();
        mNotificationDatabaseHelper.close();
        mTimeListAdapter = new TimeListAdapter(getLayoutInflater(),mNotificationList);
        mListView.setAdapter(mTimeListAdapter);
    }

    public String transferDate(String dateStr) {
        Date date = new Date();
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.parse(dateStr);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        Calendar calendarCurrent = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int yearCurrent = calendarCurrent.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int monthCurrent = calendarCurrent.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int dayCurrent = calendarCurrent.get(Calendar.DAY_OF_MONTH);
        if(year == yearCurrent && month == monthCurrent) {
            if(day == dayCurrent) {
                return "今天";
            } else {
                if(day == dayCurrent + 1) {
                    return "明天";
                } else {
                    return dateStr;
                }
            }
        } else {
            return dateStr;
        }
    }

    public class TimeListAdapter extends BaseAdapter {

        private List<NotificationInfo> mData;//定义数据。
        private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。

        /*
        定义构造器，在Activity创建对象Adapter的时候将数据data和Inflater传入自定义的Adapter中进行处理。
        */
        public TimeListAdapter(LayoutInflater inflater,List<NotificationInfo> data){
            mInflater = inflater;
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertview, ViewGroup viewGroup) {
            //获得ListView中的view
            View view = mInflater.inflate(R.layout.time_list_layout,null);
            //获得学生对象
            NotificationInfo notificationInfo = mData.get(position);
            //获得自定义布局中每一个控件的对象。
            TextView date = (TextView) view.findViewById(R.id.textView1);
            TextView time = (TextView) view.findViewById(R.id.textView2);
            TextView week = (TextView) view.findViewById(R.id.textView3);
            Button deleteButton = (Button) view.findViewById(R.id.button);
            //将数据一一添加到自定义的布局中。
            date.setText(transferDate(notificationInfo.getDate()));
            time.setText(notificationInfo.getTime());
            week.setText(notificationInfo.getWeek());
            deleteButton.setTag(position);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectList = (int) v.getTag();

                    new AlertDialog.Builder(NotificationActivity.this).setTitle("确认删除吗？")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“确认”后的操作
                                    if(mNotificationDatabaseHelper == null) {
                                        mNotificationDatabaseHelper = new NotificationDatabaseHelper(NotificationActivity.this, "forecast1.db3" ,null , 1);
                                    }
                                    String dateText = mData.get(selectList).getDate();
                                    String timeText = mData.get(selectList).getTime();
                                    mNotificationDatabaseHelper.getReadableDatabase().execSQL("delete from notification where name=? and date=? and time=?",new String[]{mCityName,dateText,timeText});
                                    mNotificationDatabaseHelper.close();
                                    Intent intent = new Intent("com.example.administrator.broadcast.receiver.DatabaseChanged");
                                    sendBroadcast(intent);
                                }
                            })
                            .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“返回”后的操作,这里不设置没有任何操作
                                }
                            }).show();
                }
            });
            return view ;
        }
    }
}
