package com.offcourse.picko.data.speech.datasource

import com.offcourse.picko.data.gleo.model.PickupRequestModel
import javax.inject.Inject

// 픽업을 뜻하는 말의 어간. 이 말이 없으면 픽업 요청으로 보지 않는다.
private val PickupVerb = Regex("픽업|태우|태워|태울|데리러|데려|모시러|마중")

// 문장 앞에 붙는 호출어("Gleo야,"). 앱이 직접 듣는 경로에서도 호출어를 붙여 말하는 사람이 있다.
private val WakeWord = Regex("^(글레오|gleo)\\s*야?\\s*[,，]?\\s*", RegexOption.IGNORE_CASE)
private val LeadingFiller = Regex("^(지금|저기|음|어|그|좀)\\s+")
private val PassengerParticle = Regex("(을|를|와|과|한테|에게|께)$")
private val PlaceSuffix = Regex("\\s*(근처|부근|주변)$")
private const val PLACE_MARKER = "에서"

/**
 * 말한 문장에서 탑승자와 장소를 읽는다. 정해진 말투만 읽는 규칙 기반이다. 문장 구조 파서용
 *
 * | 문장 | 탑승자 | 장소 |
 * | --- | --- | --- |
 * | 엄마 강남역에서 픽업해줘 | 엄마 | 강남역 |
 * | 대치동 학원가에서 현대를 태울 거야 | 현대 | 대치동 학원가 |
 * | 아빠 태우러 가줘 | 아빠 | (없음) |
 * | 현대와 만나기로 했는데 픽업 장소 정해줘 | 현대 | (없음) |
 */
class SpokenPickupParser @Inject constructor() {
    /** 픽업을 뜻하는 말이 없거나 탑승자를 찾지 못하면 null */
    fun parse(text: String): PickupRequestModel? {
        val sentence = text.trim().trimEnd('.', '?', '!', ' ')
        val verb = PickupVerb.find(sentence) ?: return null
        val head = sentence.substring(0, verb.range.first).trim().replace(WakeWord, "").replace(LeadingFiller, "")

        val markerIndex = head.indexOf(PLACE_MARKER)
        val (passenger, place) = if (markerIndex < 0) {
            pickPassenger(head.words()) to null
        } else {
            splitAroundPlaceMarker(
                before = head.substring(0, markerIndex).trim(),
                after = head.substring(markerIndex + PLACE_MARKER.length).trim(),
            )
        }

        if (passenger.isNullOrEmpty()) return null

        val cleanPlace = place?.replace(PlaceSuffix, "")?.trim().takeUnless { it.isNullOrEmpty() }

        return PickupRequestModel(passenger = passenger, place = cleanPlace)
    }

    /**
     * "에서" 뒤에 말이 있으면 장소가 먼저다("대치동 학원가에서 현대를"). 없으면 첫 단어가 탑승자다("엄마 강남역에서").
     */
    private fun splitAroundPlaceMarker(before: String, after: String): Pair<String?, String?> {
        if (after.isNotEmpty()) return pickPassenger(after.words()) to before

        val words = before.words()
        if (words.size < 2) return null to null

        return pickPassenger(listOf(words.first())) to words.drop(1).joinToString(" ")
    }

    /**
     * 탑승자 조사(를, 와 등)가 붙은 단어를 이름으로 본다. 조사가 붙은 단어가 없으면 단어가 하나일 때만 이름으로 본다.
     * `~게`로 끝나는 부사("안전하게")는 이름이 아니다.
     */
    private fun pickPassenger(words: List<String>): String? {
        val candidate = words.firstOrNull { PassengerParticle.containsMatchIn(it) } ?: words.singleOrNull()
        val name = candidate?.replace(PassengerParticle, "")?.trim()

        return name?.takeUnless { it.isEmpty() || it.endsWith("게") }
    }

    private fun String.words(): List<String> = split(Regex("\\s+")).filter { it.isNotEmpty() }
}
