# 보안 규칙

## 키와 비밀값

- API 키, 토큰, 서명 정보는 `local.properties` 또는 추적하지 않는 파일에 둔다. 소스, 문서, 로그, 커밋에 넣지 않는다.
- 빌드에 필요한 값은 `BuildConfig` 등으로 주입하되, 값 자체를 저장소에 올리지 않는다.
- Pleos 콘솔의 **클라이언트 ID와 클라이언트 시크릿**(TextToSpeech, SpeechToText 등 서버 연결 SDK용)은 `local.properties`의 `pleos.client.id`, `pleos.client.secret`에만 두고 `BuildConfig.PLEOS_CLIENT_ID`, `PLEOS_CLIENT_SECRET`로 주입한다. 이 값이 없어도 빌드가 통과해야 하며(빈 값이면 가짜 구현 사용), 실제 값을 코드·문서·로그에 쓰지 않는다.
- 카카오 네이티브 앱 키는 `local.properties`의 `kakao.native.key`에만 두고 `BuildConfig.KAKAO_NATIVE`로 주입한다. 카카오 지도 SDK는 디버그 로그(logcat)에 인증 요청 헤더의 앱 키를 출력하므로, 로그를 공유하거나 첨부하기 전에 확인한다.
- Pleos Maven 저장소에 인증이 필요하면 그 값도 `local.properties` 또는 사용자 Gradle 설정에 두고, 저장소 파일에는 올리지 않는다.
- 이 규칙을 어긴 변경은 완료로 보지 않는다.

## 토큰과 세션

- 토큰은 Android Keystore 등 안전한 저장소에 둔다.
- 차량은 공유 기기다. 로그아웃과 계정 전환을 지원한다.
- 탑승자 세션 토큰은 일회성이며 재사용하지 않는다.

## 개인정보

- 위치, 음성 수집은 필요한 이유를 명시하고 이해하기 쉬운 동의 흐름을 둔다.
- 불필요한 개인정보를 저장하지 않는다.
- 로그에 위치, 식별자 등 개인정보를 남기지 않거나 마스킹한다.
- 외부 전송 데이터의 차량·사용자 식별자는 익명화 또는 가명화 정책을 따른다.

## 미확정

- 키 보관 방식 상세 (`local.properties` + 주입 방식, 백엔드 API 키 유무)
- 탑승자 위치 데이터 보관 기간
