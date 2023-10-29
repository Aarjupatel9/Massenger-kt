package com.example.mank.functionalityClasses

import android.net.Uri

class MyImageClass(
    val uri: Uri,
    val name: String,
    val path: String,
    val type: String,
    private val id: Long
)