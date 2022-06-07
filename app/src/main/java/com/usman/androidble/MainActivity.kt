package com.usman.androidble

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.usman.androidBleNative.BleUtilityNative
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inits()
    }

    private fun inits() {
        val utility = BleUtilityNative()
        utility.connectToDevice(this,"wrtwetwywue" ,{

        },{
            utility.getCharacteristics().find { it.uuid == UUID.randomUUID() }?.let {
                utility.writeCharacteristic("sdfdfdf".toByteArray(),
                    it ,{},{},{})
            }
        },{

        })
    }
}