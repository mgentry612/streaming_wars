package com.streamingwars.restservice.logic;

import java.math.BigDecimal;

public class Report {

    private BigDecimal currentPeriodRevenue;
    private BigDecimal previousMonthRevenue;
    private BigDecimal totalRevenue;

    public Report(BigDecimal currentPeriodRevenue, BigDecimal previousMonthRevenue, BigDecimal totalRevenue) {
        this.currentPeriodRevenue = currentPeriodRevenue;
        this.previousMonthRevenue = previousMonthRevenue;
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getCurrentPeriodRevenue() {
        return currentPeriodRevenue;
    }

    public BigDecimal getPreviousMonthRevenue() {
        return previousMonthRevenue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}