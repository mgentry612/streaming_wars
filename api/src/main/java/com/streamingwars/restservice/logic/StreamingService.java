package com.streamingwars.restservice.logic;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.sql.*;

public class StreamingService {

    private String shortName;
    private String longName;
    private int subscriptionPrice;
    private ArrayList<Offering> offerings;
    private ArrayList<Transaction> transactions;
	private Connection conn;

    public StreamingService(String shortName, String longName, int subscriptionPrice, Connection conn) {
        this.shortName = shortName;
        this.longName = longName;
        this.subscriptionPrice = subscriptionPrice;

        this.offerings = new ArrayList<Offering>();
        this.transactions = new ArrayList<Transaction>();

        this.conn = conn;
    }

    public MovieOffering offerMovie(Movie movie, Calendar month, int offeringsOrderCtr) {

        if (getOfferingFromEvent(movie) != null) {
            return null;
        }

        StudioStreamingServiceTransaction transaction = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            String sql = "INSERT INTO sss_transaction (streaming_service, event_id, month, transaction_amount) VALUES (?,?,to_timestamp(?),?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, this.getShortName());
            preparedStatement.setInt(2, movie.getEventsOrderCtr());
            preparedStatement.setDouble(3, month.getTimeInMillis() / 1000);
            preparedStatement.setInt(4, movie.getLicenseFee());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction = new StudioStreamingServiceTransaction(this, movie, month, generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating transaction failed, no ID obtained.");
                    }
                }
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (transaction == null) {
            return null;
        }

        MovieOffering offering = null;
        try {
            String sql = "INSERT INTO offering (sss_transaction_id) VALUES (?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, transaction.getId());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        offering = new MovieOffering(movie, transaction, generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating offering failed, no ID obtained.");
                    }
                }
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offering;
    }

    public PayPerViewOffering offerPayPerView(PayPerView payPerView, int viewingPrice, Calendar month, int offeringsOrderCtr) {

        if (getOfferingFromEvent(payPerView) != null) {
            return null;
        }

        StudioStreamingServiceTransaction transaction = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            String sql = "INSERT INTO sss_transaction (streaming_service, event_id, month, transaction_amount) VALUES (?,?,to_timestamp(?),?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, this.getShortName());
            preparedStatement.setInt(2, payPerView.getEventsOrderCtr());
            preparedStatement.setDouble(3, month.getTimeInMillis() / 1000);
            preparedStatement.setInt(4, payPerView.getLicenseFee());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction = new StudioStreamingServiceTransaction(this, payPerView, month, generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating transaction failed, no ID obtained.");
                    }
                }
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (transaction == null) {
            return null;
        }

        PayPerViewOffering offering = null;
        try {
            String sql = "INSERT INTO offering (sss_transaction_id, viewing_price) VALUES (?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, transaction.getId());
            preparedStatement.setInt(2, viewingPrice);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        offering = new PayPerViewOffering(payPerView, transaction, generatedKeys.getInt(1), viewingPrice);
                    } else {
                        throw new SQLException("Creating offering failed, no ID obtained.");
                    }
                }
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offering;
    }

    public Offering getOfferingFromEvent(Event event) {

        Offering offering = null;
        try {
            String sql = "SELECT "
                    + "sss_transaction.id as sss_transaction_id, "
                    + "date_part('month', sss_transaction.month)::int - 1 as month, "
                    + "date_part('year', sss_transaction.month)::int - 1 as year, "
                    + "offering.id as offering_id, "
                    + "offering.viewing_price as viewing_price, "
                    + "event.event_type as event_type, "
                    + "event.name as event_name, "
                    + "event.year_produced as event_year_produced, "
                    + "event.license_fee as event_license_fee, "
                    + "event.studio as event_studio, "
                    + "event.duration as event_duration, "
                    + "event.id as event_id "
                + "FROM offering "
                + "INNER JOIN sss_transaction on offering.sss_transaction_id = sss_transaction.id "
                + "INNER JOIN event on event.id = sss_transaction.event_id "
                + "WHERE sss_transaction.event_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, event.getEventsOrderCtr());

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Calendar cal = Calendar.getInstance();
                cal.set(rs.getInt("year"), rs.getInt("month"), 1, 0, 0);
                cal.set(Calendar.MILLISECOND, 0);
                StudioStreamingServiceTransaction transaction = new StudioStreamingServiceTransaction(this, event, cal, rs.getInt("sss_transaction_id"));
                String type = rs.getString("event_type");
                if (type.equals("movie")) {
                    // Movie movie = new Movie(rs.getString("event_name"), rs.getInt("event_year_produced"), rs.getInt("event_duration"), rs.getInt("event_license_fee"), rs.getString("event_studio"), rs.getInt("event_id"));
                    offering = new MovieOffering((Movie) event, transaction, rs.getInt("offering_id"));
                } else if (type.equals("ppv")) {
                    
                    // PayPerView payPerView = new PayPerView(rs.getString("event_name"), rs.getInt("event_year_produced"), rs.getInt("event_duration"), rs.getInt("event_license_fee"), rs.getString("event_studio"), rs.getInt("event_id"));
                    offering = new PayPerViewOffering((PayPerView) event, transaction, rs.getInt("offering_id"), rs.getInt("viewing_price"));
                }
                
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offering;
    }

    public StreamingServiceDemographicTransaction watchEvent(Demographic demographic, int demographicPercentage, Event event, Calendar currentMonth) {

        StreamingServiceDemographicTransaction transaction = new StreamingServiceDemographicTransaction(this, event, currentMonth, demographic, demographicPercentage, -1, conn);
        BigDecimal transactionAmount = transaction.getTransactionAmount();
        if (transactionAmount.signum() > 0) {

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                String sql = "INSERT INTO ssd_transaction (streaming_service, event_id, month, transaction_amount, demographic_percentage, demographic) VALUES (?,?,to_timestamp(?),?,?,?)";
                PreparedStatement preparedStatement = conn.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, this.getShortName());
                preparedStatement.setInt(2, event.getEventsOrderCtr());
                preparedStatement.setDouble(3, currentMonth.getTimeInMillis() / 1000);
                preparedStatement.setInt(4, transactionAmount.intValue());
                preparedStatement.setInt(5, demographicPercentage);
                preparedStatement.setString(6, demographic.getShortName());
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows != 0) {
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            transaction.setId(generatedKeys.getInt(1));
                            return transaction;
                        } else {
                            throw new SQLException("Creating transaction failed, no ID obtained.");
                        }
                    }
                }
                preparedStatement.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void clearOfferings() {
        offerings.clear();
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public int getSubscriptionPrice() {
        return subscriptionPrice;
    }

    public ArrayList<Offering> getOfferings() {
        return (ArrayList<Offering>) offerings.clone();
    }

    public ArrayList<Transaction> getTransactions() {
        return (ArrayList<Transaction>) transactions.clone();
    }

    public void setSubscriptionPrice(int setSubscriptionPrice) {
        this.subscriptionPrice = setSubscriptionPrice;
    }
}