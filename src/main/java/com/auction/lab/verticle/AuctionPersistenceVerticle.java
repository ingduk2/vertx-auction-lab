package com.auction.lab.verticle;

import com.auction.lab.auction.AuctionHistory;
import com.auction.lab.auction.AuctionHistoryRepository;
import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.message.AuctionSaveMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuctionPersistenceVerticle extends AbstractVerticle {

    private final AuctionHistoryRepository repository;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("AuctionPersistenceVerticle started. Thread: {}", Thread.currentThread().getName());

        vertx.eventBus().consumer(EventBusAddress.AUCTION_SAVE.address(), this::handleSave);
    }

    private void handleSave(Message<AuctionSaveMessage> message) {
        AuctionSaveMessage saveMessage = message.body();
        log.info("Saving auction history: {}", saveMessage);

        try {
            AuctionHistory auctionHistory = AuctionHistory.create(
                    saveMessage.auctionId(),
                    saveMessage.winnerId(),
                    saveMessage.finalBid(),
                    saveMessage.startedAt()
            );
            repository.save(auctionHistory);
            log.info("Auction history saved: {}", saveMessage.auctionId());
            message.reply("saved");
        } catch (Exception e) {
            log.error("Failed to save auction history: {}", saveMessage.auctionId());
            message.fail(500, "Failed to save: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        log.info("AuctionPersistenceVerticle stopped.");
    }
}
