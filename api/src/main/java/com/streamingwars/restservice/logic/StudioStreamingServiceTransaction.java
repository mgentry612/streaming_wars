package com.streamingwars.restservice.logic;

import java.util.Calendar;

public class StudioStreamingServiceTransaction extends Transaction {

    public StudioStreamingServiceTransaction(StreamingService streamingService, Event event, Calendar month, int id) {
        super(streamingService, event, month, id);
    }
}