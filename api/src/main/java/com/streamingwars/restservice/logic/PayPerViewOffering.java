package com.streamingwars.restservice.logic;

public class PayPerViewOffering extends Offering{

    private int viewingPrice;

    public PayPerViewOffering(PayPerView payPerView, StudioStreamingServiceTransaction transaction, int offeringsOrderCtr, int viewingPrice) {
        super(payPerView, transaction, offeringsOrderCtr);

        this.viewingPrice = viewingPrice;
    }

    public int getViewingPrice() {
        return viewingPrice;
    }
}