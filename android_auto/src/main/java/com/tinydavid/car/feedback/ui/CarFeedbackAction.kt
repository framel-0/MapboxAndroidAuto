package com.tinydavid.car.feedback.ui

import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.mapbox.navigation.core.telemetry.events.BitmapEncodeOptions
import com.mapbox.navigation.core.telemetry.events.FeedbackHelper
import com.tinydavid.android_auto.R
import com.tinydavid.android_auto.car.map.MapboxCarMap
import com.tinydavid.car.action.MapboxActionProvider
import com.tinydavid.car.feedback.core.CarFeedbackItemProvider
import com.tinydavid.car.feedback.core.CarFeedbackSender
import kotlinx.coroutines.launch

class CarFeedbackAction(
    private val mapboxCarMap: MapboxCarMap,
    private val carFeedBackSender: CarFeedbackSender,
    private val carFeedbackItemProvider: CarFeedbackItemProvider
) : MapboxActionProvider.ScreenActionProvider {

    override fun getAction(screen: Screen): Action {
        return buildSnapshotAction(
            screen,
            carFeedBackSender,
            carFeedbackItemProvider.feedbackItems()
        )
    }

    private fun buildSnapshotAction(
        screen: Screen,
        feedbackSender: CarFeedbackSender,
        feedbackItems: List<CarFeedbackItem>
    ) = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(screen.carContext, R.drawable.mapbox_car_ic_feedback)
            ).build()
        )
        .setOnClickListener {
            screen.lifecycleScope.launch {
                val mapSurface = mapboxCarMap.mapboxCarMapSurface?.mapSurface
                val encodedSnapshot = mapSurface?.snapshot()?.let { bitmap ->
                    FeedbackHelper.encodeScreenshot(bitmap, bitmapEncodeOptions)
                }
                screen.screenManager.push(
                    CarGridFeedbackScreen(
                        screen.carContext,
                        screen.javaClass.simpleName,
                        feedbackSender,
                        feedbackItems,
                        encodedSnapshot
                    ) { screen.screenManager.pop() }
                )
            }
        }
        .build()

    private companion object {
        private const val BITMAP_COMPRESS_QUALITY = 50
        private const val BITMAP_WIDTH = 800
        private val bitmapEncodeOptions = BitmapEncodeOptions.Builder()
            .compressQuality(BITMAP_COMPRESS_QUALITY)
            .width(BITMAP_WIDTH)
            .build()
    }
}
