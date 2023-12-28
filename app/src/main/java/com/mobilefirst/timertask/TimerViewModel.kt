package com.mobilefirst.timertask

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import org.koin.core.component.KoinComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.inject

class TimerViewModel: ViewModel(), KoinComponent, LifecycleObserver {

    private var isAppInForeground = false

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        isAppInForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        isAppInForeground = false
    }


    private val notificationHelper: NotificationHelper by inject()

    private var job: Job? = null

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Initial)
    val timerState: StateFlow<TimerState> = _timerState
    var currentTime : Long? = null

    init {
        _timerState.value = TimerState.Initial
    }

    fun startPauseTimer() {
        job?.let {
            if (it.isActive) {
                job?.cancel()
                _timerState.value = TimerState.Paused
            }else if (_timerState.value == TimerState.Paused){
                startTimerAgain()
            } else {
                startTimer()
            }
        } ?: startTimer()
    }

    private fun startTimer() {
        job = viewModelScope.launch {
            _timerState.value = TimerState.Running
            for (i in 60 downTo 0) {
                delay(1000)
                _timerState.value = TimerState.RunningCountdown(i)
                currentTime = TimerState.RunningCountdown(i).remainingSeconds.toLong()
            }
            _timerState.value = TimerState.Completed
            if (!isAppInForeground) {
                notificationHelper.showNotification("Timer Completed")
            }
        }
    }

    private fun startTimerAgain() {
        job = viewModelScope.launch {
            _timerState.value = TimerState.Running
            for (i in currentTime!!.toInt() downTo 0) {
                delay(1000)
                _timerState.value = TimerState.RunningCountdown(i)
                currentTime = TimerState.RunningCountdown(i).remainingSeconds.toLong()
            }
            _timerState.value = TimerState.Completed
            if (!isAppInForeground) {
                notificationHelper.showNotification("Timer Completed")
            }
        }
    }

    fun stopTimer() {
        job?.cancel()
        _timerState.value = TimerState.Initial
    }

}