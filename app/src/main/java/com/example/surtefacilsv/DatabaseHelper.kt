package com.example.surtefacilsv

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SurteFacilSV.db"
        private const val DATABASE_VERSION = 2 // Incrementar la versión de la base de datos

        // Tabla de lo Usuarios
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_FULL_NAME = "full_name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_USER_TYPE = "user_type"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_TYPE TEXT NOT NULL, 
                $COLUMN_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_TYPE TEXT NOT NULL DEFAULT 'Soy un comprador'")
        }
    }

    // Con esto se esta Agregando los Usaurios
    fun addUser(fullName: String, email: String, password: String, userType: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password) // En producción, esto debería estar encriptado
            put(COLUMN_USER_TYPE, userType)
        }

        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    // Valida si el usuario existe o no
    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Verificar si email ya existe o No
    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Obtener usuario por email
    fun getUserByEmail(email: String): String? {
        val db = readableDatabase
        val query = "SELECT $COLUMN_FULL_NAME FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))

        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME))
        } else {
            null
        }.also { cursor.close() }
    }

    // En el DatabaseHelper.kt - Actualizan los datos de un usuario en la base local SQLite, con o sin cambiar la contraseña según el metodo usado

    fun updateUser(oldEmail: String, newFullName: String, newEmail: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, newFullName)
            put(COLUMN_EMAIL, newEmail)
            put(COLUMN_PASSWORD, newPassword)
        }

        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(oldEmail))
        return result > 0
    }

    fun updateUserWithoutPassword(oldEmail: String, newFullName: String, newEmail: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, newFullName)
            put(COLUMN_EMAIL, newEmail)
        }

        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(oldEmail))
        return result > 0
    }
}