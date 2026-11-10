# 리소스

## 문자열

```xml
<!-- 정차 구역 추천 -->
<string name="pickup_spot_empty_title">추천할 정차 구역이 없어요.</string>
<string name="pickup_spot_close_a11y">닫기</string>
<plurals name="pickup_spot_count">
    <item quantity="other">정차 구역 %1$d곳</item>
</plurals>
```

| 항목 | 규칙 |
| --- | --- |
| 이름 | `<screen>_<area>_<what>`, snake_case |
| 접근성 | `contentDescription`용 문자열은 `_a11y`로 끝낸다 |
| 위치 | `app/src/main/res/values/strings.xml`. 화면별로 `<!-- 화면 이름 -->` 주석으로 묶는다 |
| 숫자 | `%1$d`. 개수에 따라 문장이 달라지면 `plurals` |
| 재사용·정리 | 같은 의미는 재사용하고 쓰지 않는 문자열은 지운다 |
| 범위 | 사용자에게 보이는 텍스트만. 로그, 키, 테스트 데이터, Preview 샘플은 제외 |
| 음성 안내 | 음성 안내 문구도 `strings.xml`에 둔다 |
| ViewModel | 화면에 보이는 텍스트를 만들지 않는다. State에 값이나 에러 타입을 두고 UI에서 변환한다 |

| Don't | Do |
| --- | --- |
| `Text("추천할 정차 구역이 없어요.")` | `Text(stringResource(R.string.pickup_spot_empty_title))` |
| `"정차 구역 " + count + "곳"` | `pluralStringResource(R.plurals.pickup_spot_count, count, count)` |
| ViewModel에서 `"정차 구역 ${count}곳"`을 State에 넣음 | State에 `count`를 두고 UI에서 `pluralStringResource` |

## contentDescription

| 대상 | 규칙 |
| --- | --- |
| 클릭 가능하거나 의미를 전달하는 아이콘·이미지 | `stringResource(R.string.<..>_a11y)` |
| 장식용 이미지 | `null` |
| 텍스트와 함께 있는 아이콘 | `null` (중복 낭독 방지) |

| Don't | Do |
| --- | --- |
| `contentDescription = "닫기"` | `contentDescription = stringResource(R.string.pickup_spot_close_a11y)` |
| 텍스트 옆 아이콘에 같은 문구를 `contentDescription`으로 중복 | `contentDescription = null` |
