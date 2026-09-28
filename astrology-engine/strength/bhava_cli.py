import json
import sys

from models.strength import BhavaBalaCalculationRequest
from strength.bhavabala import calculate_bhava_bala


def main() -> None:
    raw = sys.stdin.read()
    payload = json.loads(raw)
    request = BhavaBalaCalculationRequest(**payload)
    result = calculate_bhava_bala(request)
    sys.stdout.write(result.model_dump_json())


if __name__ == "__main__":
    main()
