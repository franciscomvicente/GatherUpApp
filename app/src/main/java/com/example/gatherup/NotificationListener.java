package com.example.gatherup;

public interface NotificationListener {
    public void createNotification(String time, Integer id, int icon, String title);
    public void cancelNotification(Integer id);
}
