package com.streamingwars.restservice.logic;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Calendar;
import java.sql.*;

public class StreamingServiceDemographicTransaction extends Transaction {

    private Demographic demographic;
    private int demographicPercentage;
	private Connection conn;

    public StreamingServiceDemographicTransaction(StreamingService streamingService, Event event, Calendar month, Demographic demographic, int demographicPercentage, int id, Connection conn) {
        super(streamingService, event, month, id);

        this.demographic = demographic;
        this.demographicPercentage = demographicPercentage;
        this.conn = conn;
        int demographicNumAccounts = demographic.getNumAccounts();
        int lastDemographicPercent = getLastTransactionDemographicPercent(month);

        if (event instanceof Movie) {

            if (demographicPercentage > lastDemographicPercent) {
                int remainingDemographicPercentage = demographicPercentage - lastDemographicPercent;
                int subscriptionPrice = streamingService.getSubscriptionPrice();
                long numNewAccountsWatchedEvent = (long) ((demographicNumAccounts * remainingDemographicPercentage) / 100);

                this.setTransactionAmount(new BigDecimal(String.valueOf(subscriptionPrice * numNewAccountsWatchedEvent)));

            } else {
                this.setTransactionAmount(new BigDecimal(String.valueOf((int) 0)));
            }

        } else if (event instanceof PayPerView) {

            Offering offering = streamingService.getOfferingFromEvent(event);
            int viewingPrice = ((PayPerViewOffering) offering).getViewingPrice();
            long numAccountsWatchedEvent = (long) ((demographicNumAccounts * demographicPercentage) / 100);

            this.setTransactionAmount(new BigDecimal(String.valueOf(viewingPrice * numAccountsWatchedEvent)));
        }
    }

    private int getLastTransactionDemographicPercent(Calendar month) {

        try {
            String sql = "SELECT MAX(demographic_percentage) as max FROM ssd_transaction WHERE date_trunc('month', month) = date_trunc('month',to_timestamp(?)::timestamp) AND streaming_service = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setDouble(1, month.getTimeInMillis() / 1000);
            preparedStatement.setString(2, this.getStreamingService().getShortName());

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                return rs.getInt("max");
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private boolean isSameMonth(Transaction transaction, Calendar monthYear) {

        if (transaction.getMonth().get(Calendar.MONTH) == monthYear.get(Calendar.MONTH)
                && transaction.getMonth().get(Calendar.YEAR) == monthYear.get(Calendar.YEAR)) {
            return true;
        }

        return false;
    }

    public Demographic getDemographic() {
        return demographic;
    }

    public int getDemographicPercentage() {
        return demographicPercentage;
    }
}