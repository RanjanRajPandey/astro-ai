import json
import sys

from dashas.vimshottari import calculate_vimshottari_dasha
from models.dashas import DashaCalculationRequest


def main() -> None:
    raw = sys.stdin.read()
    payload = json.loads(raw)
    request = DashaCalculationRequest(**payload)
    result = calculate_vimshottari_dasha(request)
    sys.stdout.write(result.model_dump_json())


if __name__ == "__main__":
    main()
