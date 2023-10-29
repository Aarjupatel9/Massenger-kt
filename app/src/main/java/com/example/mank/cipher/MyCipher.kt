package com.example.mank.cipher

import android.util.Log

class MyCipher {
    val key = "bnkama91211"
    val kl = key.length
    fun encrypt(text1: Int): String {
        val text = text1.toString()
        val res = StringBuilder()
        var i = 0
        var j = 0
        while (i < text.length) {
            val c = text[i]
            if (c == ' ') {
                res.append("-01")
            } else {
                val x = c.code + key[j].code
                if (x > 99) {
                    res.append(x)
                } else if (x > 9) {
                    res.append("0$x")
                } else if (x > -1) {
                    res.append("00$x")
                } else {
                    Log.d("log-encryption-error", "encrypt: error in encryption")
                }
            }
            j = ++j % kl
            i++
        }
        return res.toString()
    }

    fun encrypt(text1: Float): String {
        val text = text1.toString()
        val res = StringBuilder()
        var i = 0
        var j = 0
        while (i < text.length) {
            val c = text[i]
            if (c == ' ') {
                res.append(" ")
            } else {
                res.append((c.code + key[j].code).toChar())
            }
            j = ++j % kl
            i++
        }
        return res.toString()
    }

    fun encrypt(text: String): String {
        val res = StringBuilder()
        var i = 0
        var j = 0
        while (i < text.length) {
            val c = text[i]
            if (c == ' ') {
                res.append("-01")
            } else {
                val x = c.code + key[j].code
                if (x > 99) {
                    res.append(x)
                } else if (x > 9) {
                    res.append("0$x")
                } else if (x > -1) {
                    res.append("00$x")
                } else {
                    Log.d("log-encryption-error", "encrypt: error in encryption")
                }
            }
            j = ++j % kl
            i++
        }
        return res.toString()
    }

    fun decrypt(text: String): String {
        val res = StringBuilder()
        var tmp = ""
        // text = text.toUpperCase();
        var i = 0
        var j = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '-') {
                res.append(" ")
            } else {
                tmp = "" + text[i] + text[i + 1] + text[i + 2]
                val x: Int = tmp.toInt() - key[j].code
                res.append(((x + 255) % 255).toChar())
            }
            i += 2
            j = ++j % kl
            i++
        }
        return res.toString()
    }
}