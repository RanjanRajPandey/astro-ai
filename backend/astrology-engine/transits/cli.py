import json
import sys

from models.transits import TransitCalculationRequest
from transits.gochar import calculate_gochar_transits


def main() -> None:
    raw = sys.stdin.buffer.read().decode("utf-8")
    payload = json.loads(raw)
    request = TransitCalculationRequest(**payload)
    result = calculate_gochar_transits(request)
    sys.stdout.buffer.write(result.model_dump_json().encode("utf-8"))
    sys.stdout.buffer.flush()


if __name__ == "__main__":
    main()
