package com.auction.lab.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuctionVerticle extends AbstractVerticle {

    private int highestBid = 0;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("AuctionVerticle Start Thread: {}", Thread.currentThread().getName());

        vertx.eventBus().<Integer>consumer("auction.bid.request", message -> {
            int bidAmount = message.body();
            log.info("bidAmount: {}", bidAmount);

            if (bidAmount > highestBid) {
                highestBid = bidAmount;
                log.info("highestBid: {}", highestBid);
                message.reply("bid success! highestBid: " + highestBid);
            } else {
                message.reply("bid fail! highestBid: {}" + highestBid);
            }
        });

        startPromise.complete();
    }

    @Override
    public void stop() {
        log.info("AuctionVerticle Stop");
    }
}
