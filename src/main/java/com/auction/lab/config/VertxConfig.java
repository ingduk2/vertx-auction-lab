package com.auction.lab.config;

import com.auction.lab.common.BidMessageCodec;
import com.auction.lab.domain.message.BidMessage;
import com.auction.lab.verticle.AuctionVerticle;
import com.auction.lab.verticle.WebSocketVerticle;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

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
    public WebSocketVerticle webSocketVerticle(ObjectMapper objectMapper) {
        return new WebSocketVerticle(objectMapper);
    }

    @Bean
    public ApplicationRunner deployVerticles(
            Vertx vertx,
            AuctionVerticle auctionVerticle,
            WebSocketVerticle webSocketVerticle
    ) {
        return args -> {
            vertx.eventBus().registerDefaultCodec(BidMessage.class, new BidMessageCodec());

            vertx.deployVerticle(auctionVerticle)
                    .onSuccess(id -> log.info("AuctionVerticle deployed. ID: {}", id))
                    .onFailure(err -> log.error("AuctionVerticle deployment failed.", err));

            vertx.deployVerticle(webSocketVerticle)
                    .onSuccess(id -> log.info("WebSocketVerticle deployed. ID: {}", id))
                    .onFailure(err -> log.error("WebSocketVerticle deployment failed.", err));
        };
    }
}
