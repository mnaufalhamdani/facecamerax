@file:Suppress("DEPRECATION")

package com.mnaufalhamdani.facecamerax.fragment

import android.annotation.SuppressLint
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationRequest
import com.mnaufalhamdani.facecamerax.FaceCameraX.Companion.EXTRA_IMAGE_MAX_SIZE
import com.mnaufalhamdani.facecamerax.FaceCameraX.Companion.EXTRA_IS_FACE_DETECTION
import com.mnaufalhamdani.facecamerax.FaceCameraX.Companion.EXTRA_IS_WATERMARK
import com.mnaufalhamdani.facecamerax.FaceCameraX.Companion.EXTRA_LATITUDE
import com.mnaufalhamdani.facecamerax.FaceCameraX.Companion.EXTRA_LENS_CAMERA
import com.mnaufalhamdani.facecamerax.FaceCameraX.Companion.EXTRA_LONGITUDE
import com.mnaufalhamdani.facecamerax.R
import com.mnaufalhamdani.facecamerax.utils.CameraResult
import com.mnaufalhamdani.facecamerax.core.FaceContourDetectionProcessor
import com.mnaufalhamdani.facecamerax.core.LocationLiveData
import com.mnaufalhamdani.facecamerax.databinding.FragmentCameraBinding
import com.mnaufalhamdani.facecamerax.utils.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates

class CameraXFragment : BaseFragment<FragmentCameraBinding>(R.layout.fragment_camera) {
    companion object {
        private const val TAG = "CameraXFragment"
        private const val FILENAME_FORMAT = "yyyyMMddHHmmss"
    }

    private lateinit var locationGPS: LocationLiveData

    private val maxSize by lazy { arguments?.getInt(EXTRA_IMAGE_MAX_SIZE) ?: 80 }
    private val latitude by lazy { arguments?.getDouble(EXTRA_LATITUDE) ?: 0.0 }
    private val longitude by lazy { arguments?.getDouble(EXTRA_LONGITUDE) ?: 0.0 }
    private val lensCamera by lazy { arguments?.getInt(EXTRA_LENS_CAMERA) ?: 1 }
    private val isFaceDetection by lazy { arguments?.getBoolean(EXTRA_IS_FACE_DETECTION) ?: true }
    private val isWaterMark by lazy { arguments?.getBoolean(EXTRA_IS_WATERMARK) ?: true }
    private var mLatitude = 0.0
    private var mLongitude = 0.0
    private var mAltitude = 0.0

    lateinit var onResult: CameraResult
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA

    private var flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, new ->
        binding.btnFlashCamera.setImageResource(
            when (new) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
        )
    }

    override fun onBackPressed() {
        onResult.onResultCancel()
    }

    override val binding: FragmentCameraBinding by lazy {
        FragmentCameraBinding.inflate(
            layoutInflater
        )
    }

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationGPS = LocationLiveData(binding.root.context, pPriority = LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLatitude = latitude
        mLongitude = longitude

        onResult = binding.root.context as CameraResult
        cameraExecutor = Executors.newSingleThreadExecutor()
        lensFacing =
            if (lensCamera == 1) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA

        onRunPermission(
            listenerGranted = { startCamera() },
            listenerDeny = { Toast.makeText(binding.root.context, "Please allow all permissions", Toast.LENGTH_SHORT).show() }
        )

        binding.viewFinder.setOnTouchListener { _, event ->
            val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                binding.viewFinder.width.toFloat(), binding.viewFinder.height.toFloat()
            )
            val autoFocusPoint = factory.createPoint(event.x, event.y)
            try {
                camera?.cameraControl?.startFocusAndMetering(
                    FocusMeteringAction.Builder(
                        autoFocusPoint,
                        FocusMeteringAction.FLAG_AF
                    ).apply {
                        disableAutoCancel()
                    }.build()
                )
            } catch (e: CameraInfoUnavailableException) {
                Log.d("ERROR", "cannot access camera", e)
            }
            true
        }

        if (isWaterMark) binding.tvWatermark.text = setLocation(mLatitude, mLongitude, mAltitude)
        if (!isFaceDetection) binding.btnTakePicture.visibility = View.VISIBLE
        binding.btnTakePicture.setOnClickListener {
            if (isFaceDetection){
                if (binding.graphicOverlay.isFaceDetected.value == true) takePhoto()
            }else takePhoto()
        }
        binding.btnSwitchCamera.setOnClickListener { toggleCamera() }
        binding.btnFlashCamera.setOnClickListener { selectFlash() }
        binding.btnFlashOff.setOnClickListener { closeFlashAndSelect(ImageCapture.FLASH_MODE_OFF) }
        binding.btnFlashOn.setOnClickListener { closeFlashAndSelect(ImageCapture.FLASH_MODE_ON) }
        binding.btnFlashAuto.setOnClickListener { closeFlashAndSelect(ImageCapture.FLASH_MODE_AUTO) }

        observeVM()
    }

    private fun observeVM() {
        binding.graphicOverlay.isFaceDetected.observe(viewLifecycleOwner) {
            if (isFaceDetection)
                if (it) {
                    binding.btnTakePicture.visibility = View.VISIBLE
                } else {
                    binding.btnTakePicture.visibility = View.INVISIBLE
                }
        }

        locationGPS.observe(viewLifecycleOwner){
            it?.let {
                mLatitude = it.latitude
                mLongitude = it.longitude
                mAltitude = it.altitude
                if (isWaterMark) binding.tvWatermark.text = setLocation(
                    mLatitude,
                    mLongitude,
                    mAltitude
                )
            }
        }
    }

    private fun selectAnalyzer(): ImageAnalysis.Analyzer {
        binding.graphicOverlay.cameraSelector = lensFacing
        return FaceContourDetectionProcessor(binding.graphicOverlay)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(binding.root.context)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, selectAnalyzer())
                }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = lensFacing

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = if (isFaceDetection)
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalyzer
                    )
                else
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture
                    )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(binding.root.context))
    }

    private fun takePhoto() {
        binding.btnTakePicture.setColorFilter(
            ContextCompat.getColor(
                binding.root.context,
                R.color.colorButtonGreyBg
            )
        )
        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnTakePicture.setColorFilter(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.white
                )
            )
        }, 500)

        val imageCapture = imageCapture ?: return

        setFolderDatabase(outputDirectory)
        val photoFile = File(
            outputDirectory,
            "IMG_" + SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    onResult.onResulError(exc.message.toString())
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    lifecycleScope.launch {
                        val savedUri = Uri.fromFile(photoFile)
                        if (isWaterMark){
                            val convertBmp = BitmapFactory.decodeFile(savedUri.toFile().absolutePath)
                            val bitmap = drawMultilineTextToBitmap(
                                binding.root.context,
                                convertBmp,
                                setLocation(mLatitude, mLongitude, mAltitude),
                                12
                            )
                            saveBitmap(savedUri.toFile().absolutePath, bitmap, maxSize) {
                                if (!it)
                                    Toast.makeText(binding.root.context, "Photo save failed", Toast.LENGTH_SHORT).show()
                                onResult.onImageResult(savedUri.toFile())
                            }
                        }else {
                            compressFile(outputDirectory, binding.root.context, savedUri.toFile(), maxSize)?.let { newFile ->
                                onResult.onImageResult(newFile)
                            } ?: onResult.onImageResult(savedUri.toFile())
                        }
                    }
                }
            })
    }

    @SuppressLint("RestrictedApi")
    private fun toggleCamera() = binding.btnSwitchCamera.toggleButton(
        flag = lensFacing == CameraSelector.DEFAULT_BACK_CAMERA,
        rotationAngle = 180f,
        firstIcon = R.drawable.ic_outline_camera_rear,
        secondIcon = R.drawable.ic_outline_camera_front,
    ) {
        if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
            binding.btnFlashCamera.visibility = View.INVISIBLE
        } else {
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
            binding.btnFlashCamera.visibility = View.VISIBLE
        }

        startCamera()
    }

    private fun selectFlash() {
        if (binding.llFlashOptions.isShown)
            binding.llFlashOptions.visibility = View.GONE
        else
            binding.llFlashOptions.visibility = View.VISIBLE
    }

    private fun closeFlashAndSelect(flash: Int) {
        flashMode = flash
        binding.btnFlashCamera.setImageResource(
            when (flash) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
                else -> R.drawable.ic_flash_auto
            }
        )
        imageCapture?.flashMode = flashMode
        binding.llFlashOptions.visibility = View.GONE
    }

    private fun setLocation(latitude: Double, longitude: Double, mAltitude: Double): String? {
        val address = getAddressFromGPS(binding.root.context, latitude, longitude)
        if (address != null) {
            var text = ""
            text += address.address
            text += "\n"
            text += "Lat : ${address.latitude}, Lon : ${address.longitude}, Alt : ${String.format("%.1f", mAltitude).toDouble()}m"
            text += "\n"
            text += address.timeStamp
            return text
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
