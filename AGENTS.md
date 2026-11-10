# Picko 에이전트 가이드

현대모비스 Pleos Connect SDK 기반 음성 픽업 앱. Gleo AI 음성과 실시간 위치로 안전한 정차 구역을 추천한다.
Kotlin, Compose, 단일 `app` 모듈, Hilt. 버전은 `gradle/libs.versions.toml`. 자체 서버는 없다.
검증: `./gradlew testDebugUnitTest lintDebug assembleDebug` ([HARNESS](docs/HARNESS.md)).

이 문서가 진입점이며 충돌하면 이 문서가 우선한다.

## 수정 전

[docs/index.md](docs/index.md)에서 작업에 해당하는 행을 찾아 그 문서를 먼저 읽고 따른다. SDK 코드를 쓰기 전에는 [pleos-sdk-index](docs/references/pleos-sdk-index.md)를 읽는다.

## 불변 규칙

- Pleos SDK API는 추측해서 작성하지 않는다. 문서(document.pleos.ai)나 저장소에서 확인되지 않으면 사용자에게 묻는다.
- 비밀값, 키, 로컬 환경 값은 `local.properties` 등 추적하지 않는 파일에 둔다. 코드와 문서에 쓰지 않는다. ([SECURITY](docs/SECURITY.md))
- 새 의존성은 필요한 이유와 영향을 확인한 뒤 추가한다.
- 커밋과 푸시는 사용자가 명시적으로 요청할 때만 한다.
- 요청 범위의 코드와 테스트를 바꾸면, 그 변경으로 달라진 문서의 사실(경로, 이름)을 같은 변경에서 고친다. 규칙 자체를 바꾸는 것은 먼저 묻는다. ([index](docs/index.md#문서-작성))

## 미확정 (관련 작업 전에 사용자에게 먼저 확인)

- 탑승자 웹 페이지의 제작·호스팅 주체
- Pleos Connect REST/WebSocket API 명세
- 탑승자에게 URL을 전달하는 방식
- Gleo 음성 호출 함수(최대 3개)의 목록과 파라미터. 개발 시점에 사용자와 정한다. 정해지기 전에는 `schema.json`과 관련 코드를 만들지 않는다.
- 서체, 디자인 토큰, Figma 수치 변환 규칙 (디자인 시안 확정 후)
- 음성 안내 문구를 만드는 위치 (Gleo 함수 목록이 정해질 때)
