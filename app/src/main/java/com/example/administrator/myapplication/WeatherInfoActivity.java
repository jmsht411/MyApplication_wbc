package com.example.administrator.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;


import java.util.ArrayList;
import java.util.List;

public class WeatherInfoActivity extends FragmentActivity implements WeatherInfoFragment.OnFragmentInteractionListener {


    Button mSettingButton;
    private List<String> cityList = new ArrayList<>();
    private String currentCityName = "";
    private int currentCityPosition = 0;
    private int currentPagePosition = 0;
    CityDatabaseHelper mCityDatabaseHelper;
    ViewPager mViewPager;
    ViewPagerIndicator mIndicator;

    @Override
    public void onFragmentInteraction(Uri uri) {
        System.out.println("com ok");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("WeatherInfoActivity onCreate called!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_info);
        Intent intent = getIntent();
        currentCityName = intent.getStringExtra("name");
        mSettingButton = (Button) this.findViewById(R.id.settingButton);
        mSettingButton.getBackground().setAlpha(255);
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherInfoActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
        getCityList();
        startNotificationService();
    }

    private void startNotificationService() {
        Intent intent = new Intent(this,NotificationService.class);
        startService(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String name = intent.getStringExtra("newName");
        if(name != null) {
            SharedPreferences sp = this.getSharedPreferences("savedCity",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("cityName",name);
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        getCityList();
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences sp = this.getSharedPreferences("savedCity",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("cityName",cityList.get(currentPagePosition));
        editor.commit();
        super.onPause();
    }

    private void getCityList() {
        mCityDatabaseHelper = new CityDatabaseHelper(this, "forecast.db3" ,null , 1);
        Cursor cursor = mCityDatabaseHelper.getReadableDatabase().rawQuery("select * from city",null);
        cityList.clear();
        while(cursor.moveToNext()) {
            cityList.add(cursor.getString(1));
        }
        List<Fragment> fragments=new ArrayList<>();
        for (String str:cityList
                ) {
            WeatherInfoFragment weatherInfoFragment = WeatherInfoFragment.newInstance(str,"");
            fragments.add(weatherInfoFragment);
        }

        if(cityList.isEmpty()) {
            Intent intent1 = new Intent(this,MainActivity.class);
            startActivity(intent1);
            finish();
        }

        getSavedCity();
        for(int i=0;i<cityList.size();i++) {
            String n = cityList.get(i);
            if(n.equals(currentCityName)) {
                currentCityPosition = i;
                break;
            }
        }



        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(),fragments);
        mViewPager = (ViewPager) this.findViewById(R.id.viewpager);
        mViewPager.removeAllViewsInLayout();
        mIndicator = (ViewPagerIndicator) findViewById(R.id.vpi_page);
        mIndicator.removeAllViewsInLayout();
        //动态地设置可见tab的个数,必须大于0
        mIndicator.setmTabVisibleCount(cityList.size());
        //动态设置tab
        mIndicator.setTabItemTitles(cityList);
        mViewPager.setAdapter(adapter);

        //设置关联的ViewPager
        mIndicator.setViewPager(mViewPager,currentCityPosition);

        //给自定义控件设置tab 页签改变的监听器    可以不设置
        mIndicator.addOnPageChangeListener(new ViewPagerIndicator.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d(TAG, "onPageScrolled: 1");
            }

            @Override
            public void onPageSelected(int position) {
                //Log.d(TAG, "onPageSelected: 1");
                currentPagePosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.d(TAG, "onPageScrollStateChanged: 1");
            }
        });

    }

    private void getSavedCity() {
        SharedPreferences sp = getSharedPreferences("savedCity",MODE_PRIVATE);
        currentCityName = sp.getString("cityName","defaultName");
        System.out.println("savedCity" + currentCityName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCityDatabaseHelper != null) {
            mCityDatabaseHelper.close();
        }
    }

}
