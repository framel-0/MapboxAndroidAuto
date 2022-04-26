package com.tinydavid.car

import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.tinydavid.android_auto.*
import com.tinydavid.android_auto.navigation.audio_guidance.CarAudioGuidanceUi
import com.tinydavid.car.feedback.core.CarFeedbackSender
import com.tinydavid.car.feedback.ui.CarFeedbackAction
import com.tinydavid.car.feedback.ui.activeGuidanceCarFeedbackProvider
import com.tinydavid.car.navigation.ActiveGuidanceScreen
import com.tinydavid.car.navigation.CarActiveGuidanceCarContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MainScreenManager(
    val mainCarContext: MainCarContext
) : DefaultLifecycleObserver {
    private val parentJob = SupervisorJob()
    private val parentScope = CoroutineScope(parentJob + Dispatchers.Main)

    private val carAppStateObserver = Observer<CarAppState> { carAppState ->
        val currentScreen = currentScreen(carAppState)
        val screenManager = mainCarContext.carContext.getCarService(ScreenManager::class.java)
        logAndroidAuto("MainScreenManager screen change ${currentScreen.javaClass.simpleName}")
        if (screenManager.top.javaClass != currentScreen.javaClass) {
            screenManager.push(currentScreen)
        }
    }

    fun currentScreen(): Screen = currentScreen(MapboxCarApp.carAppState.value!!)

    private fun currentScreen(carAppState: CarAppState): Screen {
        return when (carAppState) {
            FreeDriveState, RoutePreviewState -> MainCarScreen(mainCarContext)
            ActiveGuidanceState, ArrivalState -> {
                ActiveGuidanceScreen(
                    CarActiveGuidanceCarContext(mainCarContext),
                    listOf(
                        CarFeedbackAction(
                            mainCarContext.mapboxCarMap,
                            CarFeedbackSender(),
                            activeGuidanceCarFeedbackProvider(mainCarContext.carContext)
                        ),
                        CarAudioGuidanceUi()
                    )
                )
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        logAndroidAuto("MainScreenManager onCreate")
        parentScope.launch {
            MapboxCarApp.carAppState.collect { carAppState ->
                carAppStateObserver.onChanged(carAppState)
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        logAndroidAuto("MainScreenManager onDestroy")
        parentJob.cancelChildren()
    }
}
