package model;

import java.time.LocalDateTime;

public class ChatMessage {
    private String sender;
    private String recipient;
    private String content;
    private LocalDateTime timestamp;
    private byte[] encryptedContent;
    private byte[] mac;

    //Getters
    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public byte[] getEncryptedContent() {
        return encryptedContent;
    }

    public byte[] getMac() {
        return mac;
    }

    //Setters
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }

    public void setEncryptedContent(byte[] encryptedContent) {
        this.encryptedContent = encryptedContent;
    }
}
