package me.moty.cylost

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng

data class Record(
    val id: String,
    val owner: Int,
    val content: String,
    val loc: String,
    val date: String,
    val type: RecordType,
    val pin: LatLng?,
    val image1: Bitmap?,
    val image2: Bitmap?,
    val image3: Bitmap?
) {
}

enum class RecordType(val display: String) {
    KEYS("鑰匙"),
    CASH("現金"),
    STUID("學生證"),
    WALLET("皮夾"),
    HEADPHONE("耳機"),
    UMBRELLA("雨傘"),
    CLOTHS("衣物"),
    GLASSES("眼鏡"),
    ID("身分證件"),
    BOTTLE("水壺"),
    OTHERS("其他")
}