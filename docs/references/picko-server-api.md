# Picko 서버 API

> 출처: 스웨거 `<서버 주소>/v3/api-docs`(확인일 2026-11-15)와 서버 담당 쪽에 전달한 WebSocket 프로토콜 명세(사용자가 붙여 준 내용). 서버는 우리가 만들지 않는다. 프로토콜 이름과 형식은 앱에서 바꾸지 않는다.

## REST (`core/network`의 `@PickoServer`)

| 호출 | 용도 | 앱에서 |
| --- | --- | --- |
| `POST /api/sessions` | 세션 생성. 응답의 `token`이 세션 토큰이고, 요청에 `passengerPhone`이 있으면 서버가 문자를 보낸다(`smsSent`, `smsDetail`) | `PickupLinkRepositoryImpl.sendLink` |
| `POST /api/sessions/cancel` | 세션 취소(토큰, 사유) | `PickupLinkRepositoryImpl.cancelSession`(사유 `driver_canceled`), `completeSession`(사유 `pickup_completed`) |
| `GET /api/pickup/recommend` | 차량·목적지 좌표로 픽업 후보 추천 | 쓰지 않는다 (주변 장소 추천은 가짜) |

- 문자는 서버가 SOLAPI로 보낸다. 서버 아웃바운드 IP가 SOLAPI에 등록되어 있지 않으면 `smsSent=false`, `smsDetail`에 `http_403`이 온다(2026-11-15 확인).
- 서버에 세션 완료 API가 없어 픽업 완료도 취소 API로 닫는다. 탑승자 웹은 사유와 상관없이 "운전자 요청으로 픽업이 취소되었어요"를 보여 준다(`handleCanceled`, 2026-11-15 `app.js`). 소켓만 닫으면 웹은 세션이 계속 열려 있는 것으로 보인다.
- 스웨거에 `POST /api/sessions/redeem`, `GET /api/sessions/me`가 있지만 탑승자 웹용이라 앱은 쓰지 않는다.

## WebSocket (`wss://<서버 호스트>/ws/vehicle?token=<세션 토큰>`)

인증은 쿼리의 토큰뿐이다. 서버는 상태를 보관하지 않아서 다시 접속하면 상대가 다음 프레임을 보내기 전까지 아무것도 오지 않는다.

| 방향 | 메시지 |
| --- | --- |
| 차량 → 서버 → 탑승자 | `{"type":"vehicle_location","lat":<double>,"lng":<double>}` |
| 서버 → 차량 | `{"type":"pedestrian_location","lat":<double>,"lng":<double>}` |
| 서버 → 차량 | `{"type":"session_canceled","reason":"<string>"}` |

앱 쪽 동작(`data/pickuplink/datasource/PickoVehicleSocket`):

| 규칙 | 내용 |
| --- | --- |
| 중복 접속 | 연결 중이거나 연결된 같은 토큰으로 `connect`를 다시 부르면 아무것도 하지 않는다 |
| 재접속 | 실패하거나 정상 종료(1000)가 아니게 닫히면 1초에서 시작해 두 배씩, 최대 30초 간격으로 다시 접속한다 |
| 재접속 중단 | `session_canceled`를 받았을 때, 서버가 `invalid_token`으로 닫았거나 접속을 401·403으로 거절했을 때, 앱이 `close()`했을 때 |
| 알 수 없는 메시지 | 내용을 남기지 않고 무시한다 |
| 로그 | 주소에 토큰이 있어 요청 로그를 남기는 인터셉터가 없는 전용 OkHttpClient(`@PickoSocket`)를 쓴다 |
| 확인 | 단위 테스트는 메시지 해석과 대기 시간만 본다. 2026-11-15 에뮬레이터(Connect v2.1.0)에서 세션 생성, 문자 수신(SOLAPI IP 등록 뒤), 휴대폰에서 위치를 공유하면 `pedestrian_location`을 받아 경로 화면으로 넘어가는 것까지 확인했다. 차량 위치가 탑승자 웹에 보이는지, `session_canceled` 수신과 재접속 안 함, 재접속 backoff는 확인 전이다 |

## 탑승자 웹이 쓰는 데이터

> 출처: 웹 정적 파일 `<서버 주소>/app.js`와 `index.html`(확인일 2026-11-15). 서버 코드는 읽지 못했다.

| 항목 | 웹의 동작 |
| --- | --- |
| 세션 정보 | `POST /api/sessions/redeem`으로 토큰을 쿠키로 바꾸고 `GET /api/sessions/me`로 읽는다. 픽업 지점(`pickupLat`, `pickupLng`, `pickupName`), 차량(`vehicleModel`, `vehiclePlate`, `vehicleColor`), `vehicleEtaMin`을 쓴다 |
| 차량 도착 시간 | 처음에는 세션의 `vehicleEtaMin`(우리가 세션 생성 때 보낸 값)을 보여 준다. 차량 위치(`vehicle_location`)가 오면 차량과 픽업 지점의 직선거리 ÷ 400m/분(시속 24km)으로 다시 계산한다. 서버가 `{"type":"eta","minutes":<n>}`를 보내면 그 값을 쓰는 분기도 있으나 실제로 오는지는 확인하지 못했다 |
| 탑승자 도보 시간 | 서버가 주는 값이 아니다. 웹이 탑승자 위치와 픽업 지점의 직선거리 ÷ 75m/분(시속 4.5km)을 반올림해 "도보 약 N분"(최소 1분)으로 보여 준다. 앱도 같은 식을 쓴다(`estimateWalkMinutes`) |
| WebSocket | 탑승자는 `/ws/pedestrian`에 접속해 `pedestrian_location`을 보내고 `vehicle_location`, `eta`, `session_canceled`를 받는다 |
| 앱에서 본 서버 메시지 | 에뮬레이터에서 받은 메시지의 키는 `type`, `lat`, `lng`뿐이었다(도보 시간 필드 없음) |
