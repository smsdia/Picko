#!/bin/bash
# 되돌리기 어려운 git 명령을 차단한다. 일반 `git push` 는 사용자가 요청한 경우에만 하며,
# settings.json 의 ask 권한으로 매번 확인을 받는다. 강제 푸시는 항상 차단한다.

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command')

DANGEROUS_PATTERNS=(
  "git push.* (-f|--force|--force-with-lease)( |$)"
  "git reset --hard"
  "git clean -fd"
  "git clean -f"
  "git branch -D"
  "git checkout \."
  "git restore \."
)

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
  if echo "$COMMAND" | grep -qE "$pattern"; then
    echo "BLOCKED: '$COMMAND' matches dangerous pattern '$pattern'. The user has prevented you from doing this." >&2
    exit 2
  fi
done

exit 0
