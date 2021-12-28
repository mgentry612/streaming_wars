package com.streamingwars.restservice.logic;

import java.util.Calendar;
import java.math.BigDecimal;

public class Transaction {

    private StreamingService streamingService;
    private Event event;
    private Calendar month;
    private BigDecimal transactionAmount;
    private int id;

    public Transaction(StreamingService streamingService, Event event, Calendar month, int id) {
        this.streamingService = streamingService;
        this.event = event;
        this.month = month;
        this.transactionAmount = new BigDecimal(event.getLicenseFee());
        this.id = id;
    }

    public StreamingService getStreamingService() {
        return streamingService;
    }

    public Event getEvent() {
        return event;
    }

    public Calendar getMonth() {
        return month;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}