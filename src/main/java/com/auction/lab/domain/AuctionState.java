package com.auction.lab.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuctionState {
    private final String auctionId;
    private int highestBid;
    private String highestBidderId;
    private boolean closed;
    private final LocalDateTime startedAt;

    public AuctionState(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) throw new IllegalArgumentException("auctionId is required");
        this.auctionId = auctionId;
        this.highestBid = 0;
        this.highestBidderId = null;
        this.closed = false;
        this.startedAt = LocalDateTime.now();
    }

    public boolean bid(String bidderId, int amount) {
        if (closed) return false;
        if (amount <= highestBid) return false;

        highestBid = amount;
        highestBidderId = bidderId;
        return true;
    }

    public void close() {
        this.closed = true;
    }

    @Override
    public String toString() {
        return "AuctionState{" +
                "auctionId='" + auctionId + '\'' +
                ", highestBid=" + highestBid +
                ", highestBidderId='" + highestBidderId + '\'' +
                ", closed=" + closed +
                '}';
    }
}
