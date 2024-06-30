package com.example.cse535_smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // define list of gestures
        val gestures = arrayOf<String>("", "Turn on lights", "Turn off lights", "Turn on fan", "Turn off fan",
            "Increase fan speed", "Decrease fan speed", "Set Thermostat to specified temperature",
            "0","1","2","3","4","5","6","7","8","9")

        // access Spinner on Screen 1
        val spinner = findViewById<Spinner>(R.id.GestureList)
        val ad: ArrayAdapter<*> = ArrayAdapter<Any?>(this,android.R.layout.simple_spinner_item,gestures)
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = ad

        // go to screen 2 on item selected
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?,
                                        selectedItemView: View?,
                                        position: Int,
                                        id: Long
            ) {
                if(position>0) {
                    val intent = Intent(this@MainActivity, Screen2::class.java)

                    intent.putExtra("gestureText", spinner.selectedItem.toString())
                    startActivity(intent)

                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // TODO
            }
        }
    }
}