import json
import sys

from models.yogas import YogaCalculationRequest
from yogas.detector import detect_all_yogas


def main() -> None:
    raw = sys.stdin.buffer.read().decode("utf-8")
    payload = json.loads(raw)
    request = YogaCalculationRequest(**payload)
    result = detect_all_yogas(request)
    sys.stdout.buffer.write(result.model_dump_json().encode("utf-8"))
    sys.stdout.buffer.flush()


if __name__ == "__main__":
    main()
