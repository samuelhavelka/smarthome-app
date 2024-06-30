package com.example.cse535_smarthome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class Screen2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen2)

        // map gestures to filenames
        val gestureDict = mapOf("Turn on lights" to "LightOn",
            "Turn off lights" to "LightOff",
            "Turn on fan" to "FanOn",
            "Turn off fan" to "FanOff",
            "Increase fan speed" to "FanUp",
            "Decrease fan speed" to "FanDown",
            "Set Thermostat to specified temperature" to "SetThermo",
            "0" to "Num0","1" to "Num1","2" to "Num2","3" to "Num3","4" to "Num4","5" to "Num5",
            "6" to "Num6","7" to "Num7","8" to "Num8","9" to "Num9")

        // get gesture from Main Activity
        val gestureText = intent.getStringExtra("gestureText")
        val receiverText = findViewById<TextView>(R.id.GestureText)
        receiverText.text = gestureText

        val selectedGesture = gestureDict[gestureText].toString().lowercase()
        val uri = Uri.parse("android.resource://$packageName/raw/$selectedGesture")

        // Practice Button
        val nextButton = findViewById<Button>(R.id.button2_practice)
        nextButton.setOnClickListener {

            val intentNext = Intent(this@Screen2, Screen3::class.java)
            intentNext.putExtra("gestureID", gestureDict[gestureText].toString())

            startActivity(intentNext)
        }

        // Change Gesture Button
        val backButton = findViewById<Button>(R.id.button2_back)
        backButton.setOnClickListener {
            val intentBack = Intent(this@Screen2, MainActivity::class.java)
            startActivity(intentBack)
        }

        // Get the VideoView and MediaController objects
        val videoView = findViewById<VideoView>(R.id.videoView)
        val mediaController = MediaController(this)

        // set the URI for video
        videoView.setVideoURI(uri)

        videoView.setMediaController(mediaController)
        mediaController.setAnchorView(videoView)
        videoView.setOnPreparedListener { it.isLooping = true }

        // play video
        videoView.start()
    }
}