package com.auction.lab.auction;

import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.message.AuctionStartMessage;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final Vertx vertx;

    public void startAuction(AuctionStartMessage message) {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(3000);

        vertx.eventBus().<String>request(
                        EventBusAddress.AUCTION_START.address(),
                        message,
                        options
                )
                .onSuccess(reply -> log.info("Auction start reply: {}", reply.body()))
                .onFailure(error -> log.error("Auction start failed: {}", error.getMessage()));
    }
}
