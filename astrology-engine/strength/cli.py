import json
import sys

from models.strength import ShadbalaCalculationRequest
from strength.shadbala import calculate_shadbala_and_vimshopaka


def main() -> None:
    raw = sys.stdin.read()
    payload = json.loads(raw)
    request = ShadbalaCalculationRequest(**payload)
    result = calculate_shadbala_and_vimshopaka(request)
    sys.stdout.write(result.model_dump_json())


if __name__ == "__main__":
    main()
