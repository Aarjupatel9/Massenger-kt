package com.example.mank.FunctionalityClasses

import android.net.Uri

class MyImageClass(
    val uri: Uri,
    val name: String,
    val path: String,
    val type: String,
    private val id: Long
)