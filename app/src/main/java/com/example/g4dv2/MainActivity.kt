package com.example.g4dv2


import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.provider.MediaStore
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.example.g4dv2.databinding.ActivityMainBinding
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs


class MainActivity : AppCompatActivity(), com.example.g4dv2.Orientation.Listener {
    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
        )
    } else {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
    }

    private lateinit var mOrientation: com.example.g4dv2.Orientation
    private lateinit var mAttitudeIndicator: AttitudeIndicator
    private lateinit var mImageCapture: ImageCapture
    private lateinit var mCameraProvider: ProcessCameraProvider
    private lateinit var mCamera: Camera
    private lateinit var mCameraSelector: CameraSelector
    private lateinit var mLocation: Location
    //    private lateinit var mCaptureControl: CaptureControl
    private var mOrientationEventListener: OrientationEventListener? = null
    private var mLensFacing = CameraSelector.LENS_FACING_BACK
    private var myaw: Float = 0.0f
    private var mpitch: Float = 0.0f
    private var mroll: Float = 0.0f



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)
        mOrientation = Orientation(this)
        mAttitudeIndicator = findViewById(R.id.attitude_indicator)
//        mTakePhoto = TakePhoto(this, this,findViewById(R.id.captureIB))
//        mCaptureControl = CaptureControl(this@MainActivity)

        if (checkMultiplePermission()) {
            startCamera()
        }

//        mainBinding.flipCameraIB.setOnClickListener {
//            mLensFacing = if (mLensFacing == CameraSelector.LENS_FACING_FRONT) {
//                CameraSelector.LENS_FACING_BACK
//            } else {
//                CameraSelector.LENS_FACING_FRONT
//            }
//            bindCameraUserCases()
//        }
        mainBinding.captureIB.setOnClickListener {
            takePhoto()
        }
        mainBinding.flashToggleIB.setOnClickListener {
            setFlashIcon(mCamera)
        }

    }
    override fun onStart() {
        super.onStart()
//        mOrientation.startListening(this)
    }

    override fun onResume() {
        super.onResume()
        mOrientation.startListening(this)
        mOrientationEventListener?.enable()
    }

    override fun onPause() {
        super.onPause()
        mOrientation.stopListening()
        mOrientationEventListener?.disable()
    }
    override fun onStop() {
        super.onStop()
    }







    public fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            mCameraProvider = cameraProviderFuture.get()
            bindCameraUserCases()
        }, ContextCompat.getMainExecutor(this))
    }
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = maxOf(width, height).toDouble() / minOf(width, height)
        return if (abs(previewRatio - 4.0 / 3.0) <= abs(previewRatio - 16.0 / 9.0)) {
            AspectRatio.RATIO_4_3
        } else {
            AspectRatio.RATIO_16_9
        }
    }
    private fun bindCameraUserCases() {
        val screenAspectRatio = aspectRatio(
            mainBinding.previewView.width,
            mainBinding.previewView.height
        )
        val rotation = mainBinding.previewView.display.rotation
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    screenAspectRatio,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()
        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(mainBinding.previewView.surfaceProvider)
            }
        mImageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
        mCameraSelector = CameraSelector.Builder()
            .requireLensFacing(mLensFacing)
            .build()
        mOrientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                mImageCapture.targetRotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }
        }
        mOrientationEventListener?.enable()
        try {
            mCameraProvider.unbindAll()

            mCamera = mCameraProvider.bindToLifecycle(
//            this, cameraSelector, preview, imageCapture,videoCapture
                this, mCameraSelector, preview, mImageCapture
            )
//        setUpZoomTapToFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun setFlashIcon(camera: Camera) {
        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value == 0) {
                camera.cameraControl.enableTorch(true)
                mainBinding.flashToggleIB.setImageResource(R.drawable.flash_off)
            } else {
                camera.cameraControl.enableTorch(false)
                mainBinding.flashToggleIB.setImageResource(R.drawable.flash_on)
            }
        } else {
            Toast.makeText(
                this,
                "Flash is Not Available",
                Toast.LENGTH_LONG
            ).show()
            mainBinding.flashToggleIB.isEnabled = false
        }
    }
    private fun takePhoto() {

        var imageFolder = getOutputDirectory()
//        Toast.makeText(this,cameraCoordinate,Toast.LENGTH_LONG).show()
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/G4Dv2_Imagens")
                imageFolder = File(Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_PICTURES + "/G4Dv2_Imagens" + "/")
            }
        }
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = (mLensFacing == CameraSelector.LENS_FACING_FRONT);
//            setLocation(Location location)
        }
        val outputOption =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                ImageCapture.OutputFileOptions.Builder(
                    contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).setMetadata(metadata).build()
            } else {
                val imageFile = File(imageFolder, fileName)
                ImageCapture.OutputFileOptions.Builder(imageFile)
                    .setMetadata(metadata).build()
            }
        mImageCapture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    Toast.makeText(this@MainActivity,fileName,Toast.LENGTH_SHORT).show()
                    try {
                        saveEXIF(File(imageFolder, fileName))
                    }catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "File not found. Error:" + e.message.toString(), Toast.LENGTH_SHORT).show()}
                }
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@MainActivity,
                        exception.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull().let { mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir.exists())
            mediaDir else filesDir
    }
//    private fun getGalleryPath(): File {
//        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//        if (!folder.exists()) {
//            folder.mkdir()
//        }
//        return folder
//    }



    override fun onOrientationChanged(yaw: Float, pitch: Float, roll: Float) {
//        mainBinding.yaw.setText(String.format("%.1f", yaw))
//        mainBinding.pitch.setText(String.format("%.1f", pitch))
//        mainBinding.roll.setText(String.format("%.1f", roll))
//        mainBinding.val03.setText(String.format("%.4f", val03))
//            mainBinding.val04.setText(pitch.toString())
//            mainBinding.val05.setText(roll.toString())

        mAttitudeIndicator.setAttitude(pitch, roll)
        myaw =  yaw
        mpitch = pitch
        mroll = roll
    }

    private fun saveEXIF(imageFile: File){

        val decFormat = DecimalFormat("###,##000.00")
        val cameraCoordinate = "Y${decFormat.format(myaw)}P:${decFormat.format(mpitch)};R${decFormat.format(mroll)};"
        val exifInterface = ExifInterface(imageFile)
        exifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, cameraCoordinate)
        exifInterface.setAttribute(ExifInterface.TAG_COPYRIGHT, "All rights reserved by Garagem 4D.\nGaragem 4D reserves the exclusive right to capture, store, and process images using its proprietary technology and equipment. Any unauthorized use, reproduction, modification, or distribution of these images, or any part thereof, is strictly prohibited without prior written permission from Garagem 4D.\nViolation of these rights may result in legal action.")
        exifInterface.setAttribute(ExifInterface.TAG_ARTIST, "G4D_App")
        exifInterface.saveAttributes()


//        mExifInterface.setAltitude(0.999)
//        mExifInterface.hasAttribute()
//        mExifInterface.getAttribute()





//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_IMAGE_DESCRIPTION()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_IMAGE_LENGTH()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_IMAGE_UNIQUE_ID()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_IMAGE_WIDTH()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_LENS_SPECIFICATION()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_ORIENTATION()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_RESOLUTION_UNIT()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_USER_COMMENT()



//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_APERTURE_VALUE()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_ARTIST()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_CAMERA_OWNER_NAME()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_COPYRIGHT()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_DATETIME()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_DATETIME_DIGITIZED()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_DATETIME_ORIGINAL()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_DEFAULT_CROP_SIZE()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_DEVICE_SETTING_DESCRIPTION()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_DIGITAL_ZOOM_RATIO()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_FILE_SOURCE()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_FLASH()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_FOCAL_LENGTH()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_FOCAL_LENGTH_IN_35MM_FILM()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_FOCAL_PLANE_RESOLUTION_UNIT()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_FOCAL_PLANE_X_RESOLUTION()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_FOCAL_PLANE_Y_RESOLUTION()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_ALTITUDE()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_ALTITUDE_REF()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_AREA_INFORMATION()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_DATESTAMP()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_DIFFERENTIAL()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_DOP()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_H_POSITIONING_ERROR()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_IMG_DIRECTION()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_IMG_DIRECTION_REF()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_LATITUDE()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_LATITUDE_REF()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_LONGITUDE()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_LONGITUDE_REF()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_MAP_DATUM()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_MEASURE_MODE()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_SATELLITES()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_SPEED()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_SPEED_REF()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_STATUS()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_TIMESTAMP()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_TRACK()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_TRACK_REF()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_GPS_TRACK_REF()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_ISO_SPEED()
//        https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface#TAG_INTEROPERABILITY_INDEX()



    }
//    @Throws(IOException::class)
//    private fun testSaveAttributes_withFileName(fileName: String) {
//        val imageFile = File(Environment.getExternalStorageDirectory(), fileName)
//
//        var exifInterface = ExifInterface(imageFile.absolutePath)
//        exifInterface.saveAttributes()
//        exifInterface = ExifInterface(imageFile.absolutePath)
//
//        // Test for modifying one attribute.
//        val backupValue = exifInterface.getAttribute(ExifInterface.TAG_MAKE)
//        exifInterface.setAttribute(ExifInterface.TAG_MAKE, "abc")
//        exifInterface.saveAttributes()
//        exifInterface = ExifInterface(imageFile.absolutePath)
//        // Restore the backup value.
//        exifInterface.setAttribute(ExifInterface.TAG_MAKE, backupValue)
//        exifInterface.saveAttributes()
//        exifInterface = ExifInterface(imageFile.absolutePath)
//    }













































    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
                    startCamera()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(this)
                    } else {
                        // here warning permission show
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }

}