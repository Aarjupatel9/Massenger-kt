package com.example.mank.configuration

import android.Manifest

object permission_code {
    const val CAMERA_PERMISSION_CODE = 100
    const val INTERNET_PERMISSION_CODE = 102
    const val NETWORK_PERMISSION_CODE = 103
    const val STORAGE_PERMISSION_CODE = 104
    const val CONTACTS_PERMISSION_CODE = 105
    const val PERMISSION_ALL = 1
    const val PERMISSION_CONTACT_SYNC = 3
    const val PERMISSION_initContentResolver = 2
    val PERMISSIONS = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    )
    val STORAGE_PERMISSION = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val CONTACT_STORAGE_PERMISSION = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val NETWORK_PERMISSION = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE
    )
    val CONTACT_PERMISSION =
        arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
}