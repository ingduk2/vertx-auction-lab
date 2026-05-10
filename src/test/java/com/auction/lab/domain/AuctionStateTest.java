package com.auction.lab.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AuctionStateTest {

    @Nested
    class Create {
        @Test
        @DisplayName("auctionId 가 null/blanck 인 경우 fail")
        void fail1() {
            assertThatThrownBy(() -> new AuctionState(null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new AuctionState(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("auctionId 가 정상 인 경우 success")
        void success1() {
            assertThatCode(() -> new AuctionState("auctionId"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class Bid {
        @Test
        @DisplayName("입찰 실패 - 현재 최고가 보다 낮음")
        void fail1() {
            AuctionState auctionState = new AuctionState("auction1");

            auctionState.bid("bidder1", 100);

            boolean bidResult = auctionState.bid("bidder2", 70);
            assertThat(bidResult).isFalse();
        }

        @Test
        @DisplayName("입찰 실패 - 경매 종료 상태 (close)")
        void fail2() {
            AuctionState auctionState = new AuctionState("auction1");
            auctionState.close();

            boolean bidResult = auctionState.bid("bidder1", 100);

            assertThat(bidResult).isFalse();
        }

        @Test
        @DisplayName("입찰 성공 - 최고가 갱신")
        void success1() {
            AuctionState auctionState = new AuctionState("auction1");
            boolean bidResult = auctionState.bid("bidder1", 100);

            assertThat(bidResult).isTrue();
            assertThat(auctionState.getAuctionId()).isEqualTo("auction1");
            assertThat(auctionState.getHighestBid()).isEqualTo(100);
            assertThat(auctionState.getHighestBidderId()).isEqualTo("bidder1");
        }
    }
}