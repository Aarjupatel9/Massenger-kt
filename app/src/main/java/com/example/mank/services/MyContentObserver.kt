package com.example.mank.services

import android.database.ContentObserver
import android.os.Handler

class MyContentObserver(handler: Handler?) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        // handle the change in contacts
    }
}