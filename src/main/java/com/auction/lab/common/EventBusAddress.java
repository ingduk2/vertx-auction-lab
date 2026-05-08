package com.auction.lab.common;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventBusAddress {
    BID_REQUEST("auction.dis.request"),
    BID_RESULT("auction.bid.result"),
    AUCTION_END("auction.end");

    private final String address;

    public String address() {
        return address;
    }
}
