package com.auction.lab.verticle;

import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.AuctionState;
import com.auction.lab.domain.message.BidMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AuctionVerticle extends AbstractVerticle {

    // auctionId → AuctionState, 단일 Event Loop Thread에서만 접근하므로 HashMap 사용
    private final Map<String, AuctionState> auctions = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("AuctionVerticle Start Thread: {}", Thread.currentThread().getName());

        vertx.eventBus().<BidMessage>consumer(EventBusAddress.BID_REQUEST.address(), message -> {
            BidMessage bidMessage = message.body();
            log.info("Received bidMessage: {}", bidMessage);

            AuctionState state = auctions.computeIfAbsent(
                    bidMessage.auctionId(),
                    AuctionState::new
            );

            if (state.isClosed()) {
                message.fail(400, "Auction is closed");
                return;
            }

            boolean success = state.bid(bidMessage.bidderId(), bidMessage.amount());

            if (success) {
                log.info("Bid accepted - {}", state);

                message.reply("bid success ! highestBid: " + state.getHighestBid());

                vertx.eventBus().publish(
                        EventBusAddress.BID_RESULT.address(),
                        "New highest bid: " + state.getHighestBid() + " by " + state.getHighestBidderId()
                );
            } else {
                message.reply("bid fail! highestBid: " + state.getHighestBid());
            }
        });

        startPromise.complete();
    }

    @Override
    public void stop() {
        log.info("AuctionVerticle Stop");
    }
}
