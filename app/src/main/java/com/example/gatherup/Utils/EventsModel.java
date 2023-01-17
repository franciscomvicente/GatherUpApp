package com.example.gatherup.Utils;

import android.location.Location;

import com.google.android.material.slider.BaseOnChangeListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class EventsModel {
    private String Title, Address;
    private Timestamp Date;
    private String eventID;
    private GeoPoint Local;
    private String Description;
    private Location Location;
    private String Distance;
    private String Theme;
    private Integer Subscribed;
    private Boolean Private;

    private EventsModel() {}

    private EventsModel(String Title, Timestamp Date, String eventID, GeoPoint Local, String Description, String Address, Location Location, String Distance, String Theme, Boolean Private, Integer Subscribed){
        this.Title = Title;
        this.Date  = Date;
        this.eventID = eventID;
        this.Local = Local;
        this.Description = Description;
        this.Address = Address;
        this.Location = Location;
        this.Distance = Distance;
        this.Theme = Theme;
        this.Subscribed = Subscribed;
        this.Private = Private;
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

    public Location getLocation() {
        return Location;
    }

    public void setLocation(Location Location) {
        this.Location = Location;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String Distance) {
        this.Distance = Distance;
    }

    public String getTheme() {
        return Theme;
    }

    public void setTheme(String Theme) {
        this.Theme = Theme;
    }

    public Integer getSubscribed() {
        return Subscribed;
    }

    public void setSubscribed(Integer Subscribed) {
        this.Subscribed = Subscribed;
    }

    public void setPrivate()  {this.Private = Private;}

    public Boolean getPrivate() {return Private;}


}
