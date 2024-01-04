package me.moty.cylost.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.moty.cylost.MainActivity
import me.moty.cylost.Record
import me.moty.cylost.RecordType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.ByteArrayOutputStream
import java.io.IOException

class HomeViewModel() : ViewModel() {
    //    private val _records = MutableLiveData<HashMap<String, Record>>(hashMapOf())
//    val records: LiveData<HashMap<String, Record>> = _records

    private val client = OkHttpClient()

    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        return stream.toByteArray()
    }


    suspend fun fetchData(callback: (Int) -> Unit) = withContext(Dispatchers.IO) {
        // 請求並解析數據
        val req = Request.Builder()
            .url("https://itouch.cycu.edu.tw/active_project/cycu2100h_18/case_11/pick_list.jsp")
            .build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OkHttpClient", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) return
                if (response.body == null) return
                val html = response.body?.string()
                val doc: Document =
                    Jsoup.parse(html)
                val ele = doc.select("tbody").first()
                var data = 0
                ele.select("tr").forEach {
                    var id = ""
                    var type = ""
                    var date = ""
                    var loc = ""
                    var content = ""
                    it.select("td").forEach td@{ it2 ->
                        if (id.isEmpty()) {
                            id = it2.text()
                            return@td
                        }
                        if (type.isEmpty()) {
                            type = it2.text()
                            return@td
                        }
                        if (content.isEmpty()) {
                            content = it2.text()
                            return@td
                        }
                        if (loc.isEmpty()) {
                            loc = it2.text()
                            return@td
                        }
                        if (date.isEmpty()) {
                            date = it2.text()
                            return@td
                        }
                    }
                    if (date.isEmpty())
                        return@forEach
                    if (data % 5 == 0)
                        Thread.sleep(600)
                    data++
                    val bitmap = retrievePhoto(id)
                    val r =
                        Record(
                            id,
                            0,
                            content,
                            loc,
                            date,
                            RecordType.OTHERS,
                            null,
                            bitmap, null, null
                        )
                    insert(r)
                }
                callback(data)
            }
        })
    }

    fun insert(r: Record) {
        val statement = MainActivity.appContext.getDatebase().compileStatement(
            "INSERT INTO Record (id, owner, content, loc, date, type, pin, image1, image2, image3) VALUES " +
                    "(?,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET content = ?, loc = ?, type = ?" + (if (r.pin == null) "" else ", pin = ?") +
                    (if (r.image1 != null) ", image1 = ?" else "") + (if (r.image2 != null) ", image2 = ?" else "") + (if (r.image3 != null) ", image3 = ?" else "") + ";"
        )
        var index = 1
        statement.bindLong(index++, r.id.toLong())
        statement.bindLong(index++, r.owner.toLong())
        statement.bindString(index++, r.content)
        statement.bindString(index++, r.loc)
        statement.bindString(index++, r.date)
        statement.bindString(index++, r.type.name)
        if (r.pin != null)
            statement.bindString(
                index++,
                r.pin.latitude.toString() + ";" + r.pin.longitude.toString()
            )
        else
            statement.bindNull(index++)
        if (r.image1 != null)
            statement.bindBlob(index++, bitmapToBytes(r.image1))
        else
            statement.bindNull(index++)
        if (r.image2 != null)
            statement.bindBlob(index++, bitmapToBytes(r.image2))
        else
            statement.bindNull(index++)
        if (r.image3 != null)
            statement.bindBlob(index++, bitmapToBytes(r.image3))
        else
            statement.bindNull(index++)
        statement.bindString(index++, r.content)
        statement.bindString(index++, r.loc)
        statement.bindString(index++, r.type.name)
        if (r.pin != null)
            statement.bindString(
                index++,
                r.pin.latitude.toString() + ";" + r.pin.longitude.toString()
            )
        if (r.image1 != null)
            statement.bindBlob(index++, bitmapToBytes(r.image1))
        if (r.image2 != null)
            statement.bindBlob(index++, bitmapToBytes(r.image2))
        if (r.image3 != null)
            statement.bindBlob(index, bitmapToBytes(r.image3))
        statement.executeInsert()
    }

    fun retrievePhoto(id: String): Bitmap? {
        val res = client.newCall(
            Request.Builder().header("Accept", "APPLICATION/OCTET-STREAM")
                .url("https://itouch.cycu.edu.tw/active_project/cycu2100h_18/case_11/show.jsp?id=${id}")
                .build()
        ).execute()
        if (res.code != 200) return null
        if (res.body == null) return null
        val byte = res.body!!.bytes()
        return (
                if (res.headers["Content-Type"] == "image/jpeg") null
                else BitmapFactory.decodeByteArray(
                    byte,
                    0,
                    byte.size
                ))
    }
}