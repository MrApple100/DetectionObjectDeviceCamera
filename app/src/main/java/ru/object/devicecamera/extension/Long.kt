package ru.`object`.devicecamera.extension

fun Long?.orZero(): Long {
    return this ?: 0L
}