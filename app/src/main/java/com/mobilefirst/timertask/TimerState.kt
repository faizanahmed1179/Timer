package com.mobilefirst.timertask

sealed class TimerState {
    object Initial : TimerState()
    object Running : TimerState()
    object Paused : TimerState()
    object Completed : TimerState()
    data class RunningCountdown(val remainingSeconds: Int) : TimerState()
}
