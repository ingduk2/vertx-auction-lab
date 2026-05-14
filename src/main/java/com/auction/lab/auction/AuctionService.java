package com.auction.lab.auction;

import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.message.AuctionStartMessage;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final Vertx vertx;

    public Mono<String> startAuction(AuctionStartMessage message) {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(3000);

        return Mono.create(sink ->
                vertx.eventBus().<String>request(
                                EventBusAddress.AUCTION_START.address(),
                                message,
                                options
                        )
                        .onSuccess(reply -> sink.success(reply.body()))
                        .onFailure(sink::error)
        );
    }
}
