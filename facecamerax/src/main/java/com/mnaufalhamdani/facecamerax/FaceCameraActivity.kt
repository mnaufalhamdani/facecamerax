@file:Suppress("DEPRECATION")

package com.mnaufalhamdani.facecamerax

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.mnaufalhamdani.facecamerax.databinding.ActivityFaceCameraBinding
import com.mnaufalhamdani.facecamerax.utils.CameraResult
import java.io.File

class FaceCameraActivity : AppCompatActivity(), CameraResult {
    private lateinit var binding: ActivityFaceCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val host = supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
        val navController = host.navController
        navController.setGraph(R.navigation.nav_graph, intent.extras)
    }

    override fun onImageResult(data: File) {
        val intent = Intent()
        intent.data = Uri.fromFile(data)
        intent.putExtra(FaceCameraX.EXTRA_FILE_PATH, data.absolutePath)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun getCancelledIntent(): Intent {
        val intent = Intent()
        val message = getString(R.string.message_error)
        intent.putExtra(FaceCameraX.EXTRA_ERROR, message)
        return intent
    }

    override fun onResultCancel() {
        setResult(Activity.RESULT_CANCELED, getCancelledIntent())
        finish()
    }

    override fun onResulError(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        val intent = Intent()
        intent.putExtra(FaceCameraX.EXTRA_ERROR, msg)
        setResult(FaceCameraX.RESULT_ERROR, intent)
        finish()
    }
}