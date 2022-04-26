package com.tinydavid.car.feedback.core

import com.tinydavid.car.feedback.ui.CarFeedbackItem

interface CarFeedbackItemProvider {
    fun feedbackItems(): List<CarFeedbackItem>
}
