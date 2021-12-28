package com.streamingwars.restservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamingwars.restservice.logic.LoginRequest;
import com.streamingwars.restservice.logic.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
@CrossOrigin
public class LoginController {
    private Connection conn;
    private ObjectMapper objectMapper;
    private String audit_sql = "INSERT INTO audit_log (action_type, action_content, username, created_date) VALUES (?, ?, ?, ?)";

    public LoginController() {
        try {
            String url = "jdbc:postgresql://streamingwars_db:5432/postgres?user=postgres&password=postgres";
            conn = DriverManager.getConnection(url);
            objectMapper = new ObjectMapper();
            System.out.println("LoginController Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @PostMapping("/users/signin")
    public UserDetails login(@RequestBody LoginRequest loginRequest)  {
        UserDetails userDetails = null;
        String actionType = "login";
        try {
            String sql = "SELECT * FROM user_info WHERE user_name=? AND user_pw=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, loginRequest.getUsername());
            preparedStatement.setString(2, loginRequest.getPassword());

            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()) {
                userDetails = new UserDetails();
                userDetails.setUsername(rs.getString("user_name"));
                userDetails.setRoles(rs.getString("roles"));
                String recordStr = objectMapper.writeValueAsString(userDetails);
                insertAuditLog(actionType, recordStr, userDetails.getUsername());
            }
            rs.close();
            preparedStatement.close();
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return userDetails;
    }

    private void insertAuditLog(String actionType, String actionContent, String username) {
        try {
            OffsetDateTime offsetDateTime = ZonedDateTime.now(ZoneId.of("America/New_York")).toOffsetDateTime();
            PreparedStatement auditStatement = conn.prepareStatement(audit_sql);
            auditStatement.setString(1, actionType);
            auditStatement.setString(2, actionContent);
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
