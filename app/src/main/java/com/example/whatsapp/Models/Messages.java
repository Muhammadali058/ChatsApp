package com.example.whatsapp.Models;

public class Messages {
    String messageId, senderId, message, imageUrl;
    int reaction = -1;
    long time;

    public Messages() {
    }

    public Messages(String senderId, String message, long time) {
        this.senderId = senderId;
        this.message = message;
        this.time = time;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getReaction() {
        return reaction;
    }

    public void setReaction(int reaction) {
        this.reaction = reaction;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long timestamp) {
        this.time = timestamp;
    }
}
