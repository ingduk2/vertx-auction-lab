package com.auction.lab.verticle;

import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.message.BidMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuctionVerticle extends AbstractVerticle {

    private int highestBid = 0;

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("AuctionVerticle Start Thread: {}", Thread.currentThread().getName());

        vertx.eventBus().<BidMessage>consumer(EventBusAddress.BID_REQUEST.address(), message -> {
            BidMessage bidMessage = message.body();
            log.info("Received bidMessage: {}", bidMessage);

            if (bidMessage == null) {
                message.fail(400, "BidMessage is null");
                return;
            }

            if (bidMessage.amount() <= 0) {
                message.fail(400, "Bid amount must be greater than 0");
                return;
            }

            if (bidMessage.amount() > highestBid) {
                highestBid = bidMessage.amount();
                log.info("highestBid: {}", highestBid);

                // 입찰자한테 성공 응답
                message.reply("bid success! highestBid: " + highestBid);

                // 모든 참여자한테 브로드캐스트
                vertx.eventBus().publish(
                        EventBusAddress.BID_RESULT.address(),
                        "New highest bid: " + highestBid + " by " + bidMessage.bidderId()
                );
            } else {
                message.reply("bid fail! highestBid: " + highestBid);
            }
        });

        startPromise.complete();
    }

    @Override
    public void stop() {
        log.info("AuctionVerticle Stop");
    }
}
