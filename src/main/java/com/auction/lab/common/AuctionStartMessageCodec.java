package com.auction.lab.common;

import com.auction.lab.domain.message.AuctionStartMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class AuctionStartMessageCodec implements MessageCodec<AuctionStartMessage, AuctionStartMessage> {

    @Override
    public void encodeToWire(Buffer buffer, AuctionStartMessage auctionStartMessage) {

    }

    @Override
    public AuctionStartMessage decodeFromWire(int pos, Buffer buffer) {
        return null;
    }

    @Override
    public AuctionStartMessage transform(AuctionStartMessage auctionStartMessage) {
        return auctionStartMessage;
    }

    @Override
    public String name() {
        return AuctionStartMessage.class.getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
