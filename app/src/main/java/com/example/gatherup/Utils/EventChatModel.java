package com.example.gatherup.Utils;

public class EventChatModel {
    String message, sender, timestamp;

    public EventChatModel() {

    }

    public EventChatModel(String message, String sender, String timestamp){
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}