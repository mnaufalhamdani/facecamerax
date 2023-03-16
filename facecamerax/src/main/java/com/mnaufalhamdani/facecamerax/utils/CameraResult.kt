package com.mnaufalhamdani.facecamerax.utils

import java.io.File

interface CameraResult {
    fun onImageResult(data: File)
    fun onResultCancel()
    fun onResulError(msg: String)
}