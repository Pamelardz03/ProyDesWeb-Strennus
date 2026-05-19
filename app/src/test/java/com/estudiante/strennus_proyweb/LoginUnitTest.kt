package com.estudiante.strennus_proyweb

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginUnitTest {
    // Login
    @Test
    fun emptyUsername() {
        val result = SessionUtil.validateLoginInput(
            username = "",
            password = "1234"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun emptyPassword() {
        val result = SessionUtil.validateLoginInput(
            username = "tester",
            password = ""
        )
        assertThat(result).isFalse()
    }

    @Test
    fun allFieldsEmpty() {
        val result = SessionUtil.validateLoginInput(
            username = "",
            password = ""
        )
        assertThat(result).isFalse()
    }

    @Test
    fun bothFieldsComplete() {
        val result = SessionUtil.validateLoginInput(
            username = "tester",
            password = "1234"
        )
        assertThat(result).isTrue()
    }

    // Register
    @Test
    fun passwordsAreDifferent() {
        val result = SessionUtil.validateRegisterInput(
            name = "Leonardo",
            username = "leo",
            mail = "leo@test.com",
            password = "1234",
            confirmPassword = "5678"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun mailIncomplete() {
        val result = SessionUtil.validateRegisterInput(
            name = "Leonardo",
            username = "leo",
            mail = "leotest.com",
            password = "1234",
            confirmPassword = "1234"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun allFieldsComplete() {
        val result = SessionUtil.validateRegisterInput(
            name = "Leonardo",
            username = "leo",
            mail = "leo@test.com",
            password = "1234",
            confirmPassword = "1234"
        )
        assertThat(result).isTrue()
    }

    // Session
    @Test
    fun durationCorrectlyCalculated() {
        val result = SessionUtil.calculateDuration(
            startTime = 0L,
            endTime = 60000L
        )
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun endTimeLessThanStartTime() {
        val result = SessionUtil.calculateDuration(
            startTime = 60000L,
            endTime = 0L
        )
        assertThat(result).isEqualTo(0)
    }

    // Exercises
    @Test
    fun seriesIsCero() {
        val result = SessionUtil.validateExercise(
            name = "Press banca",
            series = 0,
            reps = 10
        )
        assertThat(result).isFalse()
    }
}