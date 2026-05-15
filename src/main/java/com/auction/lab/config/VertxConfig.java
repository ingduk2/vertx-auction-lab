package com.auction.lab.config;

import com.auction.lab.auction.AuctionHistoryRepository;
import com.auction.lab.common.AuctionSaveMessageCodec;
import com.auction.lab.common.AuctionStartMessageCodec;
import com.auction.lab.common.BidMessageCodec;
import com.auction.lab.domain.message.AuctionSaveMessage;
import com.auction.lab.domain.message.AuctionStartMessage;
import com.auction.lab.domain.message.BidMessage;
import com.auction.lab.verticle.AuctionPersistenceVerticle;
import com.auction.lab.verticle.AuctionVerticle;
import com.auction.lab.verticle.WebSocketVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
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
    public AuctionPersistenceVerticle auctionPersistenceVerticle(AuctionHistoryRepository repository) {
        return new AuctionPersistenceVerticle(repository);
    }

    @Bean
    public ApplicationRunner deployVerticles(
            Vertx vertx,
            AuctionVerticle auctionVerticle,
            WebSocketVerticle webSocketVerticle,
            AuctionPersistenceVerticle auctionPersistenceVerticle
    ) {
        return args -> {
            vertx.eventBus().registerDefaultCodec(BidMessage.class, new BidMessageCodec());
            vertx.eventBus().registerDefaultCodec(AuctionStartMessage.class, new AuctionStartMessageCodec());
            vertx.eventBus().registerDefaultCodec(AuctionSaveMessage.class, new AuctionSaveMessageCodec());

            vertx.deployVerticle(auctionVerticle)
                    .onSuccess(id -> log.info("AuctionVerticle deployed. ID: {}", id))
                    .onFailure(err -> log.error("AuctionVerticle deployment failed.", err));

            vertx.deployVerticle(webSocketVerticle)
                    .onSuccess(id -> log.info("WebSocketVerticle deployed. ID: {}", id))
                    .onFailure(err -> log.error("WebSocketVerticle deployment failed.", err));

            vertx.deployVerticle(auctionPersistenceVerticle,
                            new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                    .onSuccess(id -> log.info("AuctionPersistenceVerticle deployed. ID: {}", id))
                    .onFailure(err -> log.error("AuctionPersistenceVerticle deployment failed.", err));
        };
    }
}
