package com.streamingwars.restservice.logic;

import java.util.ArrayList;

public class Studio {

    private String shortName;
    private String longName;
    private ArrayList<Event> events;

    public Studio(String shortName, String longName) {
        this.shortName = shortName;
        this.longName = longName;
        this.events = new ArrayList<Event>();
    }

    // public Movie createMovie(String name, int yearProduced, int duration, int licenseFee, int eventsOrderCtr) {
    //     Movie newMovie = new Movie(name, yearProduced, duration, licenseFee, this, eventsOrderCtr);
    //     events.add(newMovie);

    //     return newMovie;
    // }

    // public PayPerView createPayPerView(String name, int yearProduced, int duration, int licenseFee, int eventsOrderCtr) {
    //     PayPerView payPerView = new PayPerView(name, yearProduced, duration, licenseFee, this, eventsOrderCtr);
    //     events.add(payPerView);

    //     return payPerView;
    // }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public ArrayList<Event> getEvents() {
        return (ArrayList<Event>) events.clone();
    }
}