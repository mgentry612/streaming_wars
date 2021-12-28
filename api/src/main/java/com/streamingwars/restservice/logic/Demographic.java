package com.streamingwars.restservice.logic;

public class Demographic {

    private String shortName;
    private String longName;
    private int numAccounts;

    public Demographic(String shortName, String longName, int numAccounts) {
        this.shortName = shortName;
        this.longName = longName;
        this.numAccounts = numAccounts;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public int getNumAccounts() {
        return numAccounts;
    }

    public void setNumAccounts(int numAccounts) {
        this.numAccounts = numAccounts;
    }

}