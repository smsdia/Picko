# Picko 에이전트 가이드

현대모비스 Pleos Connect SDK 기반 음성 픽업 앱. Gleo AI 음성과 실시간 위치로 안전한 정차 구역을 추천한다.
Kotlin, Compose, 단일 `app` 모듈, Hilt. 버전은 `gradle/libs.versions.toml`. 자체 서버는 없다.
검증: `./gradlew testDebugUnitTest lintDebug assembleDebug` ([HARNESS](docs/HARNESS.md)).

이 문서가 진입점이며 충돌하면 이 문서가 우선한다.

## 수정 전

[docs/index.md](docs/index.md)에서 작업에 해당하는 행을 찾아 그 문서를 먼저 읽고 따른다. SDK 코드를 쓰기 전에는 [pleos-sdk-index](docs/references/pleos-sdk-index.md)를 읽는다. 카카오 API를 구현하거나 호출해 보기 전에는 [kakao-api-quota](docs/references/kakao-api-quota.md)를 읽고, 호출 수가 쿼터를 넘지 않는지 확인한다.

## 불변 규칙

- Pleos SDK API는 추측해서 작성하지 않는다. 문서(document.pleos.ai)나 저장소에서 확인되지 않으면 사용자에게 묻는다.
- 비밀값, 키, 로컬 환경 값은 `local.properties` 등 추적하지 않는 파일에 둔다. 코드와 문서에 쓰지 않는다. ([SECURITY](docs/SECURITY.md))
- 새 의존성은 필요한 이유와 영향을 확인한 뒤 추가한다.
- 커밋과 푸시는 사용자가 명시적으로 요청할 때만 한다.
- 요청 범위의 코드와 테스트를 바꾸면, 그 변경으로 달라진 문서의 사실(경로, 이름)을 같은 변경에서 고친다. 규칙 자체를 바꾸는 것은 먼저 묻는다. ([index](docs/index.md#문서-작성))

## 미확정 (관련 작업 전에 사용자에게 먼저 확인)

- 탑승자 웹 페이지의 제작·호스팅 주체
- Pleos Connect REST/WebSocket API 명세
- 서버 연동(Picko 서버 `<서버 주소>`, 스웨거 `/v3/api-docs`)으로 정해진 것: 탑승자 문자는 서버가 보낸다(`POST /api/sessions`에 `passengerPhone`). 탑승자 위치는 WebSocket으로 받는다([picko-server-api](docs/references/picko-server-api.md)). 아직 정해지지 않은 것: 만료·없는 토큰 응답 규약과 세션 유효 시간, 취소 후 토큰 재사용 여부, REST의 서버 인증 방식
- Gleo 음성 호출 함수(최대 3개)의 목록과 파라미터. 개발 시점에 사용자와 정한다. 정해지기 전에는 `schema.json`과 관련 코드를 만들지 않는다.
- 디자인 토큰 중 시안에서 확인하지 못한 값: 다크 짝이 없는 색, 일부 그라데이션. 코드의 `TODO(figma)`와 [design-system](docs/conventions/design-system.md) 참조
- 컴포넌트에 아직 반영하지 않은 시안 요소: 비활성 버튼, 로딩 점이 바뀌는 속도, 스플래시 표시 시간
- 픽업 흐름의 연동 중 가짜 구현인 것: 주변 장소 추천(서울역 근처 가짜), 픽업 지점을 고르기 전에 링크를 보내는 경로(서버는 추천 후 세션 생성이라 맞지 않는다), 목적지 경로(지금은 쓰지 않는다. 차량 경로는 카카오모빌리티 자동차 길찾기로 연동했다. 도보 길찾기는 제휴 파트너 전용이라 사전 제휴가 필요하고, 상업 이용 제휴 여부는 미확인). 차량 소요 시간과 목적지 안내는 차량 내비(NaviHelper)에 맡겼고, 내비는 경로 좌표를 주지 않는다. [data-layer](docs/architecture/data-layer.md#기능별-구현-상태) 참조
- 탑승자 도보: 도보 길찾기가 제휴 전용이라 지도에는 탑승자 마커만 두고 도보 경로선은 그리지 않는다. "도보 약 N분"은 탑승자 위치와 픽업 지점의 직선거리를 분당 75m로 나눈 추정값이고(탑승자 웹과 같은 식), 탑승자 위치가 올 때마다 마커와 함께 갱신한다
- 픽업 완료 뒤 `목적지 안내하기`: 목적지를 정하지 않고 차량 내비 화면만 띄운다(`requestNavigationScreen(Foreground)`). 목적지를 앱이 넘기는 안내(`requestRoute`)는 목적지가 어디서 오는지(새 Gleo 파라미터, 완료 후 음성 질문, 탑승자 페이지 중) 정해지지 않아 쓰지 않는다
- 내비 안내 시작 뒤 우리 앱이 가려지는 문제: `목적지 안내하기`는 내비 화면을 띄우고 흐름을 끝내므로 우리 앱이 가려져도 문제가 없다. 분할을 앱이 켜는 방법은 없다 ([pleos-sdk-index](docs/references/pleos-sdk-index.md#navihelper)). 실차에서 분할 동작은 확인 전이다
- 주행 중 `CarUxRestrictions`에 따른 UI 축소(`docs/PLEOS.md`)는 구현 전이다. 앱은 `distractionOptimized`만 표시해 주행 중에도 열린다
- `음악 재생하기`의 동작(`PickupFlowViewModel.onMusicClicked`의 `TODO`)
- 연락처나 장소를 못 찾았을 때의 수동 검색 화면(시안 없음). 지금은 음성 재요청 오버레이만 있다
- `RouteMap`(카메라 맞춤, 마커 겹침, 제스처 막기)은 빌드로만 확인했고 Pleos Connect Emulator에서는 확인 전이다
- 말로 하는 픽업 요청을 읽는 규칙(`SpokenPickupParser`)은 정해진 말투만 처리하는 규칙 기반이다. 더 넓은 말투가 필요한지, 앱이 직접 듣는 STT 경로를 계속 둘지 Pleos의 공식 Gleo 호출 방법 확인 뒤 정한다
- 음성 안내 문구를 만드는 위치 (Gleo 함수 목록이 정해질 때). 지금 앱이 직접 읽는 안내는 홈·재요청 오버레이의 "연락처를 찾지 못함", "알아듣지 못함" 두 가지뿐이고 `strings.xml`에 둔다
- `TODO(vehicle-info)`: 서버가 필수로 받는 차종(`vehicleModel`)과 번호판(`vehiclePlate`)은 임시 고정값을 보낸다. Pleos Vehicle SDK 공식 문서에서 이 값을 주는 API를 확인하지 못했다(`CarInfo.getCRNInfo`가 번호판인지는 문서에 없다). 값을 얻는 방법(SDK 확인, 사용자 입력, 서버 처리)을 정한다. `Vehicle` 의존성은 아직 추가하지 않았다
- 앱이 꺼졌다 켜진 뒤 진행 중이던 픽업을 화면에서 이어가는 것: 토큰은 암호화해 저장하고 취소와 새 세션 시작 때 정리하는 데만 쓴다. 이어가려면 앱 시작 때 저장된 토큰으로 WebSocket을 다시 연결하고 세션 상태를 알아내야 하는데, 서버는 상태를 보관하지 않아 재접속 직후에는 상대가 다음 위치를 보내기 전까지 아무것도 오지 않는다
- 카카오 로컬 키워드 검색(장소 검색)은 구현했지만 실제 호출은 확인 전이다. 카카오 디벨로퍼스에서 로컬 API 사용 설정이 켜져 있는지, 길찾기 응답의 `summary.fare.toll` 형태가 맞는지 확인한다
- 픽업 완료 때 서버 세션 닫기: 서버에 완료 API가 없어 취소 API(사유 `pickup_completed`)로 닫는다. 그래서 탑승자 웹에 "운전자 요청으로 픽업이 취소되었어요"가 뜬다. 서버에 완료 처리와 완료 안내를 요청할지 정한다
