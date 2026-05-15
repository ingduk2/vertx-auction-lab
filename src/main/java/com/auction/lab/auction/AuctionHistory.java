package com.auction.lab.auction;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "auction_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String auctionId;

    private String winnerId;

    private int finalBid;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime closedAt;

    public static AuctionHistory create(
            String auctionId,
            String winnerId,
            int finalBid,
            LocalDateTime startedAt
    ) {
        AuctionHistory auctionHistory = new AuctionHistory();
        auctionHistory.auctionId = Objects.requireNonNull(auctionId);
        auctionHistory.winnerId = winnerId;
        auctionHistory.finalBid = finalBid;
        auctionHistory.startedAt = Objects.requireNonNull(startedAt);
        auctionHistory.closedAt = LocalDateTime.now();
        return auctionHistory;
    }
}
