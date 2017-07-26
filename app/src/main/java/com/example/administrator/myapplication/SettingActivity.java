package com.example.administrator.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private ListView mListView;
    private Button mBackButton;
    private Button mAddButton;
    private ArrayAdapter<String> adapter;
    private int item_id;
    private List<String> mCityList = new ArrayList<>();
    private CityDatabaseHelper mCityDatabaseHelper;
    private NotificationDatabaseHelper mNotificationDatabaseHelpler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mBackButton = (Button) this.findViewById(R.id.button1);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mAddButton =(Button) this.findViewById(R.id.button2);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        mListView = (ListView) this.findViewById(R.id.listView1);
        initListView();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0:
                // 删除操作
                deleteCity(mCityList.get(item_id));
                deleteNotificationOfCity(mCityList.get(item_id));
                mCityList.remove(item_id);
                adapter.notifyDataSetChanged();
                Toast.makeText(SettingActivity.this,"删除",Toast.LENGTH_SHORT).show();
                break;

            case 1:
                // 设置提醒
                Toast.makeText(SettingActivity.this,"设置提醒",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingActivity.this,NotificationActivity.class);
                intent.putExtra("name",mCityList.get(item_id));
                startActivity(intent);
                break;

            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        initListView();
        super.onResume();
    }

    private void deleteCity(String name) {
        if(mCityDatabaseHelper == null) {
            mCityDatabaseHelper = new CityDatabaseHelper(this, "forecast.db3" ,null , 1);
        }
        mCityDatabaseHelper.getReadableDatabase().execSQL("delete from city where name=?",new String[]{name});
    }

    private void deleteNotificationOfCity(String name) {
        if(mNotificationDatabaseHelpler == null) {
            mNotificationDatabaseHelpler = new NotificationDatabaseHelper(this, "forecast1.db3" ,null , 1);
        }
        mNotificationDatabaseHelpler.getReadableDatabase().execSQL("delete from notification where name=?",new String[]{name});
        mNotificationDatabaseHelpler.close();
        Intent intent = new Intent("com.example.administrator.myapplication.CityListChanged");
        sendBroadcast(intent);
    }

    private void initListView() {
        mCityDatabaseHelper = new CityDatabaseHelper(this, "forecast.db3" ,null , 1);
        Cursor cursor = mCityDatabaseHelper.getReadableDatabase().rawQuery("select * from city",null);
        mCityList.clear();
        while(cursor.moveToNext()) {
            String str = cursor.getString(1);
            mCityList.add(str);
        }
        mCityDatabaseHelper.close();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mCityList);
        mListView.setAdapter(adapter);
        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                item_id = info.position;
                menu.add(0, 0, 0, "删除");
                menu.add(0, 1, 0, "设置提醒");
            }
        });
    }

    @Override
    protected void onDestroy() {
        mCityDatabaseHelper.close();
        super.onDestroy();
    }
}
