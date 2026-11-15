package com.offcourse.picko.data.vehicle.model

/**
 * 차량 위치를 받을 수 없어 `VehicleRepository.observeLocation()`이 이 예외로 끝났다.
 * FusedLocation SDK 는 차량의 개인정보 보호 설정으로 막힌 경우를 따로 알려 주지 않는다. 그래서 그런 경우도 [Reason.Unavailable]에 들어간다.
 */
class VehicleLocationException(
    val reason: Reason,
    cause: Throwable? = null,
) : Exception(reason.name, cause) {
    enum class Reason {
        /** 이 환경에 FusedLocation 서비스가 없다. */
        Unsupported,

        /** 앱에 위치 권한이 허용되지 않았다. */
        PermissionDenied,

        /** 위치를 받지 못했다(SDK 오류, 개인정보 보호 설정으로 막힘 등). */
        Unavailable,
    }
}
