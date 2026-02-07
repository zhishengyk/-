import argparse
import json
import random
from datetime import datetime, timedelta


SERVICES = {
    "gateway": {
        "module": "edge",
        "endpoints": [
            ("GET", "/api/overview"),
            ("GET", "/api/stats/summary"),
            ("GET", "/api/stats/error-trend"),
            ("POST", "/api/logs/upload"),
        ],
    },
    "auth-service": {
        "module": "auth",
        "endpoints": [
            ("POST", "/api/auth/login"),
            ("GET", "/api/auth/me"),
            ("POST", "/api/auth/refresh"),
        ],
    },
    "order-service": {
        "module": "order",
        "endpoints": [
            ("POST", "/api/orders"),
            ("GET", "/api/orders/{order_id}"),
            ("POST", "/api/orders/{order_id}/cancel"),
        ],
    },
    "payment-service": {
        "module": "payment",
        "endpoints": [
            ("POST", "/api/payments/checkout"),
            ("GET", "/api/payments/{order_id}/status"),
            ("POST", "/api/payments/{order_id}/refund"),
        ],
    },
    "inventory-service": {
        "module": "inventory",
        "endpoints": [
            ("GET", "/api/inventory/{sku}"),
            ("POST", "/api/inventory/{sku}/reserve"),
            ("POST", "/api/inventory/{sku}/release"),
        ],
    },
    "search-service": {
        "module": "search",
        "endpoints": [
            ("GET", "/api/search"),
            ("GET", "/api/recommendations"),
        ],
    },
    "notification-service": {
        "module": "notify",
        "endpoints": [
            ("POST", "/api/notifications/send"),
            ("GET", "/api/notifications/{order_id}"),
        ],
    },
}

REGIONS = ["cn-sh", "cn-bj", "us-west", "eu-central"]
DEVICES = ["web", "ios", "android", "admin-console"]
USERS = [f"user-{n:04d}" for n in range(1, 260)]
SKUS = [f"SKU-{n:05d}" for n in range(1000, 1080)]


def weighted_choice(rng: random.Random, weighted_items):
    values = [item[0] for item in weighted_items]
    weights = [item[1] for item in weighted_items]
    return rng.choices(values, weights=weights, k=1)[0]


def is_peak(index: int) -> bool:
    # A periodic traffic surge lasting about 40 events in each 240-event cycle.
    return 105 <= (index % 240) <= 145


def is_incident(index: int) -> bool:
    # A short incident window in each 420-event cycle.
    return 292 <= (index % 420) <= 308


def pick_status(rng: random.Random, index: int) -> int:
    if is_incident(index):
        return weighted_choice(
            rng,
            [(500, 46), (502, 24), (503, 18), (504, 7), (429, 3), (200, 2)],
        )

    if is_peak(index):
        return weighted_choice(
            rng,
            [
                (200, 36),
                (201, 7),
                (204, 6),
                (400, 7),
                (401, 5),
                (403, 7),
                (404, 4),
                (429, 20),
                (500, 4),
                (502, 3),
                (503, 1),
            ],
        )

    return weighted_choice(
        rng,
        [
            (200, 55),
            (201, 8),
            (204, 6),
            (304, 5),
            (400, 6),
            (401, 4),
            (403, 5),
            (404, 5),
            (429, 2),
            (500, 2),
            (502, 1),
            (503, 1),
        ],
    )


def status_to_level(status_code: int) -> str:
    if status_code >= 500:
        return "ERROR"
    if status_code >= 400:
        return "WARN"
    return "INFO"


def pick_response_time_ms(rng: random.Random, status_code: int, index: int) -> int:
    if status_code >= 500:
        base = rng.randint(350, 4200)
    elif status_code == 429:
        base = rng.randint(120, 1600)
    elif status_code >= 400:
        base = rng.randint(80, 1300)
    else:
        base = rng.randint(18, 680)

    if is_peak(index):
        base += rng.randint(70, 600)
    if is_incident(index):
        base += rng.randint(300, 2100)

    return min(base, 8800)


def render_path(template: str, rng: random.Random, order_id: str, sku: str) -> str:
    path = template.replace("{order_id}", order_id).replace("{sku}", sku)
    if path == "/api/search":
        query = weighted_choice(
            rng,
            [
                ("laptop", 20),
                ("wireless mouse", 17),
                ("gaming keyboard", 14),
                ("4k monitor", 12),
                ("usb c hub", 12),
                ("noise cancelling headset", 10),
                ("camera tripod", 8),
                ("office chair", 7),
            ],
        )
        return f"{path}?q={query.replace(' ', '+')}"
    if path == "/api/recommendations":
        scene = weighted_choice(rng, [("home", 4), ("checkout", 7), ("flash-sale", 3)])
        return f"{path}?scene={scene}"
    return path


def build_message(
    status_code: int,
    method: str,
    path: str,
    service: str,
    trace_id: str,
    user_id: str,
    response_time_ms: int,
    index: int,
) -> str:
    if status_code >= 500:
        return (
            f"{method} {path} failed with {status_code}; "
            f"upstream timeout on {service}; trace={trace_id} user={user_id} rt={response_time_ms}ms"
        )

    if status_code == 429:
        return (
            f"Rate limit exceeded on {method} {path}; "
            f"burst traffic detected trace={trace_id} retry_after=2s"
        )

    if status_code in (401, 403):
        return (
            f"Auth blocked for {method} {path}; "
            f"principal={user_id} status={status_code} trace={trace_id}"
        )

    if status_code == 404:
        return f"Resource not found for {method} {path}; trace={trace_id}"

    if status_code == 400:
        return (
            f"Validation failed for {method} {path}; "
            f"invalid payload schema version; trace={trace_id}"
        )

    if is_peak(index):
        return (
            f"{method} {path} completed during campaign traffic; "
            f"trace={trace_id} user={user_id} rt={response_time_ms}ms"
        )

    return (
        f"{method} {path} completed; "
        f"trace={trace_id} user={user_id} rt={response_time_ms}ms"
    )


def next_timestamp(current: datetime, step_seconds: int, rng: random.Random) -> datetime:
    # Keep logs chronological but avoid a perfectly fixed cadence.
    jitter = rng.choice([0, 0, 1, 1, 2, 3])
    return current + timedelta(seconds=step_seconds + jitter)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--count", type=int, default=1000)
    parser.add_argument("--start", type=str, default="2025-01-01 00:00:00")
    parser.add_argument("--step-seconds", type=int, default=5)
    parser.add_argument("--output", type=str, default="sample-logs.jsonl")
    parser.add_argument("--seed", type=int, default=20250207)
    args = parser.parse_args()

    rng = random.Random(args.seed)
    start_time = datetime.strptime(args.start, "%Y-%m-%d %H:%M:%S")
    current = start_time

    with open(args.output, "w", encoding="utf-8") as f:
        for index in range(args.count):
            service = rng.choice(list(SERVICES.keys()))
            service_cfg = SERVICES[service]
            method, endpoint_tpl = rng.choice(service_cfg["endpoints"])
            order_id = f"ORD-{rng.randint(100000, 999999)}"
            sku = rng.choice(SKUS)
            path = render_path(endpoint_tpl, rng, order_id, sku)
            status_code = pick_status(rng, index)
            response_time_ms = pick_response_time_ms(rng, status_code, index)
            level = status_to_level(status_code)

            trace_id = f"{rng.getrandbits(64):016x}"
            request_id = f"req-{rng.getrandbits(48):012x}"
            user_id = rng.choice(USERS)
            session_id = f"sess-{rng.getrandbits(32):08x}"
            region = rng.choice(REGIONS)
            device = rng.choice(DEVICES)

            entry = {
                "timestamp": current.strftime("%Y-%m-%d %H:%M:%S"),
                "level": level,
                "statusCode": status_code,
                "responseTime": response_time_ms,
                "message": build_message(
                    status_code=status_code,
                    method=method,
                    path=path,
                    service=service,
                    trace_id=trace_id,
                    user_id=user_id,
                    response_time_ms=response_time_ms,
                    index=index,
                ),
                "source": "story-generator",
                "service": service,
                "module": service_cfg["module"],
                "method": method,
                "path": path,
                "traceId": trace_id,
                "requestId": request_id,
                "userId": user_id,
                "sessionId": session_id,
                "region": region,
                "device": device,
                "orderId": order_id,
                "sku": sku,
                "retryCount": rng.randint(0, 2) if status_code >= 500 else 0,
                "campaign": "new-year" if is_peak(index) else "normal",
            }
            f.write(json.dumps(entry, ensure_ascii=False) + "\n")
            current = next_timestamp(current, args.step_seconds, rng)


if __name__ == "__main__":
    main()
