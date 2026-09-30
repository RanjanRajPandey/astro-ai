import json
import sys

from models.temporal import TemporalAnalysisRequest
from temporal.synthesizer import calculate_temporal_analysis


def main() -> None:
    raw = sys.stdin.buffer.read().decode("utf-8")
    payload = json.loads(raw)
    request = TemporalAnalysisRequest(**payload)
    result = calculate_temporal_analysis(request)
    sys.stdout.buffer.write(result.model_dump_json().encode("utf-8"))
    sys.stdout.buffer.flush()


if __name__ == "__main__":
    main()
