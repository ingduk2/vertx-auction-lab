package com.auction.lab.common;

import com.auction.lab.domain.message.BidMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class BidMessageCodec implements MessageCodec<BidMessage, BidMessage> {

    @Override
    public void encodeToWire(Buffer buffer, BidMessage bidMessage) {
        // 로컬 EventBus만 쓸 거라 구현 안 해도 됨
    }

    @Override
    public BidMessage decodeFromWire(int pos, Buffer buffer) {
        // 로컬 EventBus만 쓸 거라 구현 안 해도 됨
        return null;
    }

    @Override
    public BidMessage transform(BidMessage bidMessage) {
        // 같은 JVM 내에서는 객체 그대로 전달
        return bidMessage;
    }

    @Override
    public String name() {
        return BidMessageCodec.class.getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1; // 커스텀 코덱은 항상 -1
    }
}
