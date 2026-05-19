package com.estudiante.strennus_proyweb

object SessionUtil {
    fun validateLoginInput(
        username: String,
        password: String
    ): Boolean {
        return !(username.isEmpty() || password.isEmpty())
    }

    fun validateRegisterInput(
        name: String,
        username: String,
        mail: String,
        password: String,
        confirmPassword: String
    ): Boolean{
        if(name.isEmpty() || username.isEmpty() || mail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) return false
        if(!mail.contains("@")) return false
        if(password != confirmPassword) return false
        return true
    }

    fun calculateDuration(
        startTime: Long,
        endTime: Long
    ): Int{
        if(endTime <= startTime) return 0
        return ((endTime-startTime) / 60000).toInt()
    }

    fun validateExercise(
        name: String,
        series: Int,
        reps: Int
    ): Boolean {
        if (name.isEmpty()) return false
        if (series <= 0 || reps <= 0) return false
        return true
    }
}