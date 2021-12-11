package com.ramalingam.localforecast.database;

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DataBaseHelper(val context: Context) {

    companion object {

        private const val DBName = "WeatherDB.db"

        //Table Related
        private const val CityTable = "CityTable"
        private const val CityName = "CityName"

        private const val OfflineDataTable = "OfflineDataTable"
        private const val OfflineData = "OfflineData"
    }

    fun getReadWriteDB(): SQLiteDatabase {

        val dbFile = context.getDatabasePath(DBName)
        if (!dbFile.exists()) {
            try {
                val checkDB = context.openOrCreateDatabase(DBName, Context.MODE_PRIVATE, null)
                checkDB?.close()
                copyDatabase(dbFile)
            } catch (e: IOException) {
                throw RuntimeException("Error creating source database", e)
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
    }


    @Throws(IOException::class)
    private fun copyDatabase(dbFile: File) {
        val inputStream = context.assets.open(DBName)
        val os = FileOutputStream(dbFile)

        val buffer = ByteArray(1024)
        while (inputStream.read(buffer) > 0) {
            os.write(buffer)
        }
        os.flush()
        os.close()
        inputStream.close()
    }


    fun getAllCityList(): ArrayList<String> {

        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        val cityArrayList = ArrayList<String>()
        try {
            db = getReadWriteDB()
            val query = "Select * From $CityTable Group By $CityName Order By $CityName"
            cursor = db.rawQuery(query, null)

            while (cursor != null && cursor.moveToNext()) {
                cityArrayList.add(cursor.getString(cursor.getColumnIndex(CityName)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
            if (db != null && db.isOpen) {
                db.close()
            }
        }
        return cityArrayList
    }

    fun getCityBySearch(newText: String): ArrayList<String> {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        val cityArrayList = ArrayList<String>()
        try {
            db = getReadWriteDB()
            val query = "Select * From $CityTable Where $CityName Like '$newText%'"
            cursor = db.rawQuery(query, null)

            while (cursor != null && cursor.moveToNext()) {
                cityArrayList.add(cursor.getString(cursor.getColumnIndex(CityName)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
            if (db != null && db.isOpen) {
                db.close()
            }
        }
        return cityArrayList
    }

    fun deleteInsertOfflineData(offlineData: String) {

        var db: SQLiteDatabase? = null
        try {
            db = getReadWriteDB()

            val contentValues = ContentValues()
            contentValues.put(OfflineData, offlineData)
            val lastId = db.insert(OfflineDataTable, null, contentValues)
            if (lastId.toInt() != -1) {
//                Log.e(CommonConstants.LogTag,"OfflineData Inserted")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (db != null && db.isOpen) {
                db.close()
            }
        }
    }

    fun getOfflineData(): String? {
        var offlineData: String? = null
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = getReadWriteDB()
            val query = "Select * From $OfflineDataTable"
            cursor = db.rawQuery(query, null)

            while (cursor != null && cursor.moveToNext()) {
                offlineData = cursor.getString(cursor.getColumnIndex(OfflineData))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
            if (db != null && db.isOpen) {
                db.close()
            }
        }
        return offlineData
    }
}