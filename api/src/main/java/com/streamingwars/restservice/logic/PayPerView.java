package com.streamingwars.restservice.logic;

public class PayPerView extends Event{

    public PayPerView(String name, int yearProduced, int duration, int licenseFee, Studio studio, int eventsOrderCtr) {
        super(name, yearProduced, duration, licenseFee, studio, eventsOrderCtr);
    }

}