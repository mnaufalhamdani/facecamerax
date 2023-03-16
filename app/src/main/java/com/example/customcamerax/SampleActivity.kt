@file:Suppress("DEPRECATION")

package com.example.customcamerax

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.google.android.material.button.MaterialButton
import com.mnaufalhamdani.facecamerax.FaceCameraX

class SampleActivity : AppCompatActivity() {
    private lateinit var btn: MaterialButton
    private lateinit var tvPath: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        btn = findViewById(R.id.btn_capture)
        tvPath = findViewById(R.id.tv_path)

        btn.setOnClickListener {
            FaceCameraX.with(this)
                .coordinat(-7.2891684, 112.6756733)
                .defaultCamera(FaceCameraX.LensCamera.LENS_BACK_CAMERA)
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
        tvPath.text = path
    }

}