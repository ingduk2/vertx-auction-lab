package com.auction.lab.auction;

import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.message.AuctionStartMessage;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final Vertx vertx;

    public CompletableFuture<String> startAuction(AuctionStartMessage message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(3000);

        vertx.eventBus().<String>request(
                        EventBusAddress.AUCTION_START.address(),
                        message,
                        options
                )
                .onSuccess(reply -> future.complete(reply.body()))
                .onFailure(future::completeExceptionally);

        return future;
    }
}
