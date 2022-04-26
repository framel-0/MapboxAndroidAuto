package com.tinydavid.android_auto.surface_layer.textview

import android.graphics.Rect
import android.opengl.Matrix
import com.mapbox.maps.EdgeInsets
import com.tinydavid.android_auto.AndroidAutoLog.logAndroidAuto
import com.tinydavid.android_auto.surface_layer.CarSurfaceLayer
import com.tinydavid.android_auto.surface_layer.GLUtils

class CarScene2d : CarSurfaceLayer() {

    val mvpMatrix = FloatArray(GLUtils.MATRIX_SIZE)
    val camera = CarCamera2d()
    val model = CarTextModel2d()

    override fun children() = listOf(camera, model)

    override fun visibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        super.visibleAreaChanged(visibleArea, edgeInsets)

        Matrix.multiplyMM(
            mvpMatrix, 0,
            camera.projM, 0,
            camera.viewM, 0
        )

        logAndroidAuto(
            "CarScene2d visibleAreaChanged visibleArea:$visibleArea: edgeInsets:$edgeInsets",
        )
    }
}
