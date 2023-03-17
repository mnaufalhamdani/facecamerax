@file:Suppress("DEPRECATION")

package com.example.customcamerax

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.example.customcamerax.databinding.ActivitySampleBinding
import com.google.android.material.button.MaterialButton
import com.mnaufalhamdani.facecamerax.FaceCameraX
import com.mnaufalhamdani.facecamerax.databinding.ActivityFaceCameraBinding

class SampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCapture.setOnClickListener {
            FaceCameraX.with(this)
                .compress(80)//Default compress is 80
                .coordinat(-7.2891684, 112.6756733)//Default coordinat is 0.0
                .defaultCamera(FaceCameraX.LensCamera.LENS_BACK_CAMERA)//Default camera is Front Camera
                .isFaceDetection(true)//Default is true
                .isWaterMark(true)//Default is true
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
        val path = uri.toFile().absolutePath
        binding.tvPath.text = path
    }

}