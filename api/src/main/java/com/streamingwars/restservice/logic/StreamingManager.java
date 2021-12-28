package com.streamingwars.restservice.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.sql.*;

public class StreamingManager {

    private Calendar currentMonth;
    private ArrayList<Demographic> demographics;
    private ArrayList<Studio> studios;
    private ArrayList<StreamingService> streamingServices;
    private int eventsOrderCtr = 0;
    private int offeringsOrderCtr = 0;
    private ReportingService reportingService;
	private Connection conn;

    public StreamingManager() {
        demographics = new ArrayList<Demographic>();

        studios = new ArrayList<Studio>();

        streamingServices = new ArrayList<StreamingService>();

        Calendar cal = Calendar.getInstance();
        cal.set(2020, 9, 1, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        currentMonth = cal;
        
		try {
			String url = "jdbc:postgresql://streamingwars_db:5432/postgres?user=postgres&password=postgres";
			conn = DriverManager.getConnection(url);
            System.out.println("StreamingManager Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        reportingService = new ReportingService(this, conn);
    }

    public TimeResponse incrementMonth() {
        currentMonth.add(Calendar.MONTH, 1);

        try {
            String sql = "DELETE FROM offering;";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reportingService.displayTime();
    }

    public Demographic createDemographic(String shortName, String longName, int numAccounts) {

        Demographic demographic = null;
        try {
            String sql = "INSERT INTO demographic (short_name, long_name, num_accounts) VALUES (?,?,?) ON CONFLICT (short_name) DO NOTHING";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, shortName);
            preparedStatement.setString(2, longName);
            preparedStatement.setInt(3, numAccounts);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                demographic = new Demographic(shortName, longName, numAccounts);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return demographic;
    }

    public Demographic updateDemographic(String shortName, String longName, int numAccounts) {

        Demographic demographic = null;
        try {
            String sql = "UPDATE demographic SET long_name = ? WHERE short_name = ? RETURNING *";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, longName);
            preparedStatement.setString(2, shortName);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();
            while (rs.next()) {
                demographic = new Demographic(shortName, longName, rs.getInt("num_accounts"));
            }
            rs.close();
            System.out.println("test");
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (demographic != null) {
            try {
                String sql = "UPDATE demographic SET num_accounts = ? WHERE short_name = ? AND (SELECT count(*) from ssd_transaction WHERE date_trunc('month', month) = date_trunc('month',to_timestamp(?)::timestamp) AND demographic = ?) = 0";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setInt(1, numAccounts);
                preparedStatement.setString(2, shortName);
                preparedStatement.setDouble(3, currentMonth.getTimeInMillis() / 1000);
                preparedStatement.setString(4, shortName);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows != 0) {
                    demographic.setNumAccounts(numAccounts);
                }
                preparedStatement.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return demographic;
    }

    public Demographic getDemographicFromName(String name) {

        Demographic demographic = null;
        try {
            String sql = "SELECT * FROM demographic WHERE short_name=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                demographic = new Demographic(rs.getString("short_name"), rs.getString("long_name"), rs.getInt("num_accounts"));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return demographic;
    }

    public ArrayList<AuditLogEntry> getAuditLogEntriesByUser(String username) {
        ArrayList<AuditLogEntry> logEntries = new ArrayList<>();
        try {
            String sql = "SELECT * FROM audit_log WHERE username=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()) {
                AuditLogEntry auditLogEntry = new AuditLogEntry(rs.getString("action_type"),
                        rs.getString("action_content"), rs.getString("created_date"));
                logEntries.add(auditLogEntry);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logEntries;
    }

    public Studio createStudio(String shortName, String longName) {

        try {
            String sql = "INSERT INTO studio (short_name, long_name) VALUES (?,?) ON CONFLICT (short_name) DO NOTHING";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, shortName);
            preparedStatement.setString(2, longName);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Studio(shortName, longName);
    }

    public Movie createMovie(String name, int yearProduced, int duration, String studioName, int licenseFee) {

        // Get Studio
        Studio studio = getStudioFromName(studioName);
        if (studio == null) {
            throw new IllegalArgumentException("Invalid studio name.");
        }

        if (getEventFromNameAndYear(name, yearProduced) != null) {
            throw new IllegalArgumentException("Event name and year produced must be unique.");
        }

        // Insert event
        Movie movie = null;
        try {
            String sql = "INSERT INTO event (name, year_produced, duration, license_fee, studio, event_type) VALUES (?,?,?,?,?,?) ON CONFLICT (name, year_produced) DO NOTHING";
            PreparedStatement preparedStatement = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, yearProduced);
            preparedStatement.setInt(3, duration);
            preparedStatement.setInt(4, licenseFee);
            preparedStatement.setString(5, studioName);
            preparedStatement.setString(6, "movie");
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        movie = new Movie(name, yearProduced, duration, licenseFee, studio, generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return movie;
    }

    public PayPerView createPayPerView(String name, int yearProduced, int duration, String studioName, int licenseFee) {

        // Get Studio
        Studio studio = getStudioFromName(studioName);
        if (studio == null) {
            throw new IllegalArgumentException("Invalid studio name.");
        }

        if (getEventFromNameAndYear(name, yearProduced) != null) {
            throw new IllegalArgumentException("Event name and year produced must be unique.");
        }

        // Insert event
        PayPerView payPerView = null;
        try {
            String sql = "INSERT INTO event (name, year_produced, duration, license_fee, studio, event_type) VALUES (?,?,?,?,?,?) ON CONFLICT (name, year_produced) DO NOTHING";
            PreparedStatement preparedStatement = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, yearProduced);
            preparedStatement.setInt(3, duration);
            preparedStatement.setInt(4, licenseFee);
            preparedStatement.setString(5, studioName);
            preparedStatement.setString(6, "ppv");
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payPerView = new PayPerView(name, yearProduced, duration, licenseFee, studio, generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return payPerView;
    }
    
    public Event updateEvent(String name, int yearProduced, int duration, String studioName, int licenseFee) {

        Event event = null;
        try {
            String sql = "UPDATE event SET duration = ? WHERE name = ? AND year_produced = ? RETURNING *";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, duration);
            preparedStatement.setString(2, name);
            preparedStatement.setInt(3, yearProduced);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();
            while (rs.next()) {
                event = getEventFromNameAndYear(name, yearProduced);
            }
            rs.close();
            System.out.println("test");
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (event != null) {
            try {
                String sql = "UPDATE event SET license_fee = ? WHERE name = ? AND event.year_produced = ? AND (SELECT count(*) from ssd_transaction "
                + "JOIN event ON event.id = ssd_transaction.event_id "
                + "WHERE date_trunc('month', ssd_transaction.month) = date_trunc('month',to_timestamp(?)::timestamp) AND event.name = ? AND event.year_produced = ?) = 0";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setInt(1, licenseFee);
                preparedStatement.setString(2, name);
                preparedStatement.setInt(3, yearProduced);
                preparedStatement.setDouble(4, currentMonth.getTimeInMillis() / 1000);
                preparedStatement.setString(5, name);
                preparedStatement.setInt(6, yearProduced);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows != 0) {
                    event.setLicenseFee(licenseFee);
                }
                preparedStatement.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return event;
    }

    public Studio getStudioFromName(String studioName) {

        Studio studio = null;
        try {
            String sql = "SELECT * FROM studio WHERE short_name=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, studioName);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                studio = new Studio(rs.getString("short_name"), rs.getString("long_name"));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return studio;
    }

    public ArrayList<Demographic> getAllDemographics(){

        ArrayList<Demographic> demographics = new ArrayList<Demographic>();
        try {
            String sql = "SELECT * FROM demographic";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Demographic demographic = new Demographic(rs.getString("short_name"), rs.getString("long_name"),rs.getInt("num_accounts"));
                demographics.add(demographic);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return demographics;
    }

    public ArrayList<Studio> getAllStudios() {

        ArrayList<Studio> studios = new ArrayList<Studio>();
        try {
            String sql = "SELECT * FROM studio";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Studio studio = new Studio(rs.getString("short_name"), rs.getString("long_name"));
                studios.add(studio);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return studios;
    }

    public Event getEventFromNameAndYear(String name, int yearProduced) {
        
        Event event = null;
        try {
            String sql = "SELECT * FROM event WHERE name=? AND year_produced=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, yearProduced);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String type = rs.getString("event_type");
                if (type.equals("movie")) {
                    event = new Movie(rs.getString("name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), getStudioFromName(rs.getString("studio")), rs.getInt("id"));
                } else if (type.equals("ppv")) {
                    event = new PayPerView(rs.getString("name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), getStudioFromName(rs.getString("studio")), rs.getInt("id"));
                }
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return event;
    }

    public ArrayList<Event> getEventFromStudio(String studioName) {
        
        ArrayList<Event> events = new ArrayList<Event>();
        try {
            String sql = "SELECT * FROM event WHERE studio=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, studioName);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String type = rs.getString("event_type");
                Event event = null;
                if (type.equals("movie")) {
                    event = new Movie(rs.getString("name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), getStudioFromName(rs.getString("studio")), rs.getInt("id"));
                    event.setEventType("movie");
                } else if (type.equals("ppv")) {
                    event = new PayPerView(rs.getString("name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), getStudioFromName(rs.getString("studio")), rs.getInt("id"));
                    event.setEventType("ppv");
                }
                events.add(event);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return events;
    }

    public ArrayList<Event> getEventFromStreamingService(String streamingServiceName) {
        
        ArrayList<Event> events = new ArrayList<Event>();
        try {
            String sql = "SELECT * FROM event "
                + "JOIN sss_transaction ON sss_transaction.event_id = event.id "
                + "WHERE sss_transaction.streaming_service = ? ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, streamingServiceName);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String type = rs.getString("event_type");
                Event event = null;
                if (type.equals("movie")) {
                    event = new Movie(rs.getString("name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), getStudioFromName(rs.getString("studio")), rs.getInt("id"));
                    event.setEventType("movie");
                } else if (type.equals("ppv")) {
                    event = new PayPerView(rs.getString("name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), getStudioFromName(rs.getString("studio")), rs.getInt("id"));
                    event.setEventType("ppv");
                }
                events.add(event);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return events;
    }

    public StreamingService createStreamingService(String shortName, String longName, int subscriptionPrice) {

        StreamingService streaming_service = null;
        try {
            String sql = "INSERT INTO streaming_service (short_name, long_name, subscription_price) VALUES (?,?,?) ON CONFLICT (short_name) DO NOTHING";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, shortName);
            preparedStatement.setString(2, longName);
            preparedStatement.setInt(3, subscriptionPrice);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                streaming_service = new StreamingService(shortName, longName, subscriptionPrice, conn);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return streaming_service;
    }

    public StreamingService updatetreamingService(String shortName, String longName, int subscriptionPrice) {

        StreamingService streamingService = null;
        try {
            String sql = "UPDATE streaming_service SET long_name = ? WHERE short_name = ? RETURNING *";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, longName);
            preparedStatement.setString(2, shortName);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();
            while (rs.next()) {
                streamingService = new StreamingService(shortName, longName, rs.getInt("subscription_price"), conn);
            }
            rs.close();
            System.out.println("test");
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (streamingService != null) {
            try {
                String sql = "UPDATE streaming_service SET subscription_price = ? WHERE short_name = ? AND (SELECT count(*) from ssd_transaction WHERE date_trunc('month', month) = date_trunc('month',to_timestamp(?)::timestamp) AND streaming_service = ?) = 0";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setInt(1, subscriptionPrice);
                preparedStatement.setString(2, shortName);
                preparedStatement.setDouble(3, currentMonth.getTimeInMillis() / 1000);
                preparedStatement.setString(4, shortName);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows != 0) {
                    streamingService.setSubscriptionPrice(subscriptionPrice);
                }
                preparedStatement.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return streamingService;
    }

    public MovieOffering offerMovie(String streamingServiceName, String movieName, int yearProduced) {

        StreamingService streamingService = getStreamingServiceFromName(streamingServiceName);
        if (streamingService == null) {
            throw new IllegalArgumentException("Invalid streaming service name.");
        }

        Event event = getEventFromNameAndYear(movieName, yearProduced);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        Offering offering = streamingService.getOfferingFromEvent(event);
        if (offering != null) {
            throw new IllegalArgumentException("Event already offered.");
        }

        return streamingService.offerMovie((Movie) event, (Calendar) currentMonth.clone(), offeringsOrderCtr++);
    }

    public PayPerViewOffering offerPayPerView(String streamingServiceName, String payPerViewName, int yearProduced, int viewingPrice) {

        StreamingService streamingService = getStreamingServiceFromName(streamingServiceName);
        if (streamingService == null) {
            throw new IllegalArgumentException("Invalid streaming service name.");
        }

        Event event = getEventFromNameAndYear(payPerViewName, yearProduced);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        Offering offering = streamingService.getOfferingFromEvent(event);
        if (offering != null) {
            throw new IllegalArgumentException("Event already offered.");
        }

        return streamingService.offerPayPerView((PayPerView) event, viewingPrice, (Calendar) currentMonth.clone(), offeringsOrderCtr++);
    }

    public Offering deleteOffer(String streamingServiceName, String movieName, int yearProduced) {

        Offering offering = null;
        try {
            String sql = "DELETE FROM offering WHERE sss_transaction_id = (SELECT sss_transaction.id FROM sss_transaction "
            + "JOIN event ON sss_transaction.event_id = event.id "
            + "WHERE event.name = ? AND year_produced = ? AND sss_transaction.streaming_service = ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, movieName);
            preparedStatement.setInt(2, yearProduced);
            preparedStatement.setString(3, streamingServiceName);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 0) {
                // streamingService = new Demographic(shortName, longName, rs.getInt("subscription_price"));
            }
            // rs.close();
            System.out.println("test");
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offering;
    }

    public StreamingService getStreamingServiceFromName(String streamingServiceName) {

        StreamingService streamingService = null;
        try {
            String sql = "SELECT * FROM streaming_service WHERE short_name=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, streamingServiceName);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                streamingService = new StreamingService(rs.getString("short_name"), rs.getString("long_name"), rs.getInt("subscription_price"), conn);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return streamingService;
    }

    public ArrayList<StreamingService> getAllStreamingServices() {

        ArrayList<StreamingService> streamingServices = new ArrayList<StreamingService>();
        try {
            String sql = "SELECT * FROM streaming_service";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                StreamingService streamingService = new StreamingService(rs.getString("short_name"), rs.getString("long_name"), rs.getInt("subscription_price"), conn);
                streamingServices.add(streamingService);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return streamingServices;
    }

    public StreamingServiceDemographicTransaction watchEvent(String demographicName, int demographicPercentage, String streamingServiceName, String eventName, int eventYearProduced) {
        Demographic demographic = getDemographicFromName(demographicName);
        if (demographic == null) {
            throw new IllegalArgumentException("Invalid demographic name.");
        }

        if (demographicPercentage <= 0) {
            throw new IllegalArgumentException("Demographic percentage must be greater than 0.");
        }

        StreamingService streamingService = getStreamingServiceFromName(streamingServiceName);
        if (streamingService == null) {
            throw new IllegalArgumentException("Invalid streaming service name.");
        }

        Event event = getEventFromNameAndYear(eventName, eventYearProduced);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        Offering offering = streamingService.getOfferingFromEvent(event);
        if (offering == null) {
            throw new IllegalArgumentException("Event is not offered by this streaming provider at this time.");
        }

        return streamingService.watchEvent(demographic, demographicPercentage, event, (Calendar) currentMonth.clone());
    }

    public Report displayDemographic(String demographicName) {
        return reportingService.displayDemographic(demographicName);
    }

    public ArrayList<Event> displayEvents() {
        return reportingService.displayEvents();
    }

    public StreamingServiceReport displayStream(String streamingServiceName) {
        return reportingService.displayStream(streamingServiceName);
    }

    public Report displayStudio(String studioName) {
        return reportingService.displayStudio(studioName);
    }

    public ArrayList<Offering> displayOfferings() {
        return reportingService.displayOfferings();
    }

    public TimeResponse displayTime() {
        return reportingService.displayTime();
    }

    public Calendar getCurrentMonth() {
        return (Calendar) currentMonth.clone();
    }

    public ArrayList<Demographic> getDemographics() {
        return (ArrayList<Demographic>) demographics.clone();
    }

    public ArrayList<Studio> getStudios() {
        return (ArrayList<Studio>) studios.clone();
    }

    public ArrayList<StreamingService> getStreamingServices() {
        return (ArrayList<StreamingService>) streamingServices.clone();
    }

    public int getEventsOrderCtr() {
        return eventsOrderCtr;
    }

    public int getOfferingsOrderCtr() {
        return offeringsOrderCtr;
    }
}