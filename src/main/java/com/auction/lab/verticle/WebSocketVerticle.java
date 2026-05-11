package com.auction.lab.verticle;

import com.auction.lab.common.EventBusAddress;
import com.auction.lab.domain.message.BidMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class WebSocketVerticle extends AbstractVerticle {

    private final ObjectMapper objectMapper;
    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("WebSocketVerticle started. Thread: {}", Thread.currentThread().getName());

        httpServer = vertx.createHttpServer();
        httpServer.webSocketHandler(this::handleWebSocket);
        httpServer.listen(9090)
                .onSuccess(server -> {
                    log.info("WebSocket server started on port {}", server.actualPort());
                    startPromise.complete();
                })
                .onFailure(error -> {
                    log.error("WebSocket server failed to start", error);
                    startPromise.fail(error);
                });
    }

    private void handleWebSocket(ServerWebSocket ws) {
        log.info("Client connected: {}", ws.remoteAddress());

        // BID_RESULT BroadCast 구독
        subscribeBroadcast(ws, EventBusAddress.BID_RESULT);
        // AUCTION_END BroadCast 구독
        subscribeBroadcast(ws, EventBusAddress.AUCTION_END);

        ws.textMessageHandler(rawMessage -> handleMessage(ws, rawMessage));
        ws.closeHandler(event -> log.info("Client disconnected: {}", ws.remoteAddress()));
        ws.exceptionHandler(error -> log.error("WebSocket error: {}", ws.remoteAddress(), error));
    }

    private void subscribeBroadcast(ServerWebSocket ws, EventBusAddress bidResult) {
        vertx.eventBus().<String>consumer(
                bidResult.address(),
                broadcast -> ws.writeTextMessage(broadcast.body())
        );
    }

    private void handleMessage(ServerWebSocket ws, String rawMessage) {
        log.info("Message received: {}", rawMessage);

        try {
            BidMessage bidMessage = objectMapper.readValue(rawMessage, BidMessage.class);
            bidMessage.validate();

            vertx.eventBus().<String>request(EventBusAddress.BID_REQUEST.address(), bidMessage)
                    .onSuccess(reply -> ws.writeTextMessage(reply.body()))
                    .onFailure(error -> ws.writeTextMessage("Error: " + error.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid bid: {} ", e.getMessage());
            ws.writeTextMessage("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Invalid message format: {}", rawMessage, e);
            ws.writeTextMessage("Error: invalid message format");
        }
    }

    @Override
    public void stop() {
        log.info("WebSocketVerticle stopped.");
        httpServer.close();
    }
}
