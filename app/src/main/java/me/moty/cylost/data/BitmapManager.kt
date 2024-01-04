package me.moty.cylost.data

import android.graphics.Bitmap
import android.util.LruCache
import me.moty.cylost.Record

class BitmapManager {

    private lateinit var memoryCache: LruCache<Record, Array<Bitmap>>

    init{
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<Record, Array<Bitmap>>(cacheSize) {

        }
    }
}