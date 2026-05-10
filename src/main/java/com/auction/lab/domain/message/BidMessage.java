package com.auction.lab.domain.message;

public record BidMessage(
        String auctionId,
        String bidderId,
        int amount
) {
    public void validate() {
        if (auctionId == null || auctionId.isBlank()) throw new IllegalArgumentException("auctionId is required");
        if (bidderId == null || bidderId.isBlank()) throw new IllegalArgumentException("bidderId is required");
        if (amount <= 0) throw new IllegalArgumentException("Bid amount must be greater than 0");
    }
}
