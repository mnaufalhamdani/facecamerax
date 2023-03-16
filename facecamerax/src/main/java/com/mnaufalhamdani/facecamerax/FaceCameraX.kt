@file:Suppress("DEPRECATION")

package com.mnaufalhamdani.facecamerax

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.mnaufalhamdani.facecamerax.utils.singleClick

open class FaceCameraX {
    companion object {
        // Default Request Code to Pick Image
        const val RESULT_ERROR = 64

        internal const val EXTRA_FILE_PATH = "extra.file_path"
        internal const val EXTRA_ERROR = "extra.error"

        internal const val EXTRA_IMAGE_MAX_SIZE = "EXTRA_IMAGE_MAX_SIZE"
        internal const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        internal const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
        internal const val EXTRA_IS_FACE_DETECTION = "EXTRA_IS_FACE_DETECTION"
        internal const val EXTRA_IS_WATERMARK = "EXTRA_IS_WATERMARK"
        internal const val EXTRA_LENS_CAMERA = "EXTRA_LENS_CAMERA"

        /**
         * Use this to use CaptureCameraX in Activity Class
         *
         * @param activity Activity Instance
         */
        @JvmStatic
        fun with(activity: Activity): Builder {
            return Builder(activity)
        }

        /**
         * Use this to use CaptureCameraX in Fragment Class
         *
         * @param fragment Fragment Instance
         */
        @JvmStatic
        fun with(fragment: Fragment): Builder {
            return Builder(fragment)
        }
    }

    enum class LensCamera(val value: Int) {
        LENS_BACK_CAMERA(0), LENS_FRONT_CAMERA(1)
    }

    class Builder(private val activity: Activity) {

        private var fragment: Fragment? = null
        private var maxSize: Int = 80
        private var latitude: Double = 0.0
        private var longitude: Double = 0.0
        private var lensFacing: Int = 1//0 = BACK CAMERA, 1 = FRONT CAMERA
        private var isFaceDetection: Boolean = true
        private var isWaterMark: Boolean = true

        /**
         * Call this while picking image for fragment.
         */
        constructor(fragment: Fragment) : this(fragment.requireActivity()) {
            this.fragment = fragment
        }

        fun compress(maxSize: Int): Builder {
            this.maxSize = maxSize
            return this
        }

        fun coordinat(latitude: Double, longitude: Double): Builder {
            this.latitude = latitude
            this.longitude = longitude
            return this
        }

        fun defaultCamera(lensCamera: LensCamera): Builder {
            this.lensFacing = lensCamera.value
            return this
        }

        fun isFaceDetection(isFaceDetection: Boolean): Builder {
            this.isFaceDetection = isFaceDetection
            return this
        }

        fun isWaterMark(isWaterMark: Boolean): Builder {
            this.isWaterMark = isWaterMark
            return this
        }

        fun start(reqCode: Int) {
            if (!singleClick()) return
            startActivity(reqCode)
        }

        private fun getBundle(): Bundle {
            return Bundle().apply {
                putBoolean(EXTRA_IS_FACE_DETECTION, isFaceDetection)
                putBoolean(EXTRA_IS_WATERMARK, isWaterMark)

                putInt(EXTRA_LENS_CAMERA, lensFacing)
                putInt(EXTRA_IMAGE_MAX_SIZE, maxSize)

                putDouble(EXTRA_LATITUDE, latitude)
                putDouble(EXTRA_LONGITUDE, longitude)
            }
        }

        /**
         * Start CaptureCameraX with given Argument
         */
        private fun startActivity(reqCode: Int) {
            val intent = Intent(activity, FaceCameraActivity::class.java)
            intent.putExtras(getBundle())
            if (fragment != null) {
                fragment?.startActivityForResult(intent, reqCode)
            } else {
                activity.startActivityForResult(intent, reqCode)
            }
        }
    }
}