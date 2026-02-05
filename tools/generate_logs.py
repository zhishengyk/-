import argparse
import json
import random
from datetime import datetime, timedelta


LEVELS = ["INFO", "WARN", "ERROR"]
STATUS = [200, 201, 204, 400, 401, 403, 404, 500, 502]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--count", type=int, default=1000)
    parser.add_argument("--start", type=str, default="2025-01-01 00:00:00")
    parser.add_argument("--step-seconds", type=int, default=5)
    parser.add_argument("--output", type=str, default="sample-logs.jsonl")
    args = parser.parse_args()

    start_time = datetime.strptime(args.start, "%Y-%m-%d %H:%M:%S")
    current = start_time
    with open(args.output, "w", encoding="utf-8") as f:
        for _ in range(args.count):
            entry = {
                "timestamp": current.strftime("%Y-%m-%d %H:%M:%S"),
                "level": random.choice(LEVELS),
                "statusCode": random.choice(STATUS),
                "responseTime": random.randint(5, 2000),
                "message": "Request handled",
                "source": "generator"
            }
            f.write(json.dumps(entry, ensure_ascii=False) + "\n")
            current += timedelta(seconds=args.step_seconds)


if __name__ == "__main__":
    main()
