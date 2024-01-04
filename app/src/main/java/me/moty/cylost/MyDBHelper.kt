package me.moty.cylost

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDBHelper(
    context: Context,
    name: String = database,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = v
) : SQLiteOpenHelper(context, name, factory, version) {


    companion object {
        private const val database = "CYLost"
        private const val v = 2
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE Record(id INTEGER PRIMARY KEY, owner INTEGER NOT NULL, content VARCHAR(20) NOT NULL, loc VARCHAR(10) NOT NULL, date TEXT NOT NULL, type VARCHAR(10) NOT NULL,  pin TEXT, image1 BLOB, image2 BLOB, image3 BLOB)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Record")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Record")
        onCreate(db)
    }




}