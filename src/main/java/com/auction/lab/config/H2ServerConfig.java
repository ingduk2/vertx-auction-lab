package com.auction.lab.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.sql.SQLException;

@Configuration
public class H2ServerConfig {

    private Server h2Server;

    @EventListener(ContextRefreshedEvent.class)
    public void startH2Server() throws SQLException {
        h2Server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
        h2Server.start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stopH2Server() {
        if (h2Server != null) h2Server.stop();
    }
}
