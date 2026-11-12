package com.offcourse.picko.data.speech.repository

interface SpeechSynthesisRepository {
    /**
     * 문장을 소리로 읽고 읽기가 끝나면 돌아온다. 읽는 도중 코루틴을 취소하면 읽기를 멈춘다.
     * 소리를 내지 못했으면 예외를 던지지 않고 false 를 돌려준다.
     * 문장은 화면에 보이는 텍스트와 같은 `strings.xml` 문구를 넘긴다.
     */
    suspend fun speak(text: String): Boolean
}
