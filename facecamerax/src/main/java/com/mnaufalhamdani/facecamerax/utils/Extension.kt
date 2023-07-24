@file:Suppress("DEPRECATION", "OPT_IN_IS_NOT_ENABLED")

package com.mnaufalhamdani.facecamerax.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.widget.ImageButton
import androidx.annotation.DrawableRes
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


object Constant {
    const val latitude = -6.7821934
    const val longitude = 110.9887358
}

@OptIn(DelicateCoroutinesApi::class)
fun ImageButton.toggleButton(
    flag: Boolean, rotationAngle: Float, @DrawableRes firstIcon: Int, @DrawableRes secondIcon: Int,
    action: (Boolean) -> Unit
) {
    if (flag) {
        if (rotationY == 0f) rotationY = rotationAngle
        animate().rotationY(0f).apply {
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    action(!flag)
                }
            })
        }.duration = 200
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            setImageResource(firstIcon)
        }
    } else {
        if (rotationY == rotationAngle) rotationY = 0f
        animate().rotationY(rotationAngle).apply {
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    action(!flag)
                }
            })
        }.duration = 200
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            setImageResource(secondIcon)
        }
    }
}

fun drawMultilineTextToBitmap(context: Context, resId: Bitmap, waterMark: String?, textSize: Int?): Bitmap {
    //set TextSize
    var mSize = textSize
    if (mSize == null) mSize = 12

    // prepare canvas
    val resources = context.resources
    val scale = resources.displayMetrics.density
    var bitmap = resId
    var bitmapConfig = bitmap.config
    // set default bitmap config if none
    if (bitmapConfig == null) {
        bitmapConfig = Bitmap.Config.ARGB_8888
    }

    // resource bitmaps are imutable,
    // so we need to convert it to mutable one
    bitmap = bitmap.copy(bitmapConfig, true)
    val canvas = Canvas(bitmap)

    // new antialiased Paint
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    // text color - #3D3D3D
    paint.color = Color.WHITE
    // text size in pixels
    paint.textSize = mSize * scale * 2

    // set text width to canvas width minus 16dp padding
    val textWidth = canvas.width - (16 * scale).toInt()
    // init StaticLayout for text
    val textLayout = StaticLayout(
        waterMark, paint, textWidth, Layout.Alignment.ALIGN_NORMAL,
        1.0f, 2.0f, false
    )

    // get height of multiline text
    val textHeight = textLayout.height
    // get position of text's top left corner
    val x = (bitmap.width - textWidth) / 2
    val y = (bitmap.height - textHeight) * 98 / 100

    // draw text to the Canvas center
    canvas.save()
    canvas.translate(x.toFloat(), y.toFloat())
    textLayout.draw(canvas)
    canvas.restore()
    return bitmap
}

fun convertPathToBitmap(imagePath: Uri, context: Context): Bitmap? {
    try {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imagePath)
        } else {
            val source: ImageDecoder.Source = ImageDecoder.createSource(context.contentResolver, imagePath)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        }
    }catch (e: Exception){
        Log.d("convertPathToBitmap", e.message.toString())
        return null
    }
}

fun saveBitmap(context: Context, path: String, bitmap: Bitmap, compressQuality: Int, pathGallery: String, isAddToGallery: Boolean = false, listener: (Boolean) -> Unit) {
    try {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        if (!isAddToGallery) {
            try {
                context.apply {
                    val gFile = File("$pathGallery.nomedia")
                    gFile.createNewFile()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, out)
        out.flush()
        out.close()
        if (isAddToGallery) galleryAddPic(context, file)
    } catch (e: IOException) {
        listener(false)
        e.printStackTrace()
    } catch (e: OutOfMemoryError) {
        listener(false)
        e.printStackTrace()
    } catch (e: java.lang.Exception) {
        listener(false)
        e.printStackTrace()
    } finally {
        listener(true)
    }
}

fun rotateImage(img: Bitmap, degree: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
}

@Throws(IOException::class)
fun galleryAddPic(context: Context, file: File?) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    file?.let {
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
    }
    context.sendBroadcast(mediaScanIntent)
}

fun getAddressFromGPS(context: Context, latitude: Double, longitude: Double, ): AddressDomain? {
    val fileNameFormat = "dd-MM-yyyy HH:mm:ss"
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        if (!addresses.isNullOrEmpty()) {
            return AddressDomain(
                address = addresses[0].getAddressLine(0),
                latitude = latitude.toString(),
                longitude = longitude.toString(),
                timeStamp = SimpleDateFormat(
                    fileNameFormat,
                    Locale.US
                ).format(System.currentTimeMillis())
            )
        }else return null
    }catch (e: Exception){
        Log.e("getAddressFromGPS", e.message.toString())
        return AddressDomain(
            address = "Alamat tidak diketahui, mohon muat ulang dan pastikan koneksi internet Anda stabil.",
            latitude = "-",
            longitude = "-",
            timeStamp = SimpleDateFormat(
                fileNameFormat,
                Locale.US
            ).format(System.currentTimeMillis())
        )
    }
}

// TO PREVENT DOUBLE CLICK
private var mLastClickTime: Long = 0
fun singleClick(): Boolean {
    // mis-clicking prevention, using threshold of 1000 ms
    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
        return false
    }
    mLastClickTime = SystemClock.elapsedRealtime()
    return true
}

suspend fun compressFile(
    path: String,
    context: Context,
    imageFile: File,
    compressQuality: Int = 80,
    isAddToGallery: Boolean = false
): File? {
    val folder =
        File(path)
    var success = true
    if (!folder.exists()) {
        success = folder.mkdirs()
    }
    if (success) {
        if (!isAddToGallery) {
            try {
                context.apply {
                    val gFile = File("$path.nomedia")
                    gFile.createNewFile()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            val fileName = imageFile.name
            val newPath = File(path + fileName)
            val imageCompress = Compressor.compress(context, imageFile) {
                quality(compressQuality)
                destination(newPath)
            }
            if (isAddToGallery) galleryAddPic(context, imageCompress)
            return imageCompress
        } catch (e: Exception) {
            Log.e("FileUtil", e.message.toString())
        }
    } else {
        Log.e("FileUtil", "Cannot Create Folder")
    }
    return null
}
