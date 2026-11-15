package com.offcourse.picko.data.speech.datasource

import com.offcourse.picko.data.gleo.model.PickupRequestModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpokenPickupParserTest {
    private val parser = SpokenPickupParser()

    @Test
    fun 탑승자가_먼저_오면_첫_단어가_탑승자이고_나머지가_장소다() {
        val result = parser.parse("엄마 강남역에서 픽업해줘")

        assertEquals(PickupRequestModel(passenger = "엄마", place = "강남역"), result)
    }

    @Test
    fun 장소가_먼저_오면_에서_뒤가_탑승자다() {
        val result = parser.parse("대치동 학원가에서 현대를 태울 거야")

        assertEquals(PickupRequestModel(passenger = "현대", place = "대치동 학원가"), result)
    }

    @Test
    fun 장소가_여러_단어여도_탑승자는_첫_단어다() {
        val result = parser.parse("민수 대치동 학원가에서 태워줘")

        assertEquals(PickupRequestModel(passenger = "민수", place = "대치동 학원가"), result)
    }

    @Test
    fun 장소가_없으면_null로_둔다() {
        val result = parser.parse("아빠 태우러 가줘")

        assertEquals(PickupRequestModel(passenger = "아빠", place = null), result)
    }

    @Test
    fun 탑승자_뒤의_조사를_뗀다() {
        val result = parser.parse("엄마를 픽업해줘")

        assertEquals(PickupRequestModel(passenger = "엄마", place = null), result)
    }

    @Test
    fun 장소_뒤의_앞은_뗀다() {
        val result = parser.parse("엄마 성신여대입구역 앞에서 픽업해 줘")

        assertEquals(PickupRequestModel(passenger = "엄마", place = "성신여대입구역"), result)
    }

    @Test
    fun 장소_뒤의_근처는_뗀다() {
        val result = parser.parse("엄마 강남역 근처에서 픽업해줘")

        assertEquals(PickupRequestModel(passenger = "엄마", place = "강남역"), result)
    }

    @Test
    fun 문장_앞의_군더더기와_끝의_문장부호는_무시한다() {
        val result = parser.parse("지금 엄마 강남역에서 픽업해줘.")

        assertEquals(PickupRequestModel(passenger = "엄마", place = "강남역"), result)
    }

    @Test
    fun 문장_앞의_호출어는_무시한다() {
        val result = parser.parse("Gleo야, 대치동 학원가에서 현대 태울 거야.")

        assertEquals(PickupRequestModel(passenger = "현대", place = "대치동 학원가"), result)
    }

    @Test
    fun 조사가_붙은_단어를_탑승자로_본다() {
        val result = parser.parse("현대와 만나기로 했는데 픽업 장소 정하는 것 좀 도와줘")

        assertEquals(PickupRequestModel(passenger = "현대", place = null), result)
    }

    @Test
    fun 부사는_탑승자가_아니다() {
        assertNull(parser.parse("대치동 학원가에서 안전하게 픽업할 장소를 찾아주세요"))
    }

    @Test
    fun 조사가_없는_단어가_여럿이면_탑승자를_정하지_않는다() {
        assertNull(parser.parse("오늘 저녁에 누구 좀 픽업해줘"))
    }

    @Test
    fun 픽업을_뜻하는_말이_없으면_null이다() {
        assertNull(parser.parse("오늘 날씨 어때"))
    }

    @Test
    fun 탑승자가_없으면_null이다() {
        assertNull(parser.parse("강남역에서 픽업해줘"))
        assertNull(parser.parse("픽업해줘"))
    }

    @Test
    fun 빈_문장은_null이다() {
        assertNull(parser.parse("  "))
    }
}
