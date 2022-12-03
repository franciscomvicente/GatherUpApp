package com.example.gatherup.Utils;

public class EventsModel {
    private String Title, Hours, Date;
    private String eventID;

    private EventsModel() {}

    private EventsModel(String Title, String Hours, String Date,String eventID){
        this.Title = Title;
        this.Date  = Date;
        this.Hours = Hours;
        this.eventID = eventID;
    }

    public String getTitle(){
        return Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        this.Date = Date;
    }

    public String getHours() {
        return Hours;
    }

    public void setHours(String hours) {
        this.Hours = hours;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}
