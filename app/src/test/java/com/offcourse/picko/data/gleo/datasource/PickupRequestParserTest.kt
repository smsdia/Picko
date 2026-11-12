package com.offcourse.picko.data.gleo.datasource

import com.offcourse.picko.data.gleo.model.PickupRequestModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PickupRequestParserTest {
    private val parser = PickupRequestParser()

    @Test
    fun 탑승자와_장소를_읽는다() {
        val result = parser.parse("""{"passenger":"엄마","place":"강남역"}""")

        assertEquals(PickupRequestModel(passenger = "엄마", place = "강남역"), result)
    }

    @Test
    fun 장소가_없으면_null로_둔다() {
        val result = parser.parse("""{"passenger":"엄마"}""")

        assertEquals(PickupRequestModel(passenger = "엄마", place = null), result)
    }

    @Test
    fun 빈_장소는_null로_바꾸고_공백은_다듬는다() {
        val result = parser.parse("""{"passenger":" 엄마 ","place":"  "}""")

        assertEquals(PickupRequestModel(passenger = "엄마", place = null), result)
    }

    @Test
    fun 모르는_키는_무시한다() {
        val result = parser.parse("""{"passenger":"엄마","extra":1}""")

        assertEquals("엄마", result?.passenger)
    }

    @Test
    fun 읽을_수_없으면_null이다() {
        assertNull(parser.parse(null))
        assertNull(parser.parse(""))
        assertNull(parser.parse("not json"))
        assertNull(parser.parse("""{"place":"강남역"}"""))
        assertNull(parser.parse("""{"passenger":"  "}"""))
    }
}
