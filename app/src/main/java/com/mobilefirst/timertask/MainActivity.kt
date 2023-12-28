package com.mobilefirst.timertask

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobilefirst.timertask.ui.theme.TimerTaskTheme
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 1)
                }
            }
            TimerTaskTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimerApp(timerViewModel: TimerViewModel = viewModel()) {
    val timerState by timerViewModel.timerState.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimerView(modifier = Modifier
            .weight(1f)
            .padding(16.dp),
            timerState)
        Spacer(modifier = Modifier.height(16.dp))
        TimerButtons(timerViewModel, keyboardController,timerState)
    }
}

@Composable
fun TimerView(modifier: Modifier = Modifier,
    timerState: TimerState) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val timerViewModel: TimerViewModel = viewModel()
//        Text(
//            text = when (timerState) {
//                is TimerState.Initial -> "Timer Not Started"
//                is TimerState.RunningCountdown -> formatTime(timerState.remainingSeconds)
//                is TimerState.Running -> "Timer Running..."
//                TimerState.Paused -> "Timer Paused"
//                TimerState.Completed -> "Timer Completed!"
//            },
//            style = MaterialTheme.typography.bodyLarge
//        )
        //Spacer(modifier = Modifier.height(16.dp))

        val countdownText = when (timerState) {
            is TimerState.Initial -> "00:00"
            is TimerState.RunningCountdown -> formatTime(timerState.remainingSeconds.toLong())
            is TimerState.Paused -> formatTime(timerViewModel.currentTime!!)
            else -> {
                "00:00"
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = when (timerState) {
                    is TimerState.RunningCountdown -> {
                        1 - timerState.remainingSeconds / 60f
                    }
                    is TimerState.Paused -> {
                        1 - timerViewModel.currentTime!!.toInt() / 60f
                    }
//                    is TimerState.Running -> {
//                        1 - timerViewModel.currentTime!!.toInt() / 60f
//                    }
                    else -> 1f
                },
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.background)
            )
            Text(
                text = countdownText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black,
                textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimerButtons(
    timerViewModel: TimerViewModel,
    keyboardController: SoftwareKeyboardController?,
    timerState: TimerState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val density = LocalDensity.current.density
        val buttonSize = (48 * density).dp


        IconButton(
            onClick = { timerViewModel.startPauseTimer() },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                imageVector = when (timerState) {
                    is TimerState.RunningCountdown -> {
                        Icons.Default.Pause
                    }
                    else -> Icons.Default.PlayArrow
                } ,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = { timerViewModel.stopTimer() },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun formatTime(remainingMillis: Long): String {
    val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis)
    val remainingMillisInSecond = remainingMillis % 1000
    return String.format("%02d:%02d", remainingSeconds % 60, remainingMillisInSecond)
}

@Preview(showBackground = true)
@Composable
fun TimerAppPreview() {
    TimerTaskTheme {
        TimerApp()
    }
}