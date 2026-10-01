import json
import sys

from evidence.evaluator import generate_astrological_evidence
from models.evidence import EvidenceGenerationRequest


def main() -> None:
    raw = sys.stdin.buffer.read().decode("utf-8")
    payload = json.loads(raw) if raw.strip() else {}
    request = EvidenceGenerationRequest(**payload)
    result = generate_astrological_evidence(request)
    sys.stdout.buffer.write(result.model_dump_json().encode("utf-8"))
    sys.stdout.buffer.flush()


if __name__ == "__main__":
    main()
