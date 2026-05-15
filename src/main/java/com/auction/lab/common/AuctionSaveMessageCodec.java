package com.auction.lab.common;

import com.auction.lab.domain.message.AuctionSaveMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class AuctionSaveMessageCodec implements MessageCodec<AuctionSaveMessage, AuctionSaveMessage> {

    @Override
    public void encodeToWire(Buffer buffer, AuctionSaveMessage auctionSaveMessage) {

    }

    @Override
    public AuctionSaveMessage decodeFromWire(int pos, Buffer buffer) {
        return null;
    }

    @Override
    public AuctionSaveMessage transform(AuctionSaveMessage auctionSaveMessage) {
        return auctionSaveMessage;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
