# 바로태움 (PickItUp) 에이전트 가이드

현대모비스 Pleos Connect SDK 기반 음성 픽업 앱. Gleo AI 음성과 실시간 위치로 안전한 정차 구역을 추천한다.
일반 Android 프로젝트(Kotlin, Compose, 단일 `app` 모듈)에 Pleos SDK를 추가해 차량 기능을 연동한다. 자체 서버는 없다.

이 문서는 목차다. 세부 규칙은 아래 문서가 단일 기준이며, 해당 작업 전에 먼저 읽고 따른다. 충돌하면 이 문서가 우선한다.

## 문서 지도

- 패키지 구조, 계층 책임, 의존성 방향, MVVM·UiState·Hilt, SDK 경계: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- 구현, 디버깅, 테스트, 리뷰의 작업 루프와 검증 게이트: [docs/HARNESS.md](docs/HARNESS.md)
- Pleos 앱 카테고리, 주행 제한, 디자인 규칙, 기기 환경: [docs/PLEOS.md](docs/PLEOS.md)
- 키, 토큰, 개인정보, 로그: [docs/SECURITY.md](docs/SECURITY.md)
- Pleos SDK 목록, 진입점, 권한, 원문 링크 (SDK 코드를 쓰기 전에 먼저 읽는다): [docs/references/pleos-sdk-index.md](docs/references/pleos-sdk-index.md)

## 불변 규칙

- Pleos SDK API는 추측해서 작성하지 않는다. 문서(document.pleos.ai)나 저장소에서 확인되지 않으면 사용자에게 묻는다.
- 비밀값, 키, 로컬 환경 값은 `local.properties` 등 추적하지 않는 파일에 둔다. 코드와 문서에 쓰지 않는다.
- 새 의존성은 필요한 이유와 영향을 확인한 뒤 추가한다.
- 커밋과 푸시는 사용자가 명시적으로 요청할 때만 한다.
- 요청 범위의 코드, 테스트, 관련 문서를 함께 갱신한다.

## 미확정 (관련 작업 전에 사용자에게 먼저 확인)

- 탑승자 웹 페이지의 제작·호스팅 주체
- Pleos Connect REST/WebSocket API 명세
- 탑승자에게 URL을 전달하는 방식
- Gleo 음성 호출 함수(최대 3개)의 목록과 파라미터. 개발 시점에 사용자와 정한다. 정해지기 전에는 `schema.json`과 관련 코드를 만들지 않는다.
