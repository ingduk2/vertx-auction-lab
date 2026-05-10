package com.auction.lab.domain.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BidMessageTest {

    @Nested
    class Validate {
        @Test
        @DisplayName("auctionId null/blanck 시 fail")
        void fail1() {
            validateBidMessage(
                    new BidMessage(null, "bidderId", 0),
                    "auctionId is required"
            );
            validateBidMessage(
                    new BidMessage("", "bidderId", 0),
                    "auctionId is required"
            );
        }

        @Test
        @DisplayName("bidderId null/blanck 시 fail")
        void fail2() {
            validateBidMessage(
                    new BidMessage("auctionId", null, 0),
                    "bidderId is required"
            );
            validateBidMessage(
                    new BidMessage("auctionId", "", 0),
                    "bidderId is required"
            );
        }

        @Test
        @DisplayName("amount 0 이하 시 fail")
        void fail3() {
            validateBidMessage(
                    new BidMessage("auctionId", "bidderId", 0),
                    "Bid amount must be greater than 0"
            );
            validateBidMessage(
                    new BidMessage("auctionId", "bidderId", -1),
                    "Bid amount must be greater than 0"
            );
        }

        @Test
        @DisplayName("validate 통과 시 success")
        void success1() {
            assertThatCode(new BidMessage("auctionId", "bidderId", 1000)::validate)
                    .doesNotThrowAnyException();
        }

        private void validateBidMessage(BidMessage bidMessage, String message) {
            assertThatThrownBy(bidMessage::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(message);
        }
    }
}