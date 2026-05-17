package com.auction.lab.verticle;

import com.auction.lab.common.AuctionSaveMessageCodec;
import com.auction.lab.common.AuctionStartMessageCodec;
import com.auction.lab.common.BidMessageCodec;
import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.message.AuctionSaveMessage;
import com.auction.lab.domain.message.AuctionStartMessage;
import com.auction.lab.domain.message.BidMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
class AuctionVerticleTest {

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext ctx) {
        vertx.eventBus().registerDefaultCodec(AuctionStartMessage.class, new AuctionStartMessageCodec());
        vertx.eventBus().registerDefaultCodec(BidMessage.class, new BidMessageCodec());
        vertx.eventBus().registerDefaultCodec(AuctionSaveMessage.class, new AuctionSaveMessageCodec());

        vertx.deployVerticle(new AuctionVerticle())
                .onSuccess(id -> ctx.completeNow())
                .onFailure(ctx::failNow);
    }

    private Future<Message<String>> startAuction(
            Vertx vertx,
            String auctionId,
            long duration
    ) {
        return vertx.eventBus().<String>request(
                EventBusAddress.AUCTION_START.address(),
                new AuctionStartMessage(auctionId, duration)
        );
    }

    private Future<Message<String>> placeBid(
            Vertx vertx,
            String auctionId,
            String bidderId,
            int amount
    ) {
        return vertx.eventBus().<String>request(
                EventBusAddress.BID_REQUEST.address(),
                new BidMessage(auctionId, bidderId, amount)
        );
    }

    @Test
    @DisplayName("경매 시작 성공")
    void test1(Vertx vertx, VertxTestContext ctx) {
        startAuction(vertx, "test-001", 60)
                .onSuccess(reply -> ctx.verify(() -> {
                    assertThat(reply.body()).contains("Auction started");
                    ctx.completeNow();
                }))
                .onFailure(ctx::failNow);
    }

    @Test
    @DisplayName("중복 경매 시작 실패")
    void test2(Vertx vertx, VertxTestContext ctx) {
        startAuction(vertx, "test-001", 60)
                .compose(reply -> startAuction(vertx, "test-001", 60))
                .onSuccess(reply -> ctx.failNow(new AssertionError("중복 시작이 성공하면 안 됨")))
                .onFailure(error -> ctx.verify(() -> {
                    assertThat(error.getMessage()).contains("Auction already");
                    ctx.completeNow();
                }));
    }

    @Test
    @DisplayName("입찰 성공 - 최고가 갱신")
    void test3(Vertx vertx, VertxTestContext ctx) {
        startAuction(vertx, "test-001", 60)
                .compose(reply -> placeBid(vertx, "test-001", "user-001", 5000))
                .onSuccess(reply -> ctx.verify(() -> {
                    assertThat(reply.body()).contains("bid success");
                    assertThat(reply.body()).contains("5000");
                    ctx.completeNow();
                }))
                .onFailure(ctx::failNow);
    }

    @Test
    @DisplayName("입찰 실패 - 최고가보다 낮은 금액")
    void test4(Vertx vertx, VertxTestContext ctx) {
        startAuction(vertx, "test-001", 60)
                .compose(reply -> placeBid(vertx, "test-001", "user-001", 5000))
                .compose(reply -> placeBid(vertx, "test-001", "user-002", 3000))
                .onSuccess(reply -> ctx.verify(() -> {
                    assertThat(reply.body()).contains("bid fail");
                    ctx.completeNow();
                }))
                .onFailure(ctx::failNow);
    }

    @Test
    @DisplayName("입찰 실패 - 존재하지 않는 경매")
    void test5(Vertx vertx, VertxTestContext ctx) {
        placeBid(vertx, "not-exist", "user-001", 5000)
                .onSuccess(reply -> ctx.failNow(new AssertionError("없는 경매 입찰이 성공하면 안 됨")))
                .onFailure(error -> ctx.verify(() -> {
                    assertThat(error.getMessage()).contains("Auction not found");
                    ctx.completeNow();
                }));
    }

    @Test
    @DisplayName("입찰 실패 - 종료된 경매")
    void test6(Vertx vertx, VertxTestContext ctx) {
        startAuction(vertx, "test-001", 1)
                .compose(reply -> {
                    Promise<Void> promise = Promise.promise();
                    vertx.setTimer(2000, id -> promise.complete());
                    return promise.future();
                })
                .compose(v -> placeBid(vertx, "test-001", "user-001", 5000))
                .onSuccess(reply -> ctx.verify(() -> {
                    assertThat(reply.body()).contains("bid fail");
                    ctx.completeNow();
                }))
                .onFailure(error -> ctx.verify(() -> {
                    assertThat(error.getMessage()).contains("Auction is closed");
                    ctx.completeNow();
                }));
    }
}