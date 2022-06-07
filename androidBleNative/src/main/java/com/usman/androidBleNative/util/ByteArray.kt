package com.usman.androidBleNative.util

fun ByteArray.toHex() = joinToString("") { String.format("%02X", (it.toInt() and 0xff)) }

fun String.toHex(): StringBuilder {
    val stringBuilder = StringBuilder()
    val charArray = this.toCharArray()
    for (c in charArray) {
        val charToHex = Integer.toHexString(c.code)
        stringBuilder.append(charToHex)
    }
    return stringBuilder
}

fun String.hexToByteArray(): ByteArray {
    var hex = this
    hex = if (hex.length % 2 != 0) "0$hex" else hex
    val b = ByteArray(hex.length / 2)
    for (i in b.indices) {
        val index = i * 2
        val v = Integer.parseInt(hex.substring(index, index + 2), 16)
        b[i] = v.toByte()
    }
    return b
}