package com.bazarfx.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Core domain class for a registered user.
 * Every user can act as both a buyer and a seller (Bikroy-style, single role).
 */
public class User implements Serializable {

    private String username;
    private String email;
    private String phone;
    private String location;
    private String passwordHash;
    private String joinDate;

    public User() {
        // required by Gson for deserialization
    }

    public User(String username, String email, String phone, String location, String passwordHash) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.passwordHash = passwordHash;
        this.joinDate = LocalDate.now().toString();
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public String getPasswordHash() { return passwordHash; }
    public String getJoinDate() { return joinDate; }

    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLocation(String location) { this.location = location; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
