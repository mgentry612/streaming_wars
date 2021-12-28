package com.streamingwars.restservice.logic;

import java.math.BigDecimal;

public class StreamingServiceReport extends Report{

    private BigDecimal licensing;

    public StreamingServiceReport(BigDecimal currentPeriodRevenue, BigDecimal previousMonthRevenue, BigDecimal totalRevenue, BigDecimal licensing) {
        super(currentPeriodRevenue, previousMonthRevenue, totalRevenue);

        this.licensing = licensing;
    }

    public BigDecimal getLicensing() {
        return licensing;
    }

}