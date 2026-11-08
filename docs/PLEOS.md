# Pleos Connect 플랫폼 규칙

출처: document.pleos.ai (vehicle-app-planning-guide, vehicle-app-developer-guide, connect design guide). 규칙이 불확실하면 원문을 다시 확인한다.

## 앱 분류

- 카테고리: Mobility Services. 주행 중에는 위치 기반 즉시 정보와 주차·충전 장소 탐색 같은 기능이 허용되는 범주다.
- 주행 중 UI와 동작은 `CarUxRestrictions` 상태에 따라 축소하거나 비활성화한다.

## 주행 중 금지·제한

- 금지: 영상 재생, 게임, 웹 브라우징, 과한 애니메이션, 장식적 반복 움직임.
- 텍스트 입력을 최소화한다. 프리셋, 최근 항목, 음성 입력을 우선한다.
- 자주 쓰는 기능은 음성(Gleo AI)으로 쓸 수 있어야 한다.

## 디자인

- 1~2초 안에 파악되는 정보량만 보여 준다.
- 응답은 0.25초 이내. 지연되면 시각 피드백을 준다.
- 글자·아이콘 대비 4.5:1 이상. 야간·주간 모드에 대응한다.
- 텍스트: 한 줄 80자 이하, 문단 3줄 이하. 중요 정보 5mm 이상, 보조 정보 3mm 이상.
- 터치 영역은 크게, 한 손으로 조작 가능하게 만든다.

## 기술 요구

- CCP(회전 컨트롤러) 지원, IME 창 리사이즈 대응, 오디오 포커스 관리.
- 기준 환경: API 34, minSdk 28 이상. 프로젝트 설정은 `minSdk 28`, `targetSdk 34`이고, `compileSdk`는 최신 AndroidX가 요구하는 값(현재 37)을 쓴다. 에뮬레이터는 Pleos Connect Emulator.
- 차량 상태 조회·제어 API는 권한과 지원 여부를 확인한다.

## 화면 구성 (분할 창)

- Pleos Connect는 앱 영역에 최대 2개 앱을 동시에 표시하고, 차량 정보를 보여 주는 Driving view는 고정이다. 앱은 가로·세로 모두에서 동작해야 하며 표시 영역이 바뀔 수 있다. (원문: vehicle-app-planning-guide/app-structure-screen-model, docs-design/getting-started/layout)
- 레이아웃은 고정 크기를 피하고 가로·세로 모두에서 동작하게 만든다.
- 화면 상태는 ViewModel에 두고, foreground/background 잦은 전환과 프로세스 재생성에서도 유지한다. 화면에서 사라져도 앱은 종료되지 않는다.
- 이 분할 동작은 Pleos Connect Emulator에서만 확인할 수 있다. 폰 에뮬레이터로는 재현되지 않는다.

## Back 하드 키 (문서상 필수)

원문: vehicle-app-developer-guide/ccp-rotary-controller-development

- 최상위 화면에서 Back이 눌리면 앱을 종료하지 않고 `moveTaskToBack(true)`로 숨긴다. `finish()`와 `finishAffinity()`는 쓰지 않는다. 종료 방식이 틀리면 심사에서 반려될 수 있다.
- 하위 화면과 팝업에서는 먼저 정상적으로 뒤로 간다. Compose에서는 `BackHandler` 또는 `OnBackInvokedDispatcher` 계열 API를 쓴다.
- 앱이 숨겨져도 프로세스가 종료될 수 있으므로 필요한 상태를 저장하고 복원한다.

## CCP(로터리 컨트롤러)

- 사전 설치 앱은 CCP 지원이 필수이고, 사용자 설치 앱(Pleos App Market)은 선택이다.
- CCP를 지원하지 않으면 모든 필수 조작이 터치만으로 가능해야 하고, `<application>`에 `<meta-data android:name="com.hmg.rotary.ROTARY_SUPPORTED" android:value="false" />`를 선언한다.
- CCP를 지원하면 모든 주요 UI에서 포커스 이동, 리스트 스크롤(회전), 선택(클릭), 뒤로가기가 되어야 하고 터치 전용 영역이 없어야 한다. 구현은 `focusable`, `FocusRequester`, `KeyEvent`를 쓴다.
- 테스트는 Pleos Connect Emulator에서 한다.

## 미확정

- Pleos 앱 마켓 심사·배포 정책 세부
- 배포 형태: 확정 전까지 Pleos App Market 배포(사용자 설치 앱)로 가정한다.
- CCP 지원 여부: 미확정. 확정 전에는 CCP 관련 코드와 `ROTARY_SUPPORTED` 메타데이터를 추가하지 않는다. 필수 조작은 터치만으로도 가능하게 만든다.
- 매니페스트 `screenOrientation` 설정의 정확한 의미와 윈도우 크기 수치 (원문과 에뮬레이터로 확인)
