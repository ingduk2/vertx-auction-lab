package com.auction.lab.domain.message;

public record BidMessage(
        String auctionId,
        String bidderId,
        int amount
) {
}
