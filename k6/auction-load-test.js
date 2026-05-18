import http from 'k6/http';
import { WebSocket } from 'k6/experimental/websockets';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

// ── 커스텀 메트릭 ──
const bidSuccessCount = new Counter('bid_success_count');
const bidFailCount = new Counter('bid_fail_count');
const bidLatency = new Trend('bid_latency_ms');

// ── 시나리오 설정 ──
export const options = {
    scenarios: {
        auction_scenario: {
            executor: 'per-vu-iterations',
            vus: 10000,
            iterations: 1,        // VU당 딱 1번만
            maxDuration: '60s',
        },
    },
    thresholds: {
        bid_latency_ms: ['p(95)<500'],
        bid_success_count: ['count>0'],
    },
};

const BASE_URL = 'http://localhost:8080';
const WS_URL = 'ws://localhost:9090';
const AUCTION_DURATION_SEC = 40;

// ── 경매 시작 ──
export function setup() {
    const auctionId = `load-test-${Date.now()}`;

    const res = http.post(`${BASE_URL}/api/auction/start`,
        JSON.stringify({ auctionId, durationSeconds: AUCTION_DURATION_SEC }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    console.log(`status: ${res.status}, body: ${res.body}`);
    check(res, { 'auction started': (r) => r.status === 200 });
    sleep(1);

    return { auctionId };
}

// ── 메인 시나리오 ──
export default function (data) {
    const bidderId = `user-${__VU}`;
    const auctionId = data.auctionId;
    let lastSentTime = 0;
    let intervalId = null;
    let auctionClosed = false;

    const socket = new WebSocket(WS_URL);

    socket.onopen = () => {
        console.log(`[VU ${__VU}] connected - auctionId: ${auctionId}`);

        intervalId = setInterval(() => {
            // 경매 종료되었거나 소켓이 열려있지 않으면 interval 정리
            if (auctionClosed || socket.readyState !== 1) {
                clearInterval(intervalId);
                return;
            }

            const amount = Math.floor(Math.random() * 10000) + 1000;
            lastSentTime = Date.now();
            socket.send(JSON.stringify({ auctionId, bidderId, amount }));
        }, 1000);
    };

    socket.onmessage = (e) => {
        const msg = e.data;

        if (msg.includes('bid success')) {
            bidLatency.add(Date.now() - lastSentTime);
            bidSuccessCount.add(1);

        } else if (msg.includes('bid fail')) {
            bidLatency.add(Date.now() - lastSentTime);
            bidFailCount.add(1);

        } else if (msg.includes('Error')) {
            bidFailCount.add(1);

        } else if (msg.includes('Auction closed')) {
            console.log(`[VU ${__VU}] ${msg}`);
            auctionClosed = true;
            clearInterval(intervalId);
            socket.close();
        }
    };

    socket.onerror = (e) => console.error(`[VU ${__VU}] error: ${e}`);
    socket.onclose = () => console.log(`[VU ${__VU}] disconnected`);
}

// ── 테스트 종료 ──
export function teardown() {
    console.log('Load test completed!');
}