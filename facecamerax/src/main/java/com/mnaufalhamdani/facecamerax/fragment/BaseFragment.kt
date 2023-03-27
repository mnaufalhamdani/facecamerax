@file:Suppress("DEPRECATION")

package com.mnaufalhamdani.facecamerax.fragment

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import com.mnaufalhamdani.facecamerax.R
import java.io.File

abstract class BaseFragment<B : ViewBinding>(private val fragmentLayout: Int) : Fragment() {
    abstract val binding: B

    // The Folder location where all the files will be stored
    protected val outputDirectory: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${Environment.DIRECTORY_DCIM}/${getString(R.string.app_name)}/"
        } else {
            "${requireContext().getExternalFilesDir(Environment.DIRECTORY_DCIM)}/${getString(R.string.app_name)}/"
        }
    }

    protected fun setFolderDatabase(path: String): Boolean {
        val folder = File(path)
        var success = true
        if (!folder.exists()) {
            success = folder.mkdirs()
        }
        if (success) {
            println("Success Create Folder!")
        } else {
            println("Failed Create Folder!")
        }
        return success
    }

    // The permissions we need for the app to work properly
    private val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Adding an option to handle the back press in fragment
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })

        return binding.root
    }

    abstract fun onBackPressed()

    protected fun onRunPermission(listenerGranted: (() -> Unit)?=null,
                                  listenerDeny: ((String) -> Unit)?=null) {
        activity?.let {
            val view = it.findViewById<View>(android.R.id.content)
            Dexter.withActivity(it)
                .withPermissions(permissions)
                .withListener(
                    CompositeMultiplePermissionsListener(
                        SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
                            .with(view, "The application needs this permission")
                            .withOpenSettingsButton("Setting")
                            .withDuration(Snackbar.LENGTH_INDEFINITE)
                            .build(),
                        DialogOnAnyDeniedMultiplePermissionsListener.Builder
                            .withContext(binding.root.context)
                            .withTitle("The application needs this permission")
                            .withMessage("Please allow all permissions")
                            .withButtonText(android.R.string.ok)
                            .build(),
                        object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                report?.let { multiplePermissionReport ->
                                    if (multiplePermissionReport.areAllPermissionsGranted()) {
                                        listenerGranted?.invoke()
                                    } else {
                                        var text = "Please Allowed: "
                                        multiplePermissionReport.deniedPermissionResponses.map {
                                            text += it.permissionName + ", "
                                        }
                                        listenerDeny?.invoke(text.substring(0, text.length - 2))
                                    }
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                token?.continuePermissionRequest()
                            }
                        }
                    )
                ).onSameThread().check()
        }
    }
}
