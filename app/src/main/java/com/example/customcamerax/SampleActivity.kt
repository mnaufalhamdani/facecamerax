@file:Suppress("DEPRECATION")

package com.example.customcamerax

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.example.customcamerax.databinding.ActivitySampleBinding
import com.mnaufalhamdani.facecamerax.FaceCameraX

class SampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCapture.setOnClickListener {
            FaceCameraX.with(this)
                .customPath(Environment.getExternalStorageDirectory().toString() + "/TestCam/")
                .compress(50)//Default compress is 80
                .coordinat(-7.2891684, 112.6756733)//Default coordinat is 0.0
                .defaultCamera(FaceCameraX.LensCamera.LENS_BACK_CAMERA)//Default camera is Front Camera
                .isFaceDetection(false)//Default is true
                .isWaterMark(true)//Default is true
                .additionalWaterMark("Anda berada pada ketinggian 200m")
                .start(0)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    it.data?.let { uri ->
                        processImage(uri)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processImage(uri: Uri) {
        Log.d("processImage2:", "${uri.toFile().absolutePath}")
        val path = uri.toFile().absolutePath
        binding.tvPath.text = path
    }

}