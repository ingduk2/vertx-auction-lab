package com.auction.lab.domain.message;

public record AuctionStartMessage(
        String auctionId,
        long durationSeconds
) {
}
