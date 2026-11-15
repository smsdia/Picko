package com.offcourse.picko.core.network.quota

private const val DAILY_LIMIT_PERCENT = 80

/**
 * 호출 수를 세는 카카오 API. [dailyFreeQuota]는 하루 무료 쿼터이고, 가드는 그 80%를 하루 한도로 둔다.
 * 쿼터 값의 출처와 바뀔 때 고칠 곳은 `docs/references/kakao-api-quota.md`.
 */
enum class KakaoApi(val dailyFreeQuota: Int) {
    /** 카카오맵 REST 키워드로 장소 검색 */
    LocalKeywordSearch(dailyFreeQuota = 100_000),

    /** 카카오내비 REST 자동차 길찾기 */
    CarDirections(dailyFreeQuota = 10_000),
    ;

    val dailyLimit: Int get() = dailyFreeQuota / 100 * DAILY_LIMIT_PERCENT
}
