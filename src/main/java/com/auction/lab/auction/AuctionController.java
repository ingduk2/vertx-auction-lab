package com.auction.lab.auction;

import com.auction.lab.domain.message.AuctionStartMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auction")
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping("/start")
    public CompletableFuture<ResponseEntity<String>> startAuction(
            @RequestBody AuctionStartRequest request
    ) {
        AuctionStartMessage message = request.toMessage();
        return auctionService.startAuction(request.toMessage())
                .thenApply(ResponseEntity::ok)
                .exceptionally(error -> ResponseEntity
                        .badRequest()
                        .body(error.getCause().getMessage())
                );
    }
}
