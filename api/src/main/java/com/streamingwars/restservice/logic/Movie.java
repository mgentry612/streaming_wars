package com.streamingwars.restservice.logic;

public class Movie extends Event{

    public Movie(String name, int yearProduced, int duration, int licenseFee, Studio studio, int eventsOrderCtr) {
        super(name, yearProduced, duration, licenseFee, studio, eventsOrderCtr);
    }

}