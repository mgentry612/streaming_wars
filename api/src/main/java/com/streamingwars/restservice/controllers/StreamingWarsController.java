package com.streamingwars.restservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamingwars.restservice.logic.*;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import com.google.gson.Gson; 

@RestController
@CrossOrigin
public class StreamingWarsController {

    private final StreamingManager streamingManager = new StreamingManager();
	private Connection conn;
	private Jedis jedis;
	private Gson gson;
	private ObjectMapper objectMapper;
	private String audit_sql = "INSERT INTO audit_log (action_type, action_content, username, created_date) VALUES (?, ?, ?, ?)";

	public StreamingWarsController() {
		try {
			String url = "jdbc:postgresql://streamingwars_db:5432/postgres?user=postgres&password=postgres";
			conn = DriverManager.getConnection(url);
			objectMapper = new ObjectMapper();
			System.out.println("StreamingWarsController Connected to the PostgreSQL server successfully.");

			jedis = new Jedis("redis");
			gson = new Gson();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private String argsToHash(ArrayList<String> args) {
		String combinedArgs = String.join(",", args);
		int hash = combinedArgs.hashCode();
		return String.valueOf(hash);
	}

	@GetMapping("/demographic")
	public Demographic getDemographic(@RequestParam(value = "short_name") String shortName, @RequestParam(value = "x-loggedin-user") String loggedInUser) {

		// Check if results already in cache
		ArrayList<String> argsList = new ArrayList<>();
		argsList.add(shortName);

		Demographic demographic = null;
		String hash = argsToHash(argsList);
		String value = jedis.get(hash);
		if (value != null) {
			// retrieve from cache
			demographic = gson.fromJson(value, Demographic.class);
		} else {
			// retrieve from db
			demographic = streamingManager.getDemographicFromName(shortName);
			String json = gson.toJson(demographic);
			// store in cache
			jedis.set(hash, json);
		}

		insertAuditLog("getDemographic", demographic, loggedInUser);
        return demographic;
	}

	@GetMapping("/demographics")
	public ArrayList<Demographic> getDemographics(@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		ArrayList<Demographic> demographics = streamingManager.getAllDemographics();
		insertAuditLog("getDemographic", demographics, loggedInUser);
		return demographics;
	}

	@PostMapping("/demographic")
	public Demographic postDemographic(@RequestParam(value = "short_name") String shortName, @RequestParam(value = "long_name") String longName, @RequestParam(value = "num_accounts") String numAccounts, @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		Demographic createdDemographic = streamingManager.createDemographic(shortName, longName, Integer.parseInt(numAccounts));
		insertAuditLog("postDemographic", createdDemographic, loggedInUser);
        return createdDemographic;
	}

	@PutMapping("/demographic")
	public Demographic putDemographic(@RequestParam(value = "short_name") String shortName, @RequestParam(value = "long_name") String longName, @RequestParam(value = "num_accounts") String numAccounts,@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		Demographic updatedDemographic = streamingManager.updateDemographic(shortName, longName, Integer.parseInt(numAccounts));
		insertAuditLog("putDemographic", updatedDemographic, loggedInUser);

		// Update cache
		ArrayList<String> argsList = new ArrayList<>();
		argsList.add(shortName);
		String hash = argsToHash(argsList);
		if (jedis.get(hash) != null) {
			String json = gson.toJson(updatedDemographic);
			jedis.set(hash, json);
		}

        return updatedDemographic;
	}

	@GetMapping("/studio")
	public Studio getStudio(@RequestParam(value = "short_name") String shortName,
							@RequestParam(value = "x-loggedin-user") String loggedInUser) {

		ArrayList<String> argsList = new ArrayList<>();
		argsList.add(shortName);

		Studio studio = null;
		String hash = argsToHash(argsList);
		String value = jedis.get(hash);
		if (value != null) {
			studio = gson.fromJson(value, Studio.class);
		} else {
			studio = streamingManager.getStudioFromName(shortName);
			String json = gson.toJson(studio);
			jedis.set(hash, json);
		}

		insertAuditLog("getStudio", studio, loggedInUser);
        return studio;
	}

	@GetMapping("/studios")
	public ArrayList<Studio> getStudios(@RequestParam(value = "x-loggedin-user") String loggedInUser) {
        ArrayList<Studio> studios = streamingManager.getAllStudios();
		insertAuditLog("getStudios", studios, loggedInUser);
		return studios;
	}

	@PostMapping("/studio")
	public Studio postStudio(@RequestParam(value = "short_name") String shortName, @RequestParam(value = "long_name") String longName,
							 @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		Studio studio = streamingManager.createStudio(shortName, longName);
		insertAuditLog("postStudio", studio, loggedInUser);
        return studio;
	}

	@GetMapping("/streaming_service")
	public StreamingService getStream(@RequestParam(value = "short_name") String shortName,
									  @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		StreamingService streamingService = streamingManager.getStreamingServiceFromName(shortName);
		insertAuditLog("getStream", streamingService, loggedInUser);
        return streamingService;
	}

	@GetMapping("/streaming_services")
	public ArrayList<StreamingService> getStreamingServices(@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		ArrayList<StreamingService> streamingServices = streamingManager.getAllStreamingServices();
		insertAuditLog("getStreamingServices", streamingServices, loggedInUser);
		return streamingServices;
	}

	@PostMapping("/streaming_service")
	public StreamingService postStream(@RequestParam(value = "short_name") String shortName, @RequestParam(value = "long_name") String longName, @RequestParam(value = "subscription_price") String subscriptionPrice,
									   @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		StreamingService streamingService = streamingManager.createStreamingService(shortName, longName, Integer.parseInt(subscriptionPrice));
		insertAuditLog("postStream", streamingService, loggedInUser);
        return streamingService;
	}

	@PutMapping("/streaming_service")
	public StreamingService putStream(@RequestParam(value = "short_name") String shortName, @RequestParam(value = "long_name") String longName, @RequestParam(value = "subscription_price") String subscriptionPrice,
									  @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		StreamingService streamingService = streamingManager.updatetreamingService(shortName, longName, Integer.parseInt(subscriptionPrice));
		insertAuditLog("putStream", streamingService, loggedInUser);
        return streamingService;
	}

	@GetMapping("/event")
	public Event getEvent(@RequestParam(value = "short_name") String name, @RequestParam(value = "year_produced") String yearProduced,
						  @RequestParam(value = "x-loggedin-user") String loggedInUser) {

		ArrayList<String> argsList = new ArrayList<>();
		argsList.add(name);
		argsList.add(yearProduced);

		Event event = null;
		String hash = argsToHash(argsList);
		String value = jedis.get(hash);
		if (value != null) {
			event = gson.fromJson(value, Event.class);
		} else {
			event = streamingManager.getEventFromNameAndYear(name,  Integer.parseInt(yearProduced));
			String json = gson.toJson(event);
			jedis.set(hash, json);
		}

		insertAuditLog("getEvent", event, loggedInUser);
        return event;
	}

	@GetMapping("/event/studio")
	public ArrayList<Event> getEventFromStudio(@RequestParam(value = "studio") String studio) {
        return streamingManager.getEventFromStudio(studio);
	}

	@GetMapping("/event/streaming_service")
	public ArrayList<Event> getEventFromStreamingService(@RequestParam(value = "streaming_service") String streamingService) {
        return streamingManager.getEventFromStreamingService(streamingService);
	}

	@PostMapping("/event")
	public Event postEvent(@RequestParam(value = "type") String type, @RequestParam(value = "name") String name, @RequestParam(value = "year_produced") String yearProduced, @RequestParam(value = "duration") String duration, @RequestParam(value = "studio_name") String studioName, @RequestParam(value = "license_fee") String licenseFee,
						   @RequestParam(value = "x-loggedin-user") String loggedInUser) {

        if (type.equals("movie")) {
        	Event movie = streamingManager.createMovie(name, Integer.parseInt(yearProduced), Integer.parseInt(duration), studioName, Integer.parseInt(licenseFee));
			insertAuditLog("postEvent", movie, loggedInUser);
            return movie;
		} else if (type.equals("ppv")) {
        	Event ppv = streamingManager.createPayPerView(name, Integer.parseInt(yearProduced), Integer.parseInt(duration), studioName, Integer.parseInt(licenseFee));
			insertAuditLog("postEvent", ppv, loggedInUser);
            return ppv;
		}

		return null;
	}

	@PutMapping("/event")
	public Event putEvent(@RequestParam(value = "type") String type, @RequestParam(value = "name") String name, @RequestParam(value = "year_produced") String yearProduced, @RequestParam(value = "duration") String duration, @RequestParam(value = "studio_name") String studioName, @RequestParam(value = "license_fee") String licenseFee,
						  @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		Event updatedEvent = streamingManager.updateEvent(name, Integer.parseInt(yearProduced), Integer.parseInt(duration), studioName, Integer.parseInt(licenseFee));
		insertAuditLog("putEvent", updatedEvent, loggedInUser);

		// Update cache
		ArrayList<String> argsList = new ArrayList<>();
		argsList.add(name);
		argsList.add(yearProduced);
		String hash = argsToHash(argsList);
		if (jedis.get(hash) != null) {
			String json = gson.toJson(updatedEvent);
			jedis.set(hash, json);
		}
		return updatedEvent;
	}

	@PostMapping("/offer_movie")
	public MovieOffering offerMovie(@RequestParam(value = "streaming_service_name") String streamingServiceName, @RequestParam(value = "movie_name") String movieName, @RequestParam(value = "year_produced") String yearProduced,
									@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		MovieOffering movieOffering = streamingManager.offerMovie(streamingServiceName, movieName, Integer.parseInt(yearProduced));
		insertAuditLog("offerMovie", movieOffering, loggedInUser);
		return movieOffering;
	}

	@PostMapping("/offer_ppv")
	public PayPerViewOffering offerPPV(@RequestParam(value = "streaming_service_name") String streamingServiceName, @RequestParam(value = "ppv_name") String ppvName, @RequestParam(value = "year_produced") String yearProduced, @RequestParam(value = "viewing_price") String viewingPrice,
									   @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		PayPerViewOffering payPerViewOffering = streamingManager.offerPayPerView(streamingServiceName, ppvName, Integer.parseInt(yearProduced), Integer.parseInt(viewingPrice));
		insertAuditLog("offerPPV", payPerViewOffering, loggedInUser);
        return payPerViewOffering;
	}

	@DeleteMapping("/offer")
	public Offering deleteOffer(@RequestParam(value = "streaming_service_name") String streamingServiceName, @RequestParam(value = "movie_name") String movieName, @RequestParam(value = "year_produced") String yearProduced,
								@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		Offering deletedOffer = streamingManager.deleteOffer(streamingServiceName, movieName, Integer.parseInt(yearProduced));
		insertAuditLog("deleteOffer", deletedOffer, loggedInUser);
        return deletedOffer;
	}

	@PostMapping("/watch_event")
	public StreamingServiceDemographicTransaction watchEvent(@RequestParam(value = "demographic") String demographicName, @RequestParam(value = "demographic_percentage") String demographicPercentage, @RequestParam(value = "streaming_service") String streamingServiceName, @RequestParam(value = "event_name") String eventName, @RequestParam(value = "event_year_produced") String eventYearProduced,
														   @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		StreamingServiceDemographicTransaction ssdTransaction = streamingManager.watchEvent(demographicName, Integer.parseInt(demographicPercentage), streamingServiceName, eventName, Integer.parseInt(eventYearProduced));
		insertAuditLog("watchEvent", ssdTransaction, loggedInUser);
        return ssdTransaction;
	}

	@GetMapping("/report/demographic")
	public Report getReportDemographic(@RequestParam(value = "demographic_name") String demographicName,
									   @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		Report report = streamingManager.displayDemographic(demographicName);
		insertAuditLog("getReportDemographic", report, loggedInUser);
        return report;
	}

	@GetMapping("/report/events")
	public ArrayList<Event> getReportEvents(@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		ArrayList<Event> eventsReport = streamingManager.displayEvents();
		insertAuditLog("getReportEvents", eventsReport, loggedInUser);
		return eventsReport;
	}

	@GetMapping("/report/streaming_service")
	public StreamingServiceReport getReportStreamingService(@RequestParam(value = "name") String streamingService,
															@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		StreamingServiceReport streamingServiceReport = streamingManager.displayStream(streamingService);
		insertAuditLog("getReportStreamingService", streamingServiceReport, loggedInUser);
        return streamingServiceReport;
	}

	@GetMapping("/report/studio")
	public Report getReportStudio(@RequestParam(value = "name") String studio,
								  @RequestParam(value = "x-loggedin-user") String loggedInUser) {
		Report report = streamingManager.displayStudio(studio);
		insertAuditLog("getReportStudio", report, loggedInUser);
        return report;
	}

	@GetMapping("/report/offerings")
	public ArrayList<Offering> getReportOfferings(@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		ArrayList<Offering> offeringsReport = streamingManager.displayOfferings();
		insertAuditLog("getReportOfferings", offeringsReport, loggedInUser);
		return offeringsReport;
	}

	@GetMapping("/time")
	public TimeResponse displayTime(@RequestParam(value = "x-loggedin-user") String loggedInUser) {
        TimeResponse timeResponse = streamingManager.displayTime();
		insertAuditLog("displayTime", timeResponse, loggedInUser);
		return timeResponse;
	}

	@PostMapping("/time")
	public TimeResponse incrementMonth(@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		TimeResponse timeResponse = streamingManager.incrementMonth();
		insertAuditLog("incrementMonth", timeResponse, loggedInUser);
        return timeResponse;
	}

	@GetMapping("/audit_logs")
	public ArrayList<AuditLogEntry> fetchAuditLogEntries(@RequestParam(value = "username") String username,
											@RequestParam(value = "x-loggedin-user") String loggedInUser) {
		ArrayList<AuditLogEntry> logEntries = streamingManager.getAuditLogEntriesByUser(username);
		String actionContent = loggedInUser + " queried audit logs for " + username;
		insertAuditLog("fetchAuditLogEntries", actionContent, loggedInUser);
		return logEntries;
	}

	private void insertAuditLog(String actionType, Object actionContent, String username) {
		try {
			OffsetDateTime offsetDateTime = ZonedDateTime.now(ZoneId.of("America/New_York")).toOffsetDateTime();
			PreparedStatement auditStatement = conn.prepareStatement(audit_sql);
			auditStatement.setString(1, actionType);
			auditStatement.setString(2, objectMapper.writeValueAsString(actionContent));
			auditStatement.setString(3, username);
			auditStatement.setString(4, offsetDateTime.toString());
			auditStatement.execute();
			auditStatement.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
