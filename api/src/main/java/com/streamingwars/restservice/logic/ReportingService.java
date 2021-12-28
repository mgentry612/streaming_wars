package com.streamingwars.restservice.logic;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.sql.*;

public class ReportingService {

    private StreamingManager streamingManager;
	private Connection conn;

    public ReportingService(StreamingManager streamingManager, Connection conn) {
        this.streamingManager = streamingManager;

        this.conn = conn;
    }

    public Report displayDemographic(String demographicName) {

        Demographic demographic = streamingManager.getDemographicFromName(demographicName);
        if (demographic == null) {
            throw new IllegalArgumentException("Demographic not found.");
        }

        BigDecimal currentMonthRevenue = getDemographicRevenueByMonth(demographic, streamingManager.getCurrentMonth());

        Calendar previousMonth = streamingManager.getCurrentMonth();
        previousMonth.add(Calendar.MONTH, -1);
        BigDecimal previousMonthRevenue = getDemographicRevenueByMonth(demographic, previousMonth);

        BigDecimal totalRevenue = getTotalDemographicRevenueExceptMonth(demographic, streamingManager.getCurrentMonth());

        System.out.println("current_period," + currentMonthRevenue);
        System.out.println("previous_period," + previousMonthRevenue);
        System.out.println("total," + totalRevenue);

        return new Report(currentMonthRevenue, previousMonthRevenue, totalRevenue);
    }

    private BigDecimal getDemographicRevenueByMonth(Demographic demographic, Calendar month) {
        BigDecimal monthRevenue = new BigDecimal(String.valueOf((int) 0));

        try {
            String sql = "SELECT transaction_amount FROM ssd_transaction WHERE date_trunc('month', month) = date_trunc('month',to_timestamp(?)::timestamp) AND demographic = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setDouble(1, month.getTimeInMillis() / 1000);
            preparedStatement.setString(2, demographic.getShortName());
            System.out.println(month.getTimeInMillis() / 1000);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                monthRevenue = monthRevenue.add(BigDecimal.valueOf(rs.getInt("transaction_amount")));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return monthRevenue;
    }

    private BigDecimal getTotalDemographicRevenueExceptMonth(Demographic demographic, Calendar exceptMonth) {
        BigDecimal totalRevenue = new BigDecimal(String.valueOf((int) 0));

        try {
            String sql = "SELECT transaction_amount FROM ssd_transaction WHERE date_trunc('month', month) < date_trunc('month',to_timestamp(?)::timestamp) AND demographic = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setDouble(1, exceptMonth.getTimeInMillis() / 1000);
            preparedStatement.setString(2, demographic.getShortName());

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                totalRevenue = totalRevenue.add(BigDecimal.valueOf(rs.getInt("transaction_amount")));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalRevenue;
    }

    private boolean isSameMonth(Transaction transaction, Calendar monthYear) {

        if (transaction.getMonth().get(Calendar.MONTH) == monthYear.get(Calendar.MONTH)
                && transaction.getMonth().get(Calendar.YEAR) == monthYear.get(Calendar.YEAR)) {
            return true;
        }

        return false;
    }

    public ArrayList<Event> displayEvents() {

        ArrayList<Event> events = new ArrayList<Event>();

        try {
            String sql = "SELECT * FROM event ORDER BY id ASC";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String type = rs.getString("event_type");
                Event event = null;
                if (type.equals("movie")) {
                    event = new Movie(rs.getString("name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), streamingManager.getStudioFromName(rs.getString("studio")), rs.getInt("id"));
                } else if (type.equals("ppv")) {
                    event = new PayPerView(rs.getString("name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), streamingManager.getStudioFromName(rs.getString("studio")), rs.getInt("id"));
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

    public StreamingServiceReport displayStream(String streamingServiceName) {
        StreamingService streamingService = streamingManager.getStreamingServiceFromName(streamingServiceName);
        if (streamingService == null) {
            throw new IllegalArgumentException("Invalid streaming service name.");
        }

        System.out.println("stream," + streamingService.getShortName() + "," + streamingService.getLongName());
        System.out.println("subscription," + streamingService.getSubscriptionPrice());

        BigDecimal currentMonthRevenue = getStreamingServiceRevenueByMonth(streamingService, streamingManager.getCurrentMonth());

        Calendar previousMonth = streamingManager.getCurrentMonth();
        previousMonth.add(Calendar.MONTH, -1);
        BigDecimal previousMonthRevenue = getStreamingServiceRevenueByMonth(streamingService, previousMonth);

        BigDecimal totalRevenue = getTotalStreamingServiceRevenueExceptMonth(streamingService, streamingManager.getCurrentMonth());

        System.out.println("current_period," + currentMonthRevenue);
        System.out.println("previous_period," + previousMonthRevenue);
        System.out.println("total," + totalRevenue);

        BigDecimal totalCost = getTotalStreamingServiceCost(streamingService);
        System.out.println("licensing," + totalCost);

        return new StreamingServiceReport(currentMonthRevenue, previousMonthRevenue, totalRevenue, totalCost);
    }

    private BigDecimal getStreamingServiceRevenueByMonth(StreamingService streamingService, Calendar month) {
        BigDecimal monthRevenue = new BigDecimal(String.valueOf((int) 0));

        try {
            String sql = "SELECT transaction_amount FROM ssd_transaction WHERE date_trunc('month', month) = date_trunc('month',to_timestamp(?)::timestamp) AND streaming_service = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setDouble(1, month.getTimeInMillis() / 1000);
            preparedStatement.setString(2, streamingService.getShortName());

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                monthRevenue = monthRevenue.add(BigDecimal.valueOf(rs.getInt("transaction_amount")));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return monthRevenue;
    }

    private BigDecimal getTotalStreamingServiceRevenueExceptMonth(StreamingService streamingService, Calendar exceptMonth) {
        BigDecimal totalRevenue = new BigDecimal(String.valueOf((int) 0));

        try {
            String sql = "SELECT transaction_amount FROM ssd_transaction WHERE date_trunc('month', month) < date_trunc('month',to_timestamp(?)::timestamp) AND streaming_service = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setDouble(1, exceptMonth.getTimeInMillis() / 1000);
            preparedStatement.setString(2, streamingService.getShortName());

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                totalRevenue = totalRevenue.add(BigDecimal.valueOf(rs.getInt("transaction_amount")));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalRevenue;
    }

    private BigDecimal getTotalStreamingServiceCost(StreamingService streamingService) {
        BigDecimal totalCost = new BigDecimal(String.valueOf((int) 0));

        try {
            String sql = "SELECT transaction_amount FROM sss_transaction WHERE streaming_service = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, streamingService.getShortName());

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                totalCost = totalCost.add(BigDecimal.valueOf(rs.getInt("transaction_amount")));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalCost;
    }

    public Report displayStudio(String studioName) {
        Studio studio = streamingManager.getStudioFromName(studioName);
        if (studio == null) {
            throw new IllegalArgumentException("Invalid streaming service name.");
        }

        System.out.println("studio," + studio.getShortName() + "," + studio.getLongName());

        BigDecimal currentMonthRevenue = getStudioRevenueByMonth(studio, streamingManager.getCurrentMonth());

        Calendar previousMonth = streamingManager.getCurrentMonth();
        previousMonth.add(Calendar.MONTH, -1);
        BigDecimal previousMonthRevenue = getStudioRevenueByMonth(studio, previousMonth);

        BigDecimal totalRevenue = getTotalStudioRevenueExceptMonth(studio, streamingManager.getCurrentMonth());

        System.out.println("current_period," + currentMonthRevenue);
        System.out.println("previous_period," + previousMonthRevenue);
        System.out.println("total," + totalRevenue);

        return new Report(currentMonthRevenue, previousMonthRevenue, totalRevenue);
    }

    private BigDecimal getStudioRevenueByMonth(Studio studio, Calendar month) {
        
        BigDecimal monthRevenue = new BigDecimal(String.valueOf((int) 0));

        try {
            String sql = "SELECT transaction_amount FROM sss_transaction "
                + "JOIN event ON sss_transaction.event_id = event.id "
                + "WHERE event.studio = ? "
                + "AND date_trunc('month', sss_transaction.month) = date_trunc('month',to_timestamp(?)::timestamp)";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, studio.getShortName());
            preparedStatement.setDouble(2, month.getTimeInMillis() / 1000);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                monthRevenue = monthRevenue.add(BigDecimal.valueOf(rs.getInt("transaction_amount")));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return monthRevenue;


        // BigDecimal monthRevenue = new BigDecimal(String.valueOf((int) 0));
        // for (StreamingService streamingService : streamingManager.getStreamingServices()) {
        //     for (Transaction transaction : streamingService.getTransactions()) {
        //         if (transaction instanceof StudioStreamingServiceTransaction
        //                 && isSameMonth(transaction, month)
        //                 && studio == transaction.getEvent().getStudio()) {

        //             BigDecimal transactionAmount = transaction.getTransactionAmount();
        //             monthRevenue = monthRevenue.add(transactionAmount);
        //         }
        //     }
        // }
        // return monthRevenue;
    }

    private BigDecimal getTotalStudioRevenueExceptMonth(Studio studio, Calendar exceptMonth) {
        
        BigDecimal totalRevenue = new BigDecimal(String.valueOf((int) 0));

        try {
            String sql = "SELECT transaction_amount FROM sss_transaction "
                + "JOIN event ON sss_transaction.event_id = event.id "
                + "WHERE event.studio = ? "
                + "AND date_trunc('month', sss_transaction.month) < date_trunc('month',to_timestamp(?)::timestamp)";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, studio.getShortName());
            preparedStatement.setDouble(2, exceptMonth.getTimeInMillis() / 1000);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                totalRevenue = totalRevenue.add(BigDecimal.valueOf(rs.getInt("transaction_amount")));
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalRevenue;


        // BigDecimal totalRevenue = new BigDecimal(String.valueOf((int) 0));
        // for (StreamingService streamingService : streamingManager.getStreamingServices()) {
        //     for (Transaction transaction : streamingService.getTransactions()) {
        //         if (transaction instanceof StudioStreamingServiceTransaction
        //                 && !isSameMonth(transaction, exceptMonth)
        //                 && studio == transaction.getEvent().getStudio()) {

        //             BigDecimal transactionAmount = transaction.getTransactionAmount();
        //             totalRevenue = totalRevenue.add(transactionAmount);
        //         }
        //     }
        // }
        // return totalRevenue;
    }

    public ArrayList<Offering> displayOfferings() {

        ArrayList<Offering> offerings = new ArrayList<Offering>();

        try {
            String sql = "SELECT "
                + "sss_transaction.streaming_service, "
                + "sss_transaction.transaction_amount as license_fee, "
                + "date_part('month', sss_transaction.month)::int - 1 as month, "
                + "date_part('year', sss_transaction.month)::int - 1 as year, "
                + "sss_transaction.id as sss_transaction_id, "
                + "event.event_type, "
                + "event.name as event_name, "
                + "event.year_produced, "
                + "event.duration, "
                + "event.studio, "
                + "event.id as event_id, "
                + "offering.id as offering_id, "
                + "offering.viewing_price "
                + "FROM offering "
                + "JOIN sss_transaction ON sss_transaction.id = offering.sss_transaction_id "
                + "JOIN event ON event.id = sss_transaction.event_id "
                + "ORDER BY offering.id ASC";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                
                String type = rs.getString("event_type");
                Offering offering = null;
                if (type.equals("movie")) {
                    Movie movie = new Movie(rs.getString("event_name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), streamingManager.getStudioFromName(rs.getString("studio")), rs.getInt("event_id"));
                    
                    Calendar cal = Calendar.getInstance();
                    cal.set(rs.getInt("year"), rs.getInt("month"), 1, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    StudioStreamingServiceTransaction transaction = new StudioStreamingServiceTransaction(streamingManager.getStreamingServiceFromName(rs.getString("streaming_service")), movie, cal, rs.getInt("sss_transaction_id"));

                    offering = new MovieOffering(movie, transaction, rs.getInt("offering_id"));
                } else if (type.equals("ppv")) {
                    PayPerView payPerView = new PayPerView(rs.getString("event_name"), rs.getInt("year_produced"), rs.getInt("duration"), rs.getInt("license_fee"), streamingManager.getStudioFromName(rs.getString("studio")), rs.getInt("event_id"));
                    
                    Calendar cal = Calendar.getInstance();
                    cal.set(rs.getInt("year"), rs.getInt("month"), 1, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    StudioStreamingServiceTransaction transaction = new StudioStreamingServiceTransaction(streamingManager.getStreamingServiceFromName(rs.getString("streaming_service")), payPerView, cal, rs.getInt("sss_transaction_id"));

                    offering = new PayPerViewOffering(payPerView, transaction, rs.getInt("offering_id"), rs.getInt("viewing_price"));
                }
                offerings.add(offering);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offerings;



        // ArrayList<Offering> allOfferings = new ArrayList<Offering>();
        // for (StreamingService streamingService : streamingManager.getStreamingServices()) {
        //     for (Offering offering : streamingService.getOfferings()) {
        //         allOfferings.add(offering);
        //     }
        // }

        // Collections.sort(allOfferings, (a, b) -> {
        //     if (a.getOfferingsOrderCtr() > b.getOfferingsOrderCtr()) {
        //         return 1;
        //     } else if (a.getOfferingsOrderCtr() < b.getOfferingsOrderCtr()) {
        //         return -1;
        //     } else {
        //         return 0;
        //     }
        // });

        // String type;
        // String viewingPrice;
        // for (Offering offering : allOfferings) {
        //     Event event = offering.getTransaction().getEvent();
        //     if (event instanceof Movie) {
        //         type = "movie";
        //         viewingPrice = "";
        //     } else {
        //         type = "ppv";
        //         viewingPrice = "," + ((PayPerViewOffering) offering).getViewingPrice();
        //     }

        //     System.out.println(offering.getTransaction().getStreamingService().getShortName() + "," + type + "," + event.getName() + "," + event.getYearProduced() + viewingPrice);
        // }
    }

    public TimeResponse displayTime() {
        return new TimeResponse((streamingManager.getCurrentMonth().get(Calendar.MONTH) + 1) + "," + streamingManager.getCurrentMonth().get(Calendar.YEAR));
    }

}