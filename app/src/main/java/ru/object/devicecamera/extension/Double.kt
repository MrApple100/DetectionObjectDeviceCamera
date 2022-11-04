package ru.`object`.devicecamera.extension

fun Double?.orZero(): Double {
    return this ?: 0.0
}