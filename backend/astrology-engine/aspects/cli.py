import json
import sys

from aspects.drishti import calculate_all_aspects
from models.aspects import AspectCalculationRequest


def main() -> None:
    raw = sys.stdin.read()
    payload = json.loads(raw)
    request = AspectCalculationRequest(**payload)
    result = calculate_all_aspects(request)
    sys.stdout.write(result.model_dump_json())


if __name__ == "__main__":
    main()
