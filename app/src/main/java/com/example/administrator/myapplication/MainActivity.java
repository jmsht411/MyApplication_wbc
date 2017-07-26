package com.example.administrator.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.iflytek.aiui.AIUIAgent;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    Button mSearchButton;
    Button mVoiceButton;
    EditText mSearchText;
    TableLayout mTableLayout;
    CityDatabaseHelper mCityDatabaseHelper;

    final String[] citys = new String[]{"北京","天津","上海","重庆","沈阳",
            "大连","长春","哈尔滨","郑州","武汉","长沙","广州","合肥",
            "深圳","南京","西安","无锡","常州","苏州","杭州","宁波",
            "济南","青岛","福州","厦门","成都","昆明"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.city_select);
        initViews();
        initCityList();

    }

    private void initViews() {
        mSearchButton = (Button) this.findViewById(R.id.button1);
        mSearchText = (EditText) this.findViewById(R.id.editText1);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertCity(mSearchText.getText().toString());
                Intent intent = new Intent(MainActivity.this,WeatherInfoActivity.class);
                intent.putExtra("name",mSearchText.getText().toString());
                startActivity(intent);
            }
        });
        mVoiceButton = (Button) this.findViewById(R.id.button2);
        mVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NlpDemo.class);
                startActivityForResult(intent,1);
            }
        });
        mTableLayout = (TableLayout) this.findViewById(R.id.cityList);
    }

    private void initCityList() {
        TableRow tableRow = new TableRow(this);
        for(int i=0;i<citys.length;i++) {
            if(i%4 == 0) {
                tableRow = new TableRow(this);
                tableRow.setPadding(0,50,0,50);
                mTableLayout.addView(tableRow);
            }
            Button button = new Button(this);
            button.setText(citys[i]);
            button.setTag(i);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("clicked!");
                    int i = (Integer) v.getTag();
                    insertCity(citys[i]);
                    Intent intent = new Intent(MainActivity.this,WeatherInfoActivity.class);
                    intent.putExtra("name",citys[i]);
                    startActivity(intent);
                }
            });
            tableRow.addView(button);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) {
            String result = data.getStringExtra("name");
            insertCity(result);
            Intent intent = new Intent(MainActivity.this,WeatherInfoActivity.class);
            intent.putExtra("name",result);
            startActivity(intent);
        }
    }

    private void insertCity(String name) {
        mCityDatabaseHelper = new CityDatabaseHelper(MainActivity.this, "forecast.db3" ,null , 1);
        Cursor cursor = mCityDatabaseHelper.getReadableDatabase().rawQuery("select * from city where name = ?",new String[]{name});
        if(cursor.moveToNext()) {
            mCityDatabaseHelper.close();
        } else {
            mCityDatabaseHelper.getReadableDatabase().execSQL("insert into city values(null,?)",new String[]{name});
        }
        cursor.close();
        mCityDatabaseHelper.close();
        updateSharedPreference(name);
        Intent intent = new Intent("com.example.administrator.myapplication.CityListChanged");
        sendBroadcast(intent);
    }

    private void updateSharedPreference(String name) {
        SharedPreferences sp = this.getSharedPreferences("savedCity",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("cityName",name);
        editor.commit();
    }
}
