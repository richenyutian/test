package com.example.ticketsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ticket")
public class DemoUserProperties {

    private List<String> demoUsers = new ArrayList<>();

    public List<String> getDemoUsers() {
        return demoUsers;
    }

    public void setDemoUsers(List<String> demoUsers) {
        this.demoUsers = demoUsers;
    }
}
