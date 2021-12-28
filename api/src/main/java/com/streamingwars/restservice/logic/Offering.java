package com.streamingwars.restservice.logic;

public class Offering {

    private Event event;
    private StudioStreamingServiceTransaction transaction;
    private int offeringsOrderCtr;

    public Offering(Event event, StudioStreamingServiceTransaction transaction, int offeringsOrderCtr) {
        this.event = event;
        this.transaction = transaction;
        this.offeringsOrderCtr = offeringsOrderCtr;
    }

    public Event getEvent() {
        return event;
    }

    public StudioStreamingServiceTransaction getTransaction() {
        return transaction;
    }

    public int getOfferingsOrderCtr() {
        return offeringsOrderCtr;
    }
}