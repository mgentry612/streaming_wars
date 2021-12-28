package com.streamingwars.restservice.logic;

public class MovieOffering extends Offering{

    public MovieOffering(Movie movie, StudioStreamingServiceTransaction transaction, int offeringsOrderCtr) {
        super(movie, transaction, offeringsOrderCtr);
    }

}