package com.example.administrator.myapplication.beans;

/**
 * Created by Administrator on 2017/7/13.
 */

public class NotificationInfo {

    private String date;
    private String time;
    private String week;

    public NotificationInfo(String date, String time, String week) {
        this.date = date;
        this.time = time;
        this.week = week;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    @Override
    public String toString() {
        return "NotificationInfo{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", week='" + week + '\'' +
                '}';
    }
}
