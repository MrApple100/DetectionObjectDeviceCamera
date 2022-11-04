package ru.`object`.devicecamera.extension

fun Int?.orZero(): Int {
    return this ?: 0
}