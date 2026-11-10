# Picko 문서

| 작업 | 읽을 문서 |
| --- | --- |
| 패키지 구조, 의존성 방향, Repository, Pleos SDK 경계, Fake·Hilt 교체 | [data-layer](architecture/data-layer.md) |
| UseCase를 만들지 말지, 비즈니스 정책 | [domain-layer](architecture/domain-layer.md) |
| Route, Screen, State, SideEffect, Action, ScreenState, ViewModel | [presentation](architecture/presentation.md) |
| NavHost, 화면 이동, Back | [navigation](architecture/navigation.md) |
| 클래스, 파일, 패키지 이름 | [naming](conventions/naming.md) |
| 코드 스타일, 주석, 코드 가독성 | [code-style](conventions/code-style.md) |
| 문자열, `contentDescription` | [resources](conventions/resources.md) |
| 작업 루프, 검증 게이트, 권한과 승인 | [HARNESS](HARNESS.md) |
| Pleos 앱 카테고리, 주행 제한, 디자인, 기기 환경 | [PLEOS](PLEOS.md) |
| 키, 토큰, 개인정보, 로그 | [SECURITY](SECURITY.md) |
| Pleos SDK 목록, 진입점, 권한 (SDK 코드를 쓰기 전) | [pleos-sdk-index](references/pleos-sdk-index.md) |

| 폴더 | 담는 것 |
| --- | --- |
| `architecture/` | 코드가 어떻게 나뉘는가 |
| `conventions/` | 코드와 리소스를 어떻게 쓰는가 |
| `references/` | 외부 문서 요약 |

## 문서 작성

| 규칙 | 내용 |
| --- | --- |
| 형식 | 제목과 표 헤더는 한국어. 서술 대신 표, Don't/Do 표, 코드 중심으로 쓴다. 코드 주석은 한국어 |
| 한 주제 한 문서 | 같은 규칙을 두 곳에 쓰지 않고 링크한다 |
| 확정된 규칙만 | 제안과 미확정 사항은 쓰지 않는다. 미확정은 [AGENTS.md](../AGENTS.md)에 둔다 |
| 코드 예시 | 저장소에 실제로 있는 코드만 쓴다. 코드가 생기면 그때 예시를 추가한다 |
| 규칙 변경 | `architecture/`, `conventions/`의 규칙을 새로 정하거나 바꾸는 것은 사용자에게 먼저 묻는다 |
| 사실 갱신 | 코드 변경으로 달라진 경로, 이름, 예시는 묻지 않고 같은 변경에서 고친다 |
| 정리 | 필요 없어진 규칙은 지운다 |
