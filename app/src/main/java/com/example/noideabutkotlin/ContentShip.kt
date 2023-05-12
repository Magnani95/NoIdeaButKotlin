package com.example.noideabutkotlin

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log


class ContentShip : ContentProvider() {
	companion object {
		val PROVIDER_NAME = "testing.cpex.ContentStudenti"
		val URL = "content://$PROVIDER_NAME/ships"
		val CONTENT_URI: Uri = Uri.parse(URL)

		val USER = "user"
		val POSITIONX = "positionX"
		val POSITIONY = "positionY"
		val SECTORX = "sectorX"
		val SECTORY = "sectorY"

	}

	private var db: SQLiteDatabase? = null
	var map :Map<String, Any>? = null
	val DATABASE_NAME = "saves"
	val TABLE_NAME = "ships"
	val DATABASE_VERSION = 1
	val CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME +
			" (user TEXT PRIMARY KEY, " +
			" sectorX TEXT NOT NULL," +
			" sectorY TEXT NOT NULL" +
			"positionX TEXT NOT NULL" +
			"positionY TEXT NOT NULL" +
			");"

	private var dbHelper: DBWrapper? = null
	private var qb: SQLiteQueryBuilder? = null


	override fun onCreate(): Boolean {
		val context = context
		dbHelper = DBWrapper(context)

		db = dbHelper!!.writableDatabase
		qb = SQLiteQueryBuilder()
		qb!!.projectionMap = map as MutableMap<String, String>?
		qb!!.tables = TABLE_NAME
		return db != null
	}

	override fun query(uri: Uri,
	                   projection: Array<out String>?,
	                   selection: String?,
	                   selectionArgs: Array<out String>?,
	                   sortOrder: String?): Cursor? {

		return qb!!.query(
				db, projection, selection,
				selectionArgs, null, null, sortOrder
		)
	}

	fun contaElementi(): Long {
		return DatabaseUtils.queryNumEntries(db, TABLE_NAME)
	}

	fun getRawDB(): SQLiteDatabase? {
		return db
	}
	fun debugContent() {
		val c = db!!.rawQuery("SELECT * FROM $TABLE_NAME", null)
		if (c != null) {
			c.moveToFirst()
			do {
				var s = ""
				for(i in 0 until c.columnCount) {
					s += " || " + c.getString(i)
				}
				Log.d("TAG", s)
			} while (c.moveToNext())
		}
	}

	override fun query(uri: Uri,
	                   projection: Array<out String>?,
	                   selection: String?,
	                   selectionArgs: Array<out String>?,
	                   sortOrder: String?,
	                   cancellationSignal: CancellationSignal?): Cursor? {
		return qb!!.query(
				db, projection, selection,
				selectionArgs, null, null, sortOrder
		)
	}
	override fun getType(uri: Uri): String? {
		return "testing.cpex.Studente"
	}
	override fun insert(uri: Uri, values: ContentValues?): Uri? {
		var rowID: Long = -1
		rowID = db!!.insert(TABLE_NAME, "", values)
		return if (rowID > 0) {
			ContentUris.withAppendedId(CONTENT_URI, rowID)
		} else {
			Log.d("TAG", "Errore. Entry già esistente?")
			null
		} //non siamo riusciti ad aggiungere la nostra nuova entry
	}

	override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
		//todo
		return -1
	}

	override fun delete(uri: Uri, extras: Bundle?): Int {
		var selection = extras?.get("selection")
		var selectionArgs = extras?.get("selectionArgs")
		return db!!.delete(TABLE_NAME, selection as String?, selectionArgs as Array<out String>?)
	}

	override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
		TODO("Not yet implemented")
	}

	override fun update(uri: Uri, values: ContentValues?, extras: Bundle?): Int {
		var selection = extras?.get("selection")
		var selectionArgs = extras?.get("selectionArgs")
		return db!!.update(TABLE_NAME, values, selection as String?,
				selectionArgs as Array<out String>?
		)
	}
}

class DBWrapper (context: Context?) : SQLiteOpenHelper(context, "ship", null, 1) {
	val DATABASE_NAME = "saves"


	override fun onCreate(db: SQLiteDatabase) {
		val CREATE_DB_TABLE = " CREATE TABLE " + "ships" +
				"(user TEXT PRIMARY KEY, " +
				" sectorX TEXT NOT NULL," +
				" sectorY TEXT NOT NULL," +
				"positionX TEXT NOT NULL," +
				"positionY TEXT NOT NULL" +
				");"
		db.execSQL(CREATE_DB_TABLE)
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		val TABLE_NAME = "ships"
		db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
		onCreate(db)
	}
}
