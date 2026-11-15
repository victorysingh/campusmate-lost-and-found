package com.campusmate.model;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** One chat message, stored in claims/{claimId}/messages. */
public class ChatMessage {

    public String id = "";
    public String senderId = "";
    public String senderName = "";
    public String text = "";
    public long createdAt;

    public ChatMessage() {
    }

    public ChatMessage(String senderId, String senderName, String text, long createdAt) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("senderId", senderId);
        map.put("senderName", senderName);
        map.put("text", text);
        map.put("createdAt", new Timestamp(new Date(createdAt)));
        return map;
    }

    public static ChatMessage fromMap(String id, Map<String, Object> map) {
        ChatMessage m = new ChatMessage();
        m.id = id;
        m.senderId = Item.text(map.get("senderId"), "");
        m.senderName = Item.text(map.get("senderName"), "");
        m.text = Item.text(map.get("text"), "");
        m.createdAt = Item.millis(map.get("createdAt"));
        return m;
    }
}
