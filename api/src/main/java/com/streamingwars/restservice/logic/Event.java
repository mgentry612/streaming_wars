package com.streamingwars.restservice.logic;

public class Event {

    private String name;
    private int yearProduced;
    private int duration;
    private int licenseFee;
    private Studio studio;
    private int eventsOrderCtr;
    private String type;

    public Event(String name, int yearProduced, int duration, int licenseFee, Studio studio, int eventsOrderCtr) {
        this.name = name;
        this.yearProduced = yearProduced;
        this.duration = duration;
        this.licenseFee = licenseFee;
        this.studio = studio;
        this.eventsOrderCtr = eventsOrderCtr;
    }

    public String getName() {
        return name;
    }

    public int getYearProduced() {
        return yearProduced;
    }

    public int getDuration() {
        return duration;
    }

    public int getLicenseFee() {
        return licenseFee;
    }

    public Studio getStudio() {
        return studio;
    }

    public int getEventsOrderCtr() {
        return eventsOrderCtr;
    }

    public void setLicenseFee(int licenseFee) {
        this.licenseFee = licenseFee;
    }

    public void setEventType(String eventType){
        this.type = eventType;
    }

    public String getType() {
        return this.type;
    }
}