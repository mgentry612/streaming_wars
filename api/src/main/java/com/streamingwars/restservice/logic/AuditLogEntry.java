package com.streamingwars.restservice.logic;

public class AuditLogEntry {
    private String actionType;
    private String actionContent;
    private String createdDate;

    public AuditLogEntry(String actionType, String actionContent, String createdDate) {
        this.actionType = actionType;
        this.actionContent = actionContent;
        this.createdDate = createdDate;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionContent() {
        return actionContent;
    }

    public void setActionContent(String actionContent) {
        this.actionContent = actionContent;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
