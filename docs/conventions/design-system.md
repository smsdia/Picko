# 디자인 시스템

위치: `core/designsystem/theme/`. 디자인 원본은 Figma `picko` 파일이다.

| 대상 | 위치 |
| --- | --- |
| Style Guide (색상, 타이포그래피) | 페이지 `Style Guide`, 노드 `76:2496` |
| 화면 시안 | 페이지 `GUI`, 노드 `12:3`. 같은 화면이 `Light`와 `Dark` 프레임 한 쌍으로 있다 (2560×1440) |

## 구성

| 파일 | 담는 것 |
| --- | --- |
| `theme/Color.kt` | 팔레트 상수, `PickoColors`, `lightPickoColors`, `darkPickoColors`, 그라데이션 |
| `theme/Type.kt` | `PickoFont`, `PickoTypography`, `defaultPickoTypography` |
| `theme/Theme.kt` | `PickoTheme`(Composable과 접근자 object), `staticCompositionLocalOf` 제공 |
| `theme/Image.kt` | 모드별 이미지 에셋 접근자 `PickoImages`(`PickoTheme.images`). 3D 핀, 그라데이션 배경 |
| `icon/PickoIcons.kt` | 아이콘 `ImageVector`. 아래 [컴포넌트](#컴포넌트)의 아이콘 규칙 참고 |
| `core/common/extensions/ModifierExt.kt` | `Modifier.dropShadowLevel1(shape)`, `Modifier.dropShadowLevel2(shape)` (그림자 외 공용 Modifier도 함께 있다) |
| `component/` | 상태 없는 UI 컴포넌트. 아래 [컴포넌트](#컴포넌트) 참고 |

## 색상

두 층으로 나눈다.

| 층 | 규칙 |
| --- | --- |
| 팔레트 | Style Guide 색을 Figma 이름 그대로 PascalCase 상수로 둔다(`BluePrimary`, `NavySurface`). 값만 있고 역할은 없다 |
| 역할 | `PickoColors`의 필드가 역할을 정한다. 라이트·다크 값은 `lightPickoColors`와 `darkPickoColors`가 각각 팔레트에서 골라 채운다 |

| Don't | Do |
| --- | --- |
| 화면에서 `BluePrimary` 같은 팔레트 상수를 직접 쓴다 | `PickoTheme.colors.accent`로 읽는다 |
| 화면에서 `if (darkTheme)`로 색을 고른다 | 역할 필드를 읽는다. 분기는 `lightPickoColors`와 `darkPickoColors`가 맡는다 |
| 화면에 `Color(0xFF...)`를 쓴다 | 팔레트와 역할에 없으면 Figma에서 확인해 추가한다 |

- 새 색은 팔레트 상수를 먼저 추가하고, 필요하면 `PickoColors` 필드를 더한다. 필드를 더하면 라이트와 다크 인스턴스를 모두 채운다.
- 다크 짝을 시안에서 확인하지 못한 값은 `TODO(figma)`로 표시한다. 이름으로 짝을 추정한 값은 `?`로 표시한다.
- `PickoColors`는 `@Immutable`이고 `Brush`(그라데이션)도 담는다.
- Figma `ANGULAR` 그라데이션은 `Brush.sweepGradient`로 옮긴다.

### 그라데이션

`cssLinearGradient(angleDeg, vararg colorStops)`는 CSS `linear-gradient`와 같은 방식으로 그린다. 각도는 위쪽이 0도이고 시계 방향으로 증가한다. 시작점과 끝점은 그려지는 크기에서 계산한다.

```kotlin
val ButtonPrimaryLightGradient = cssLinearGradient(
    168.69f,
    0.064f to Color(0xFF678AED),
    0.48f to Color(0xFF416DE9),
    0.864f to Color(0xFF2E5AD2),
)
```

## 이미지

| 항목 | 규칙 |
| --- | --- |
| 위치 | `res/drawable-nodpi/`. 이름은 `img_<이름>_light`, `img_<이름>_dark`. 폭이 큰 에셋은 WebP로 줄여 넣는다 |
| 모드 | `drawable-night/`는 시스템 설정만 따르고 `PickoTheme(darkTheme = ...)`으로 주입한 값과 어긋나므로 쓰지 않는다. `PickoImages`의 라이트·다크 인스턴스가 고른다 |
| 읽기 | `ImageBitmap.imageResource(PickoTheme.images.mapPin3d)`로 읽고 `Image(bitmap = ...)`로 그린다 |
| 크기 | 화면에서 그리는 크기의 2배를 넘기지 않는다. 디코딩한 비트맵은 가로×세로×4바이트이다 |
| 원본 | 심볼을 내보낸 PNG에는 배경이 채워질 수 있다. 투명 배경이 필요하면 Figma 원본 이미지(`rawImages`)를 쓴다 |

## 화면 틀과 부품

| 컴포넌트 | 위치 | 용도 |
| --- | --- | --- |
| `PickoScreenFrame` | `component/layout/` | 그라데이션 배경, 로고 머리글, 구분선을 두고 내용을 가운데에 맞추는 화면 틀. `showHeader = false`면 머리글을 숨긴다. `decoration`은 내용 뒤, `overlay`는 내용 위에 창 전체를 덮는 자리다 |
| `PickoMicBadge` | `component/indicator/` | 음성을 기다리는 원형 마이크 표시. 바깥 halo(Aurora Halo)가 돈다. `onDim = true`면 dim 위용으로 원이 투명하고 마이크가 흰색이다 |
| `PickoSuccessEmblem` | `component/indicator/` | 완료를 알리는 원형 체크 표시 |

## 그림자

Figma `shadow_level_1`, `shadow_level_2`를 `Modifier.dropShadow`로 옮긴 확장 함수다. 그림자는 도형 바깥에만 그려지므로 반투명한 면에도 안전하다.

| 함수 | 시안 값 | 쓰는 곳 |
| --- | --- | --- |
| `dropShadowLevel1(shape)` | Y 6, Blur 24, 검정 8% | 버튼, 연락처 액션 카드, 픽업 표시, 로딩 점, 지도 마커 |
| `dropShadowLevel2(shape)` | (Y 12, Blur 40, 검정 30%)와 (Y 3, Blur 4, 검정 16%)를 겹침 | 대화상자 |

`.clip`과 `.background`보다 앞에 두어야 그림자가 잘리지 않는다. 같은 `shape`를 넘긴다.

## 주야간

`PickoTheme(darkTheme: Boolean = isSystemInDarkTheme())`가 모드를 결정한다. 차량의 주야간 신호를 쓰게 되면 호출부에서 `darkTheme`에 값을 넘긴다. Material You(`dynamicColor`)는 쓰지 않는다.

`MaterialTheme` 안에서 `PickoTheme`이 제공된다. Material3 컴포넌트가 읽는 `primary`, `background`, `surface`와 각 `on*`만 `PickoColors`에서 매핑한다.

## 타이포그래피

| 항목 | 규칙 |
| --- | --- |
| 폰트 | Pretendard(`res/font`). Regular, Medium, SemiBold, Bold를 `PickoFont`로 연결한다 |
| 접근 | `PickoTheme.typography.title.titleLarge`처럼 그룹, 스타일 순으로 읽는다. 그룹은 `display`, `headline`, `title`, `body`, `label`, `tag`, `marker` |
| 크기 | Figma px를 그대로 sp로 쓴다. 기기가 160dpi라서 1px = 1dp = 1sp이다 |
| 줄 높이 | Figma가 px이면 같은 값의 sp. `%`이면 글자 크기에 곱해 sp. `Auto`이면 지정하지 않는다 |
| 자간 | Figma가 전부 0이므로 지정하지 않는다 |
| 폰트 패딩 | `includeFontPadding = false` |
| 제외 | Style Guide의 `ios/*` 스타일, Pleos 시스템 토큰 `Asta Sans` |

시안에서 줄 높이가 글자 크기보다 작은 스타일이 있다(`titleLarge` 40/34, `titleMedium` 36/26, `headlineLarge` 48/42, `headlineMedium` 39/38, `labelLarge` 32/24, `labelMedium` 30/26). 시안 값 그대로 쓰며, 글자가 잘리면 시안과 함께 확인한다.

## 컴포넌트

위치: `core/designsystem/component/<종류>/`. Figma `Component Asset` 섹션(노드 `260:5591`)의 심볼을 옮긴 것이다. Light와 Dark 심볼은 컴포넌트 하나로 합치고, 모드 차이는 `PickoTheme.colors`가 맡는다.

| 컴포넌트 | 위치 | 용도 |
| --- | --- | --- |
| `PickoButton` (`Primary`, `Secondary`) | `button/` | 그라데이션 버튼 |
| `PickoResultCard`, `PickoContactResultCard`, `PickoPlaceResultCard`, `PickoNearbyPlaceResultCard` | `card/` | 번호가 붙은 결과 카드. 앞의 것은 내용을 슬롯으로 받는 뼈대 |
| `PickoContactActionCard` | `card/` | 탑승자 이름과 번호, 링크 보내기 버튼 |
| `PickoPickupInfoCard`, `PickoNavigationStatusCard` | `card/` | 추천 픽업 장소와 도착 시간, 내비게이션 중 요약 |
| `PickoPickupIndicator` | `indicator/` | SMART PICKUP 표시 |
| `PickoLoadingIndicator` | `indicator/` | 점 세 개 로딩 표시 |
| `PickoMapMarker`, `PickoMapMarkerBadge`, `PickoMapMarkerLabel` | `marker/` | 차량, 탑승자, 픽업 장소 마커 (`PickoMapMarkerType`) |
| `PickoDialog` | `dialog/` | 제목, 설명, 버튼 두 개의 확인 대화상자 |

| 규칙 | 내용 |
| --- | --- |
| 이름 | `PickoXxx`. 화면 이름이나 도메인 모델을 알지 못하고 값만 받는다 |
| 상태 | 컴포넌트는 상태를 갖지 않는다. 클릭은 람다로 올린다. 예외는 `PickoLoadingIndicator`의 점 순서이다 |
| 문구 | 사용자에게 보이는 문구는 호출하는 쪽이 `String`으로 넘긴다. 시안에 고정된 라벨(통행료, 픽업 장소 등)만 컴포넌트가 `strings.xml`에서 읽는다 |
| 색·글꼴 | `PickoTheme.colors`와 `PickoTheme.typography`만 읽는다. 팔레트 상수나 `if (darkTheme)`를 쓰지 않는다 |
| 아이콘 | Figma SVG를 `icon/PickoIcons.kt`의 `ImageVector`로 옮긴다. `painterResource`는 벡터 XML을 다시 파싱하고 `Painter`로 감싸는 단계를 거치므로 쓰지 않고 `Icon(imageVector = ...)`로 쓴다. 모드에 따라 색이 바뀌는 것은 검정 한 색으로 만들어 `tint`에 역할 색상을 준다. 마커 아이콘(`Marker*`)은 모드와 무관한 색을 담고 있어 `Image`로 그린다. `res/drawable/`에는 카카오 지도 라벨처럼 `Drawable`이 필요한 것만 둔다 |
| 크기 | Figma px를 dp로 그대로 쓴다. 폭은 `fillMaxWidth`로 두고, 높이는 최소값을 둔다 |
| Preview | 파일마다 `private` Preview를 두고 `@PickoCarPreview`를 붙인다. 차량 앱 영역(1700×1185)의 라이트와 다크 두 장이 나온다. 내용은 `PickoPreviewSurface`로 감싼다 |

`PickoMapMarker`는 Compose에서만 그려진다. 카카오 지도 라벨은 비트맵만 받으므로 ([kakao-maps](../references/kakao-maps.md)) 지도에 올리려면 비트맵 변환이 따로 필요하다.
