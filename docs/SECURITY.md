# 보안 규칙

## 키와 비밀값

- API 키, 토큰, 서명 정보는 `local.properties` 또는 추적하지 않는 파일에 둔다. 소스, 문서, 로그, 커밋에 넣지 않는다.
- 빌드에 필요한 값은 `BuildConfig` 등으로 주입하되, 값 자체를 저장소에 올리지 않는다.
- Pleos STT와 TTS는 온디바이스 모드(`Mode.ON_DEVICE`)로만 쓰므로 앱 등록이 필요 없고 Pleos 콘솔의 클라이언트 ID와 시크릿을 앱에 두지 않는다(`BuildConfig.PLEOS_CLIENT_*`도 없다). 서버 연결 모드(`HYBRID`, `SERVER`)를 쓰게 되면 그 값은 `local.properties`에만 두고 `BuildConfig`로 주입하며, 값이 없어도 빌드가 통과해야 한다. 실제 값을 코드·문서·로그에 쓰지 않는다.
- 카카오모빌리티 길찾기 REST API 키는 `local.properties`의 `kakao.platform.key`에만 두고 `BuildConfig.KAKAO_PLATFORM`으로 주입한다(`KakaoDirectionsDataSource`의 `Authorization: KakaoAK` 헤더). 키가 없으면 길찾기를 건너뛰고 가짜 경로를 쓴다.
- 카카오 네이티브 앱 키는 `local.properties`의 `kakao.native.key`에만 두고 `BuildConfig.KAKAO_NATIVE`로 주입한다. 카카오 지도 SDK는 디버그 로그(logcat)에 인증 요청 헤더의 앱 키를 출력하므로, 로그를 공유하거나 첨부하기 전에 확인한다.
- Pleos Maven 저장소에 인증이 필요하면 그 값도 `local.properties` 또는 사용자 Gradle 설정에 두고, 저장소 파일에는 올리지 않는다.
- 이 규칙을 어긴 변경은 완료로 보지 않는다.

## 토큰과 세션

- 서버 세션 토큰은 Android Keystore의 AES-256-GCM 키로 암호화해 DataStore에 저장한다. 그 저장소는 백업과 기기 이전에서 제외한다([data-layer](architecture/data-layer.md#세션-토큰-저장)). 토큰과 탑승자 전화번호는 로그에 남기지 않는다.
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
