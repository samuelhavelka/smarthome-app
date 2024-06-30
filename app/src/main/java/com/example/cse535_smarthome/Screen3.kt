package com.example.cse535_smarthome

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.cse535_smarthome.databinding.ActivityScreen3Binding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Screen3 : AppCompatActivity() {

    private lateinit var viewBinding: ActivityScreen3Binding

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        viewBinding = ActivityScreen3Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // get gestureID for filename and path
        val gestureID = intent.getStringExtra("gestureID").toString()
        val filename = gestureID + "_PRACTICE.mp4"
        val filepath = "/storage/emulated/0/Movies/CameraX-Video/$filename"

        // set video capture button to start
        viewBinding.videoCaptureButton.apply {
            text = getString(R.string.start_capture)
            isEnabled = true
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.videoCaptureButton.setOnClickListener {

            // If file already exists, delete it (only occurs if user retakes video)
            val file = File(filepath)
            if (file.exists()) {
                file.delete()
            }

            // hide prompt and disable upload button
            viewBinding.promptUser.visibility = View.INVISIBLE
            viewBinding.uploadButton.isEnabled = false

            captureVideo(filename)
        }
        cameraExecutor = Executors.newSingleThreadExecutor()


        // Upload button
        viewBinding.uploadButton.setOnClickListener {

            viewBinding.promptUser.visibility = View.INVISIBLE

            // upload video to server
            postRequest(filename, filepath)

            // go to MainActivity
            val intentUpload = Intent(this@Screen3, MainActivity::class.java)
            startActivity(intentUpload)
        }

    }

    // Upload file to Flask Server
    private fun postRequest(filename : String, filepath : String) {

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", filename, File(filepath).asRequestBody("video/mp4".toMediaTypeOrNull()))
            .build()

        val okHttpClient = OkHttpClient()

        val request = Request.Builder()
            .url("http://192.168.1.67:5000/upload")
            .post(requestBody)
            .build()

        // making call asynchronously
        okHttpClient.newCall(request).enqueue(object : Callback {

            // called if server is unreachable
            override fun onFailure(call: Call, e: IOException) {
                Log.d("debug", e.toString())
                call.cancel()
            }

            // called if we get a response from the server
            override fun onResponse(call: Call, response: Response) {
                Log.d("debug", response.toString())
                runOnUiThread {
                    Toast.makeText(this@Screen3, "File Upload Successful", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

    // Function for displaying counter
    private fun mStartCounter(view: TextView, delay: Long){
        Thread{
            for(i in 0..delay/1000){
                runOnUiThread {
                    view.text = ((delay/1000) - i).toString()
                }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun captureVideo(filename : String) {
        val videoCapture = this.videoCapture ?: return

        viewBinding.videoCaptureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        viewBinding.textTimer.visibility = View.VISIBLE
        mStartCounter(viewBinding.textTimer, 3000)

        // start camera recording after 3 sec
        Handler(Looper.getMainLooper()).postDelayed( {

            viewBinding.textTimer.visibility = View.INVISIBLE

            // create and start a new recording session
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
                }
            }

            val mediaStoreOutputOptions = MediaStoreOutputOptions
                .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build()
            recording = videoCapture.output
                .prepareRecording(this, mediaStoreOutputOptions)
                .apply {
                    if (PermissionChecker.checkSelfPermission(this@Screen3,
                            Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED)
                    {
                        withAudioEnabled()
                    }
                }
                .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                    when(recordEvent) {
                        is VideoRecordEvent.Start -> {
                            viewBinding.videoCaptureButton.apply {
                                text = getString(R.string.stop_capture)
                                isEnabled = true
                            }
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                val msg = "Video capture succeeded: " + "${recordEvent.outputResults.outputUri}"
//                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                                Log.d(TAG, msg)
                            } else {
                                recording?.close()
                                recording = null
                                Log.e(TAG, "Video capture ends with error: " + "${recordEvent.error}")
                            }
                            viewBinding.videoCaptureButton.apply {
                                text = getString(R.string.retry)
                                isEnabled = true
                            }

                            // make prompt visible
                            viewBinding.promptUser.visibility = View.VISIBLE

                            // enable upload button
                            viewBinding.uploadButton.isEnabled = true
                        }
                    }
                }

            // after 5 sec stop recording automatically
            Handler(Looper.getMainLooper()).postDelayed( {
                val xRecording = recording
                if (xRecording != null) {
                    xRecording.stop()
                    recording = null
                }
            }, 5000)

        }, 3000)

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener( {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // prompt user to allow required permissions
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "cse535_smarthome"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }
}