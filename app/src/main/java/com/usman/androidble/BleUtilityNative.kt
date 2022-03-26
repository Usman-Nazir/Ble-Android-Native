package com.usman.androidble

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.usman.androidble.util.hasProperty
import java.util.*


class BleUtilityNative {

    var context:Context?= null
    val allCharacteristics: MutableList<BluetoothGattCharacteristic> = arrayListOf()
    var bleDevice: BluetoothDevice? = null
    private var characteristicUuid: UUID = UUID.fromString("10fc308a-f1f9-493c-9643-6a9f016f5ecd")

    var bluetoothGatt: BluetoothGatt? = null
    var connectionErrorR: ((Throwable) -> Unit)? = null
    var connectionStateCallback: ((Int) -> Unit)? = null
    var servicesDiscovered: (() -> Unit)? = null
    var connectionStatus: Int? = null
    var gattServer: BluetoothGatt? = null

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            connectionStateCallback?.invoke(status)
            connectionStatus = newState
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                gatt?.discoverServices()
                gattServer = gatt
                // successfully connected to the GATT Server
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            gatt?.services?.forEach {
                it?.characteristics?.let {
                    allCharacteristics.addAll(it)
                }
            }
            servicesDiscovered?.invoke()
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    callBackSuccess.find {
                        it.second.uuid == characteristic?.uuid && it.third.contentEquals(
                            characteristic?.value
                        )
                    }?.let {
                        it.first.invoke(it.third)
                        callBackSuccess.remove(it)
                    }
                }
                else -> {
                    callBackFailure.find {
                        it.second.uuid == characteristic?.uuid && it.third.contentEquals(
                            characteristic?.value
                        )
                    }?.let {
                        it.first.invoke(Throwable(status.toString()))
                        callBackFailure.remove(it)
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    callBackSuccess.find {
                        it.second.uuid == characteristic?.uuid && it.third.contentEquals(
                            characteristic?.value
                        )
                    }?.let {
                        it.first.invoke(it.third)
                        callBackSuccess.remove(it)
                    }
                }
                else -> {
                    callBackFailure.find {
                        it.second.uuid == characteristic?.uuid && it.third.contentEquals(
                            characteristic?.value
                        )
                    }?.let {
                        it.first.invoke(Throwable(status.toString()))
                        callBackFailure.remove(it)
                    }
                }
            }
        }
    }


//    private val disconnectTriggerSubject = PublishSubject.create<Unit>()
    fun triggerDisconnect() = run {
        if (connectionStatus == BluetoothProfile.STATE_CONNECTED) {
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            gattServer?.disconnect()
            bleDevice = null
        }
    }

    private fun disConnectDevice() {

    }


    fun connectToDevice(
        context: Context,
        it: BluetoothDevice?,
        connectionError: (Throwable) -> Unit,
        servicesDiscovered: () -> Unit,
        connectionStateCallback: (Int) -> Unit
    ) {
        allCharacteristics.clear()
        bleDevice = null
        bleDevice = it

        connectionErrorR = connectionError
        this.connectionStateCallback = connectionStateCallback
        this.servicesDiscovered = servicesDiscovered
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bluetoothGatt = bleDevice?.connectGatt(context, false, bluetoothGattCallback)
    }


    fun onNotifyCharacteristic(
        characteristicUuid: BluetoothGattCharacteristic,
        callBackSuccess: (ByteArray) -> Unit,
        callBackFailure: (Throwable) -> Unit
    ) {
        if (connectionStatus == BluetoothProfile.STATE_CONNECTED) {
            this.callBackSuccess.add(
                Triple(
                    callBackSuccess,
                    characteristicUuid,
                    characteristicUuid.value
                )
            )
            this.callBackFailure.add(
                Triple(
                    callBackFailure,
                    characteristicUuid,
                    characteristicUuid.value
                )
            )

            if (characteristicUuid.hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                gattServer?.setCharacteristicNotification(characteristicUuid, true)
            } else {
                callBackFailure.invoke(Throwable("Not writable property"))
                this.callBackFailure.find { it.first == callBackFailure }
                    ?.let { this.callBackFailure.remove(it) }
            }
        } else {
            callBackFailure.invoke(Throwable("Device Not Connected"))
            this.callBackFailure.find { it.first == callBackFailure }
                ?.let { this.callBackFailure.remove(it) }
        }
    }


    fun readCharacteristic(
        characteristicUuid: BluetoothGattCharacteristic,
        callBackSuccess: (ByteArray) -> Unit,
        callBackFailure: (Throwable) -> Unit
    ) {
        if (connectionStatus == BluetoothProfile.STATE_CONNECTED) {
            this.callBackSuccess.add(
                Triple(
                    callBackSuccess,
                    characteristicUuid,
                    characteristicUuid.value
                )
            )
            this.callBackFailure.add(
                Triple(
                    callBackFailure,
                    characteristicUuid,
                    characteristicUuid.value
                )
            )

            if (characteristicUuid.hasProperty(BluetoothGattCharacteristic.PROPERTY_READ)) {
                if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                gattServer?.readCharacteristic(characteristicUuid)
            } else {
                callBackFailure.invoke(Throwable("Not writable property"))
                this.callBackFailure.find { it.first == callBackFailure }
                    ?.let { this.callBackFailure.remove(it) }
            }
        } else {
            callBackFailure.invoke(Throwable("Device Not Connected"))
            this.callBackFailure.find { it.first == callBackFailure }
                ?.let { this.callBackFailure.remove(it) }
        }
    }


    var callBackSuccess: MutableList<Triple<(ByteArray) -> Unit, BluetoothGattCharacteristic, ByteArray>> =
        arrayListOf()
    var callBackFailure: MutableList<Triple<(Throwable) -> Unit, BluetoothGattCharacteristic, ByteArray>> =
        arrayListOf()

    fun writeCharacteristic(
        inputBytes: ByteArray,
        characteristicUuid: BluetoothGattCharacteristic,
        callBackSuccess: (ByteArray) -> Unit,
        callBackFailure: (Throwable) -> Unit
    ) {

        if (connectionStatus == BluetoothProfile.STATE_CONNECTED) {
            this.callBackSuccess.add(Triple(callBackSuccess, characteristicUuid, inputBytes))
            this.callBackFailure.add(Triple(callBackFailure, characteristicUuid, inputBytes))

            if (characteristicUuid.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)) {
                characteristicUuid.value = inputBytes
                if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                gattServer?.writeCharacteristic(characteristicUuid)
            } else {
                callBackFailure.invoke(Throwable("Not writable property"))
                this.callBackFailure.find { it.first == callBackFailure }
                    ?.let { this.callBackFailure.remove(it) }
            }
        } else {
            callBackFailure.invoke(Throwable("Device Not Connected"))
            this.callBackFailure.find { it.first == callBackFailure }
                ?.let { this.callBackFailure.remove(it) }
        }
    }

    fun getCharacteristics(): MutableList<BluetoothGattCharacteristic> {
        return allCharacteristics
    }

    fun BluetoothDevice.pairDevice() {
        var TAG = "test"
        try {
            javaClass.getMethod("createBond").invoke(this)
        } catch (e: Exception) {
            Log.e(TAG, "Removing bond has been failed. ${e.message}")
        }
    }

}