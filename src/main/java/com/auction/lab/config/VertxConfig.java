package com.auction.lab.config;

import com.auction.lab.verticle.AuctionVerticle;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class VertxConfig {

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public AuctionVerticle auctionVerticle() {
        return new AuctionVerticle();
    }

    @Bean
    public ApplicationRunner deployVerticles(Vertx vertx, AuctionVerticle auctionVerticle) {
        return args -> vertx.deployVerticle(auctionVerticle())
                .onSuccess(id -> log.info("AuctionVerticle Success ID: {}", id))
                .onFailure(err -> log.error("AuctionVerticle Fail, err"));
    }
}
