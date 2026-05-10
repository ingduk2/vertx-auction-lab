package com.auction.lab.verticle;

import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.AuctionState;
import com.auction.lab.domain.message.AuctionStartMessage;
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
    // auctionId → timerId
    private final Map<String, Long> timers = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("AuctionVerticle Start Thread: {}", Thread.currentThread().getName());

        // 경매 시작
        vertx.eventBus().<AuctionStartMessage>consumer(EventBusAddress.AUCTION_START.address(), message -> {
            AuctionStartMessage startMessage = message.body();
            String auctionId = startMessage.auctionId();

            if (auctions.containsKey(auctionId)) {
                message.fail(400, "Auction already exists: " + auctionId);
                return;
            }

            auctions.put(auctionId, new AuctionState(auctionId));
            log.info("Auction started: {}", auctionId);

            // 타이머 등록 - durationSeconds 후 경매 종료
            long timerId = vertx.setTimer(
                    startMessage.durationSeconds() * 1000,
                    id -> closeAuction(auctionId)
            );

            timers.put(auctionId, timerId);
            message.reply("Auction started: " + auctionId);
        });

        // 입찰 처리
        vertx.eventBus().<BidMessage>consumer(EventBusAddress.BID_REQUEST.address(), message -> {
            BidMessage bidMessage = message.body();
            log.info("Received bidMessage: {}", bidMessage);

            AuctionState state = auctions.get(bidMessage.auctionId());

            if (state == null) {
                message.fail(404, "Auction not found " + bidMessage.auctionId());
                return;
            }

            if (state.isClosed()) {
                message.fail(400, "Auction is closed: " + bidMessage.auctionId());
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

    private void closeAuction(String auctionId) {
        AuctionState state = auctions.get(auctionId);
        if (state == null) return;

        state.close();
        timers.remove(auctionId);

        log.info("Auction closed - auctionId: {}, highestBid: {}, winner: {}",
                auctionId, state.getHighestBid(), state.getHighestBidderId());

        // 경매 종료 브로드캐스트
        vertx.eventBus().publish(
                EventBusAddress.AUCTION_END.address(),
                "Auction closed! Winner: " + state.getHighestBidderId() + " with " + state.getHighestBid()
        );
    }

    @Override
    public void stop() {
        log.info("AuctionVerticle Stop");
    }
}
