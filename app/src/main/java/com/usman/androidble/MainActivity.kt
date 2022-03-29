package com.usman.androidble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inits()
    }

    private fun inits() {
        var utility = BleUtilityNative()
//        utility.connectToDevice(this)
    }
}