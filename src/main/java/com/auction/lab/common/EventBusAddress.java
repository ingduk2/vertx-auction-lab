package com.auction.lab.common;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventBusAddress {
    BID_REQUEST("auction.dis.request"),
    BID_RESULT("auction.bid.result"),
    AUCTION_START("auction.start"),
    AUCTION_END("auction.end"),
    AUCTION_SAVE("auction.save");

    private final String address;

    public String address() {
        return address;
    }
}
