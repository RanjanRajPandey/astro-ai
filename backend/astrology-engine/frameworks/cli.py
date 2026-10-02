import json
import sys

from frameworks.classifier import classify_question_and_load_framework
from models.frameworks import QuestionClassificationRequest


def main() -> None:
    raw = sys.stdin.buffer.read().decode("utf-8")
    payload = json.loads(raw) if raw.strip() else {}
    request = QuestionClassificationRequest(**payload)
    result = classify_question_and_load_framework(request)
    sys.stdout.buffer.write(result.model_dump_json().encode("utf-8"))
    sys.stdout.buffer.flush()


if __name__ == "__main__":
    main()
