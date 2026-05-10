package com.auction.lab.auction;

import com.auction.lab.domain.message.AuctionStartMessage;

public record AuctionStartRequest(
        String auctionId,
        long durationSeconds
) {
    public AuctionStartMessage toMessage() {
        return new AuctionStartMessage(auctionId, durationSeconds);
    }
}
