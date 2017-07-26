package com.example.administrator.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimePickerActivity extends AppCompatActivity {

    private EditText datePicker;
    private EditText timePicker;
    private Button mBackButton;
    private Button mSaveButton;

    private String mCityName;
    private NotificationDatabaseHelper notificationDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);
        Intent intent = getIntent();
        mCityName = intent.getStringExtra("name");
        mBackButton = (Button) this.findViewById(R.id.button1);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSaveButton = (Button) this.findViewById(R.id.button2);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(testTime()) {
                    Toast.makeText(TimePickerActivity.this,"非法时间",Toast.LENGTH_SHORT).show();
                } else {
                    saveNotification();
                }
            }
        });
        datePicker = (EditText) this.findViewById(R.id.editText1);
        timePicker = (EditText) this.findViewById(R.id.editText2);
        datePicker.setCursorVisible(false);
        timePicker.setCursorVisible(false);
        datePicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showDatePickDlg();
                    return true;
                }
                return false;
            }
        });
        datePicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickDlg();
                }
            }
        });

        timePicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showTimePickDlg();
                    return true;
                }
                return false;
            }
        });

        timePicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showTimePickDlg();
                }
            }
        });
    }

    private boolean testTime() {
        if(TextUtils.isEmpty(datePicker.getText()) || TextUtils.isEmpty(timePicker.getText())) {
            return true;
        } else {
            String dateStr = datePicker.getText().toString();
            String timeStr = timePicker.getText().toString();
            Date date = new Date();
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                date = sdf.parse(dateStr + " " + timeStr);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
            Date dateCurrent = new Date();
            return date.before(dateCurrent);
        }
    }

    private void saveNotification() {
        notificationDatabaseHelper = new NotificationDatabaseHelper(this, "forecast1.db3" ,null , 1);
        String date = datePicker.getText().toString();
        String time = timePicker.getText().toString();
        String week = getWeekOfDate(getDate(date));
        Cursor cursor = notificationDatabaseHelper.getReadableDatabase().rawQuery("select * from notification where name=? and date=? and time=?",new String[]{mCityName,date,time});
        if(cursor.moveToNext()) {
            Toast.makeText(TimePickerActivity.this,"选择提醒时间重复，请重新选择！",Toast.LENGTH_SHORT).show();
        } else {
            notificationDatabaseHelper.getReadableDatabase().execSQL("insert into notification values(null,?,?,?,?)",new String[]{mCityName,date,time,week});
        }
        cursor.close();
        notificationDatabaseHelper.close();
        Intent intent = new Intent("com.example.administrator.broadcast.receiver.DatabaseChanged");
        sendBroadcast(intent);
        finish();
    }

    private void showDatePickDlg() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(TimePickerActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                TimePickerActivity.this.datePicker.setText(year + "-" + addZero(monthOfYear+1) + "-" + addZero(dayOfMonth));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickDlg() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(TimePickerActivity.this,new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                TimePickerActivity.this.timePicker.setText(addZero(hourOfDay) + ":" + addZero(minute));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true);
        timePickerDialog.show();
    }

    private Date getDate(String dateString) {
        Date date = null;
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.parse(dateString);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return date;
    }

    public String getWeekOfDate(Date dt) {
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    private String addZero(int num) {
        String numStr = String.valueOf(num);
        if(num < 10) {
            numStr = "0" + numStr;
        }
        return numStr;
    }

}
