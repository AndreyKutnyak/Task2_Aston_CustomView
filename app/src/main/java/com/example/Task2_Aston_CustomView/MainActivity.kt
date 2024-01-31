package com.example.Task2_Aston_CustomView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var customViewDrum: CustomViewDrum
    private lateinit var startButton: Button
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customViewDrum = findViewById(R.id.spinWheelView)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)

        startButton.setOnClickListener {
            customViewDrum.startSpinning()
        }

        resetButton.setOnClickListener {
            customViewDrum.resetState()
        }
    }
}