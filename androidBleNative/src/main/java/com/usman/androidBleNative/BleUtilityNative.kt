package com.usman.androidBleNative

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.usman.androidBleNative.util.hasProperty
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


class BleUtilityNative {
    private val allCharacteristics: MutableList<BluetoothGattCharacteristic> = arrayListOf()
    private var characteristicChanged: AtomicReference<MutableList<Pair<BluetoothGattCharacteristic, ((BluetoothGattCharacteristic?) -> Unit)>>> = AtomicReference(arrayListOf())
    var bleDevice: BluetoothDevice? = null
//    private var characteristicUuid: UUID = UUID.fromString("10fc308a-f1f9-493c-9643-6a9f016f5ecd")


    private var connectionErrorR: ((Throwable) -> Unit)? = null
    private var connectionStateCallback: ((Int) -> Unit)? = null
    private var servicesDiscovered: (() -> Unit)? = null
    var connectionStatus: Int? = null
    var gattServer: BluetoothGatt? = null
    var context: Context? = null

    private val bluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, gatStatus: Int, newState: Int) {
            Handler(Looper.getMainLooper()).post {
                connectionStateCallback?.invoke(newState)
                connectionStatus = newState
            }
            Log.i("test", "gatStatus  ${gatStatus}  newState   ${newState}")
            when (gatStatus) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Handler(Looper.getMainLooper()).post {
                        connectionErrorR?.invoke(Throwable("Gatt Success $gatStatus"))
                    }
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            if (context == null) return
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                return
                            }
                            gatt?.discoverServices()
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            releaseResources()
                        }
                        else -> {
                            Handler(Looper.getMainLooper()).post {
                                connectionErrorR?.invoke(Throwable("Connection Error $gatStatus"))
                                releaseResources()
                            }

                        }
                    }
                }
                133, 22 -> {
                    Handler(Looper.getMainLooper()).post {
                        connectionErrorR?.invoke(Throwable("Gatt Error $gatStatus trying again.."))
                        tryAgain()
                    }
                }
                else -> {
                    Handler(Looper.getMainLooper()).post {
                        connectionErrorR?.invoke(Throwable("Gatt Error $gatStatus"))
                        releaseResources()
                    }

                }
//                        133 ->
//                            Log.w("onConnectionStateChange", "DEVICE_NOT_EXIST")
//                        22 -> // Connection Terminated By Local Host
//                            Log.w("onConnectionStateChange", "LOCAL_HOST_TERMINATED.")
//                        19 -> // Disconnected by device
//                            Log.w("onConnectionStateChange", " Disconnected by device.")
//                        8 ->
//                            Log.w("onConnectionStateChange", "BLE_HCI_CONNECTION_TIMEOUT.")
//                        40 ->  BLE_HCI_INSTANT_PASSED -> Connection is unstable. air -> 링크레이어에서 LL_CHANNEL_MAP_REQ / LL_CONNECTION_UPDATE_REQ가 전송되어 수신되면 40 에러 발생 -> Disconnect
//                            Log.w("onConnectionStateChange", "BLE_HCI_INSTANT_PASSED.")
            }

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, gattStatus: Int) {
            super.onServicesDiscovered(gatt, gattStatus)
            Log.i("test", "services discovered ${gattStatus}")
            gatt?.services?.forEach {
                //                        it?.characteristics?.firstOrNull()?.hasProperty(BluetoothGattCharacteristic.PROPERTY_READ)
                it?.characteristics?.let {
                    allCharacteristics.addAll(it)
                }
            }
            Handler(Looper.getMainLooper()).post {
                servicesDiscovered?.invoke()
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, gatStatus: Int) {
            var gatStatusTemp = 137
            super.onCharacteristicRead(gatt, characteristic, gatStatus)

            when (gatStatus) {
                BluetoothGatt.GATT_SUCCESS -> {
                    //look for next call
                    bleCallsTrack.get().removeFirstOrNull()?.let {
                        if (it.propertyType == BluetoothGattCharacteristic.PROPERTY_READ)
                            Handler(Looper.getMainLooper()).post {
                                characteristic?.value?.let { it1 -> it.callBackSuccess?.invoke(it1) }
                            }
                        handleRemainingQueue()
                    }
                }
//                133 -> {
//                    retryReadCount.incrementAndGet()
//                    if (retryReadCount.get() < 3) {
//                        Handler(Looper.getMainLooper()).post {
//                            connectionErrorR?.invoke(Throwable("Read Error $gatStatus retrying ${retryReadCount.get()}"))
//                        }
//                        Schedulers.io().scheduleDirect({
//                            bleCallsTrack.get().find { it.propertyType == BluetoothGattCharacteristic.PROPERTY_READ }?.let {
//                                if (context ==null)return@let
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                                    return@let
//                                }
//                                gattServer?.readCharacteristic(it.characteristicUuid)
//                            }
//                        }, 1L, TimeUnit.SECONDS)
//                    } else {
//                        Handler(Looper.getMainLooper()).post {
//                            connectionErrorR?.invoke(Throwable("Read Error $gatStatus failed"))
//                        }
//                        retryReadCount.set(0)
//                    }
//                }
//                137 -> {
//                    //do bonding thing
////                    val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
////                    context?.registerReceiver(mReceiver, filter)
//                }
                else -> {
                    Handler(Looper.getMainLooper()).post {
                        connectionErrorR?.invoke(Throwable("Read Error $gatStatus"))
                    }

                    //look for next call
                    bleCallsTrack.get().removeFirstOrNull()?.let {
                        if (it.propertyType == BluetoothGattCharacteristic.PROPERTY_READ)
                            Handler(Looper.getMainLooper()).post {
                                it.callBackFailure.invoke(Throwable(gatStatus.toString()))
                            }
                        handleRemainingQueue()
                    }
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, gatStatus: Int) {
            super.onCharacteristicWrite(gatt, characteristic, gatStatus)
            when (gatStatus) {
                BluetoothGatt.GATT_SUCCESS -> {

                    //look for next call
                    bleCallsTrack.get().removeFirstOrNull()?.let {
                        if (it.propertyType == BluetoothGattCharacteristic.PROPERTY_WRITE)
                            Handler(Looper.getMainLooper()).post {
                                it.inputBytes?.let { it1 -> it.callBackSuccess?.invoke(it1) }
                            }
                        handleRemainingQueue()
                    }
                }
                else -> {
                    Handler(Looper.getMainLooper()).post {
                        connectionErrorR?.invoke(Throwable("Write Error $gatStatus"))
//                        if (gatStatus == 133) {
//                            tryAgain()
//                        }
                    }

                    //look for next call
                    bleCallsTrack.get().removeFirstOrNull()?.let {
                        if (it.propertyType == BluetoothGattCharacteristic.PROPERTY_WRITE)
                            Handler(Looper.getMainLooper()).post {
                                it.callBackFailure.invoke(Throwable(gatStatus.toString()))
                            }
                        handleRemainingQueue()
                    }
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            characteristicChanged?.get().find { it?.first?.uuid == characteristic?.uuid }?.let {
                it?.second?.invoke(characteristic)
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, gatStatus: Int) {
            super.onDescriptorWrite(gatt, descriptor, gatStatus)

            when (gatStatus) {
                BluetoothGatt.GATT_SUCCESS -> {
                    //look for next call
                    bleCallsTrack.get().removeFirstOrNull()?.let {
                        if (it.propertyType == BluetoothGattCharacteristic.PROPERTY_NOTIFY)
                            Handler(Looper.getMainLooper()).post {
                                it.notifyCallBack?.notificationRegisteredSuccess?.invoke(descriptor)
                                it.notifyCallBack?.notificationChangeCallback?.let { callBackFunc->
                                    characteristicChanged?.get().add(Pair( it.characteristicUuid,callBackFunc))
                                }

                            }
                        handleRemainingQueue()
                    }
                }
                else -> {
                    Handler(Looper.getMainLooper()).post {
                        connectionErrorR?.invoke(Throwable("Write Error $gatStatus"))
//                        if (gatStatus == 133) {
//                            tryAgain()
//                        }
                    }

                    //look for next call
                    bleCallsTrack.get().removeFirstOrNull()?.let {
                        if (it.propertyType == BluetoothGattCharacteristic.PROPERTY_NOTIFY)
                            Handler(Looper.getMainLooper()).post {
                                it.callBackFailure.invoke(Throwable(gatStatus.toString()))
                            }
                        handleRemainingQueue()
                    }
                }
            }
        }

        private fun handleRemainingQueue() {
            if (context == null) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            bleCallsTrack.get().firstOrNull()?.let {
                when (it.propertyType) {
                    BluetoothGattCharacteristic.PROPERTY_WRITE -> {
                        it.isRequestSent =true
                        it.characteristicUuid.value = it.inputBytes
                        gattServer?.writeCharacteristic(it.characteristicUuid)
                    }
                    BluetoothGattCharacteristic.PROPERTY_READ -> {
                        it.isRequestSent =true
                        gattServer?.readCharacteristic(it.characteristicUuid)
                    }
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY -> {
                        it.isRequestSent = true
                        it.notifyCallBack?.let { enabled ->
                            gattServer?.setCharacteristicNotification(it.characteristicUuid, enabled.isNotifyEnabled)
                            val descriptor = it.characteristicUuid.getDescriptor(it.characteristicUuid.uuid)
                            descriptor.value = if (enabled.isNotifyEnabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                            gattServer?.writeDescriptor(descriptor)
                        }
                    }
                    else -> {
                    }
                }
            }
        }


    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action: String? = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val state: Int = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                when (state) {
                    BluetoothDevice.BOND_BONDING -> {
                    }
                    BluetoothDevice.BOND_BONDED ->                     // Bonded...
                        context?.unregisterReceiver(this)
                    BluetoothDevice.BOND_NONE -> {
                    }
                }
            }
        }
    }


    private var retryCount = AtomicInteger(0)
    private var retryReadCount = AtomicInteger(0)


    fun triggerDisconnect() = run {
        if (connectionStatus == BluetoothProfile.STATE_CONNECTED) {
//            getCharacteristics().find { it.uuid == characteristicUuid }?.let { characteristic ->
//                writeCharacteristic("d:d".toByteArray(), characteristic, {
//                    Log.i("test", "exit called ${characteristic.uuid}")
////                    releaseResources()
//                }, {
//                    Log.i("test", "exit called with error ${it}")
//                    try {
////                        releaseResources()
//                    } catch (e: Exception) {
//                    }
//                }, {
//                    Log.i("test", "Write success now disconnecting ${it}")
//                    connectionStateCallback?.invoke(BluetoothProfile.STATE_DISCONNECTED)
//                    connectionStatus = BluetoothProfile.STATE_DISCONNECTED
//                    Handler(Looper.getMainLooper()).post { releaseResources() }
//                })
//            }
        }
    }

    private fun releaseResources() {
        Log.i("test", "Release called")
        if (context == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        gattServer?.close()
        bleDevice = null
        connectionStatus = null

        retryCount.set(0)

        bleCallsTrack.get()?.clear()
        characteristicChanged.get().clear()

        Handler(Looper.getMainLooper()).post {
            connectionStateCallback?.invoke(BluetoothProfile.STATE_DISCONNECTED)
            connectionStateCallback = null
        }
        connectionErrorR = null
        servicesDiscovered = null
    }

    var blueToothManager:BluetoothManager?=null
    var bluetoothAdapter: BluetoothAdapter? =null

    fun connectToDevice(
        context: Context,
        macAddress: String?,
        connectionError: (Throwable) -> Unit,
        servicesDiscovered: () -> Unit,
        connectionStateCallback: (Int) -> Unit
    ) {

        blueToothManager = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
        if (blueToothManager ==null){
            Toast.makeText(context , "Feature not supported" ,Toast.LENGTH_SHORT).show()
            return
        }
        bluetoothAdapter = blueToothManager?.adapter
        if (bluetoothAdapter?.isEnabled ==false){
            Toast.makeText(context , "Please check Bluetooth settings" ,Toast.LENGTH_SHORT).show()
            return
        }
        var device = try {
            bluetoothAdapter?.getRemoteDevice(macAddress)
        } catch (e: Exception) {
            Toast.makeText(context , "Invalid Device" ,Toast.LENGTH_SHORT).show()
            return
        }



        retryCount.set(0)
        bleCallsTrack.get()?.clear()
        characteristicChanged.get().clear()
        allCharacteristics.clear()
        bleDevice = null
        bleDevice = device

        connectionErrorR = connectionError
        this.connectionStateCallback = connectionStateCallback
        this.servicesDiscovered = servicesDiscovered

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            gattServer = bleDevice?.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            gattServer = bleDevice?.connectGatt(context, false, bluetoothGattCallback)
        }

//        gattServer = bleDevice?.connectGatt(context, false, bluetoothGattCallback )

    }

    private var tryAgainDisposable: Handler? = null

    @SuppressLint("MissingPermission")
    private var runnable = Runnable {
        retryCount.incrementAndGet()
//        Log.i("test", "reconnecting ... ")
        Handler(Looper.getMainLooper()).post {
            if (context == null) return@post
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return@post
            }
            gattServer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bleDevice?.connectGatt(context!!, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                bleDevice?.connectGatt(context!!, false, bluetoothGattCallback)
            }
        }
    }

    @Synchronized
    private fun tryAgain() {
        if (retryCount.get() <= 2) {
            tryAgainDisposable?.removeCallbacks(runnable)
            tryAgainDisposable?.postDelayed(runnable, 1000)
        } else {
//            retryCount.set(0)
        }
    }

    fun onNotifyCharacteristic(
        characteristicUuid: BluetoothGattCharacteristic,
        callBackFailure: (Throwable) -> Unit, enabled: Boolean,
        notificationRegisteredSuccess:(BluetoothGattDescriptor?) -> Unit,
        notificationChangeCallback:(BluetoothGattCharacteristic?) -> Unit,
    ) {

        if (connectionStatus == BluetoothProfile.STATE_CONNECTED) {
            if (characteristicUuid.hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                bleCallsTrack.get().add(BleCalls(null, characteristicUuid, null, callBackFailure, BluetoothGattCharacteristic.PROPERTY_NOTIFY, notifyCallBack = NotifyCallBack( enabled,notificationRegisteredSuccess,notificationChangeCallback)))
                val isFound = bleCallsTrack.get().find { it.propertyType == BluetoothGattCharacteristic.PROPERTY_NOTIFY && it.isRequestSent }
                if (isFound == null) {
                    bleCallsTrack.get().find { it.propertyType == BluetoothGattCharacteristic.PROPERTY_WRITE && !it.isRequestSent }?.let {
                        if (context == null) return
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return
                        }
                        it.isRequestSent = true
                        gattServer?.setCharacteristicNotification(characteristicUuid, enabled)
                        val descriptor = characteristicUuid.getDescriptor(characteristicUuid.uuid)
                        descriptor.value = if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                        gattServer?.writeDescriptor(descriptor)
                    }
                }
            } else {
                callBackFailure.invoke(Throwable("Not writable property"))
                connectionErrorR?.invoke(Throwable("Not writable property"))
            }
        } else {
            callBackFailure.invoke(Throwable("Device Not Connected"))
            connectionErrorR?.invoke(Throwable("Device Not Connected"))
        }
    }


    fun readCharacteristic(characteristicUuid: BluetoothGattCharacteristic, callBackSuccess: (ByteArray) -> Unit, callBackFailure: (Throwable) -> Unit) {
        if (connectionStatus == BluetoothProfile.STATE_CONNECTED) {
            if (characteristicUuid.hasProperty(BluetoothGattCharacteristic.PROPERTY_READ)) {
                bleCallsTrack.get().add(BleCalls(null, characteristicUuid, callBackSuccess, callBackFailure, BluetoothGattCharacteristic.PROPERTY_READ))
                val isFound = bleCallsTrack.get().find { it.propertyType == BluetoothGattCharacteristic.PROPERTY_READ && it.isRequestSent }
                if (isFound == null) {
                    bleCallsTrack.get().find { it.propertyType == BluetoothGattCharacteristic.PROPERTY_READ && !it.isRequestSent }?.let {
                        it.isRequestSent = true
                        if (context == null) return
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return
                        }
                        gattServer?.readCharacteristic(characteristicUuid)
                    }
                }
            } else {
                callBackFailure.invoke(Throwable("Not readable property"))
                connectionErrorR?.invoke(Throwable("Not readable property"))
            }
        } else {
            callBackFailure.invoke(Throwable("Device Not Connected"))
            connectionErrorR?.invoke(Throwable("Device Not Connected"))
        }
    }

    private var bleCallsTrack: AtomicReference<MutableList<BleCalls>> = AtomicReference(arrayListOf())

    data class BleCalls(
        val inputBytes: ByteArray?,
        val characteristicUuid: BluetoothGattCharacteristic,
        val callBackSuccess: ((ByteArray) -> Unit)?,
        val callBackFailure: (Throwable) -> Unit?,
        val propertyType: Int,
        var isRequestSent: Boolean = false,
        var notifyCallBack: NotifyCallBack? = null
    )

    data class NotifyCallBack(
        var isNotifyEnabled: Boolean,
        var notificationRegisteredSuccess:(BluetoothGattDescriptor?) -> Unit,
        var notificationChangeCallback:(BluetoothGattCharacteristic?) -> Unit
    )


    fun writeCharacteristic(
        inputBytes: ByteArray,
        characteristicUuid: BluetoothGattCharacteristic,
        callBackSuccess: (ByteArray) -> Unit,
        callBackFailure: (Throwable) -> Unit,
        isWriteSucess: (Boolean) -> Unit
    ) {
        if (connectionStatus == BluetoothProfile.STATE_CONNECTED) {
//            this.callBackSuccess.add(Triple(callBackSuccess, characteristicUuid, inputBytes))
//            this.callBackFailure.add(Triple(callBackFailure, characteristicUuid, inputBytes))

            if (characteristicUuid.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)) {
                bleCallsTrack.get().add(BleCalls(inputBytes, characteristicUuid, callBackSuccess, callBackFailure, BluetoothGattCharacteristic.PROPERTY_WRITE))
                val isFound = bleCallsTrack.get().find { it.propertyType == BluetoothGattCharacteristic.PROPERTY_WRITE && it.isRequestSent }
                if (isFound == null) {
                    bleCallsTrack.get().find { it.propertyType == BluetoothGattCharacteristic.PROPERTY_WRITE && !it.isRequestSent }?.let {
                        if (context == null) return
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return
                        }
                        it.isRequestSent = true
                        characteristicUuid.value = inputBytes
                        isWriteSucess.invoke(gattServer?.writeCharacteristic(characteristicUuid) ?: false)
                    }
                }


//                characteristicUuid.value = inputBytes
//                gattServer?.writeCharacteristic(characteristicUuid)
            } else {
                callBackFailure.invoke(Throwable("Not writable property"))
                connectionErrorR?.invoke(Throwable("Not writable property"))
//                this.callBackFailure.find { it.first == callBackFailure }
//                    ?.let {
//                        connectionErrorR?.invoke(Throwable("Not writable property"))
//                        this.callBackFailure.remove(it)
//                    }
            }
        } else {
            callBackFailure.invoke(Throwable("Device Not Connected"))
            connectionErrorR?.invoke(Throwable("Device Not Connected"))
//            this.callBackFailure.find { it.first == callBackFailure }
//                ?.let { this.callBackFailure.remove(it) }
        }
    }

    fun getCharacteristics(): List<BluetoothGattCharacteristic> {
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

    fun isDeviceConnected(): Boolean {
        return connectionStatus == BluetoothProfile.STATE_CONNECTED
    }

}