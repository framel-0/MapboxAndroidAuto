package com.tinydavid.android_auto.surface_layer

import com.tinydavid.android_auto.surface_layer.GLUtils.BYTES_PER_FLOAT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

internal object GLUtils {
    const val BYTES_PER_FLOAT: Int = 4
    const val MATRIX_SIZE: Int = 16
}

internal fun FloatArray.toFloatBuffer(): FloatBuffer =
    ByteBuffer.allocateDirect(size * BYTES_PER_FLOAT).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(this@toFloatBuffer)
            rewind()
        }
    }
