# 카카오 지도 참조

> 출처: https://apis.map.kakao.com/android_v2/ (확인일 2026-11-11). 요약이며 원문 시그니처는 사용 전에 확인한다.

## 설정

| 항목 | 값 |
| --- | --- |
| 좌표 | `com.kakao.maps.open:android:2.15.2` (`libs.kakao.maps`). 최신 릴리스는 저장소의 `maven-metadata.xml`로 확인한다 |
| 저장소 | `https://devrepo.kakao.com/nexus/repository/kakaomap-releases/` (`settings.gradle.kts`, `includeGroup("com.kakao.maps.open")`) |
| 초기화 | `PickoApplication`에서 `KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE)` |
| 키 | `local.properties`의 `kakao.native.key` ([SECURITY](../SECURITY.md)) |
| 키 해시 | 카카오 개발자 콘솔 → 플랫폼 → Android에 패키지명과 함께 등록한다. 디버그 해시는 `keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android \| openssl sha1 -binary \| openssl base64` |
| 권한 | `INTERNET` |

## 구현 (`presentation/main/component/PickupMap.kt`)

- `MapView`를 `AndroidView`로 감싼다. `start(MapLifeCycleCallback, KakaoMapReadyCallback)`로 시작하고 `ON_RESUME`/`ON_PAUSE`에 `resume()`/`pause()`, 해제할 때 `finish()`를 호출한다.
- 초기 카메라는 `KakaoMapReadyCallback`의 `getPosition()`/`getZoomLevel()`로 준다. 이후 이동은 `moveCamera(CameraUpdateFactory.newCenterPosition(latLng, zoom))`.
- POI는 `labelManager.layer.addLabel(LabelOptions.from(latLng).setStyles(...))`.
- **벡터 드로어블은 `LabelStyle.from(context, resId)`로 그려지지 않았다** (에뮬레이터 실측: 라벨은 추가되지만 보이지 않음). 비트맵으로 바꿔 `LabelStyle.from(bitmap).setApplyDpScale(false).setAnchorPoint(0.5f, 1f)`로 넘기면 보인다.
- 인증 실패는 `MapLifeCycleCallback.onMapError`로 온다(키 오류, 키 해시 미등록 등).
- 지도는 Preview에서 그릴 수 없어 `LocalInspectionMode`일 때 건너뛴다.
