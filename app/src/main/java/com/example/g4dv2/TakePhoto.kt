//package com.example.g4d_camera
//
//import android.app.Activity
//import android.content.ContentValues
//import android.content.Context
//import android.os.Build
//import android.provider.MediaStore
//import android.util.AttributeSet
//import android.view.View
//import android.view.View.OnClickListener
//import android.widget.Toast
//import androidx.camera.core.AspectRatio
//import androidx.camera.core.Camera
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.Preview
//import androidx.camera.core.resolutionselector.AspectRatioStrategy
//import androidx.camera.core.resolutionselector.ResolutionSelector
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.core.content.ContextCompat
//import java.io.File
//import java.text.DecimalFormat
//import java.text.SimpleDateFormat
//import java.util.Locale
//import kotlin.math.abs
//
//class CaptureControl (activity: Activity) {
//
//    private var mLensFacing = CameraSelector.LENS_FACING_BACK
//    public fun TakePhoto(myaw: Float, mpitch: Float, mroll: Float) {
//        val imageFolder = getOutputDirectory()
//        val decFormat = DecimalFormat("###,##000.00")
//        val cameraCoordinate = "Y${decFormat.format(myaw)}P${decFormat.format(mpitch)}R${decFormat.format(mroll)}"
////        Toast.makeText(this,cameraCoordinate,Toast.LENGTH_LONG).show()
//        val fileName = SimpleDateFormat("yyMMddHHmmss", Locale.getDefault())
//            .format(System.currentTimeMillis()) + cameraCoordinate +".jpg"
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
//            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/G4D_Imagens")
//            }
//        }
//        val metadata = ImageCapture.Metadata().apply {
//            isReversedHorizontal = (mLensFacing == CameraSelector.LENS_FACING_FRONT)
//        }
//        val outputOption =
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                ImageCapture.OutputFileOptions.Builder(
//                    contentResolver,
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    contentValues
//                ).setMetadata(metadata).build()
//            } else {
//                val imageFile = File(imageFolder, fileName)
//                ImageCapture.OutputFileOptions.Builder(imageFile)
//                    .setMetadata(metadata).build()
//            }
//        mImageCapture.takePicture(
//            outputOption,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    val message = "Photo Capture Succeeded: ${outputFileResults.savedUri}"
//                    Toast.makeText(
//                        this,
//                        fileName,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        exception.message.toString(),
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        )
//    }
//
//    private fun getOutputDirectory(): File {
//        val mediaDir = externalMediaDirs.firstOrNull().let { mFile ->
//            File(mFile, resources.getString(R.string.app_name)).apply {
//                mkdirs()
//            }
//
//        }
//        return if (mediaDir.exists())
//            mediaDir else filesDir
//    }
//
//}