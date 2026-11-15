# 도메인 계층

`domain`은 선택적이다. 아래 조건일 때만 UseCase를 만든다.

| 항목 | 규칙 |
| --- | --- |
| 만드는 경우 | Repository를 둘 이상 조합한다. 또는 둘 이상의 화면이 같은 비즈니스 정책(판단 기준)을 공유한다 |
| 만들지 않는 경우 | Repository 호출 하나를 그대로 넘기기만 한다. ViewModel이 Repository를 바로 쓴다 |
| 이름 | 동사로 시작하고 `UseCase`로 끝낸다. 클래스 하나에 동작 하나 (`RecommendSpotUseCase`) |
| 상태 | 가변 상태를 갖지 않는다. `@Singleton`을 쓰지 않는다 |
| 의존 | Repository 인터페이스와 다른 UseCase. Android 타입과 UI 타입은 쓰지 않는다 |
| 반환 | 도메인 값을 돌려준다. 화면 표시용 타입은 ViewModel에서 변환한다 |
| 위치 | `domain/<기능>/model`, `domain/<기능>/usecase` |

| Don't | Do |
| --- | --- |
| Repository 메서드 하나만 호출하는 UseCase | ViewModel이 Repository를 직접 호출 |
| `object SpotPolicy`, `Util`, `Helper` | 동작 이름의 `XxxUseCase` 클래스 |
| UseCase가 UI 타입이나 문자열을 반환 | 도메인 값을 반환하고 ViewModel에서 변환 |

## 예시

`ListenForPickupUseCase`는 두 Repository를 조합한다. 음성 인식(`SpeechRecognitionRepository`)으로 요청을 읽고, 탑승자 연락처가 있는지(`ContactRepository`) 확인한다. 홈과 재요청 오버레이가 같은 정책(연락처가 없으면 흐름을 시작하지 않고 듣던 화면에 머문다)을 쓴다.

```kotlin
class ListenForPickupUseCase @Inject constructor(
    private val speechRecognitionRepository: SpeechRecognitionRepository,
    private val contactRepository: ContactRepository,
) {
    operator fun invoke(): Flow<PickupListenEvent> = speechRecognitionRepository.listen().map { event ->
        when (event) {
            is SpeechRecognitionEvent.Understood -> checkContact(event.request)
            // ...
        }
    }
}
```

위치는 `domain/pickup/usecase/`이고 반환 타입 `PickupListenEvent`는 `domain/pickup/model/`에 둔다.

계층 구조와 의존성 방향: [data-layer](data-layer.md)
