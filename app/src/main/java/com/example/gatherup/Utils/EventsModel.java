package com.example.gatherup.Utils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class EventsModel {
    private String Title, Address;
    private Timestamp Date;
    private String eventID;
    private GeoPoint Local;
    private String Description;

    private EventsModel() {}

    private EventsModel(String Title, Timestamp Date, String eventID, GeoPoint Local, String Description, String Address){
        this.Title = Title;
        this.Date  = Date;
        this.eventID = eventID;
        this.Local = Local;
        this.Description = Description;
        this.Address = Address;
    }

    public String getTitle(){
        return Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public Timestamp getDate() {
        return Date;
    }

    public void setDate(Timestamp date) {
        this.Date = date;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public GeoPoint getLocal() {
        return Local;
    }

    public void setLocal(GeoPoint Local) {
        this.Local = Local;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }
}
