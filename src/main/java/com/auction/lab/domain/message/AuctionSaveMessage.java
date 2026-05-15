package com.auction.lab.domain.message;

import java.time.LocalDateTime;

public record AuctionSaveMessage(
        String auctionId,
        String winnerId,
        int finalBid,
        LocalDateTime startedAt
) {
}
