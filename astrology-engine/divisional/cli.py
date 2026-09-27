import json
import sys

from divisional.shodashavarga import calculate_shodashavarga
from models.divisional import DivisionalCalculationRequest


def main() -> None:
    raw = sys.stdin.read()
    payload = json.loads(raw)
    request = DivisionalCalculationRequest(**payload)
    result = calculate_shodashavarga(request)
    sys.stdout.write(result.model_dump_json())


if __name__ == "__main__":
    main()
