import json
import sys

from models.reasoning import ReasoningSynthesisRequest
from reasoning.synthesizer import synthesize_astrological_reasoning


def main() -> None:
    raw = sys.stdin.buffer.read().decode("utf-8")
    payload = json.loads(raw) if raw.strip() else {}
    request = ReasoningSynthesisRequest(**payload)
    result = synthesize_astrological_reasoning(request)
    sys.stdout.buffer.write(result.model_dump_json().encode("utf-8"))
    sys.stdout.buffer.flush()


if __name__ == "__main__":
    main()
