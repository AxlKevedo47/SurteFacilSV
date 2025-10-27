package com.example.surtefacilsv

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseSyncHelper(private val context: Context) {

    private val dbFirebase = FirebaseFirestore.getInstance()
    private val dbLocal = DatabaseHelper(context)

    fun syncUsers() {
        if (!isInternetAvailable()) {
            Log.d("SYNC", "Sin conexión, se pospone sincronización.")
            return
        }

        val localUsers = getLocalUsers()
        if (localUsers.isEmpty()) {
            Log.d("SYNC", "No hay usuarios locales que sincronizar.")
            return
        }

        for (user in localUsers) {
            dbFirebase.collection("users")
                .add(user)
                .addOnSuccessListener {
                    Log.d("SYNC", "Usuario sincronizado: ${user["email"]}")
                }
                .addOnFailureListener { e ->
                    Log.e("SYNC", "Error al sincronizar usuario", e)
                }
        }
    }

    private fun getLocalUsers(): List<Map<String, Any>> {
        val users = mutableListOf<Map<String, Any>>()
        val db = dbLocal.readableDatabase
        val cursor = db.rawQuery("SELECT full_name, email, password, created_at FROM users", null)

        while (cursor.moveToNext()) {
            val user = mapOf(
                "full_name" to cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow("email")),
                "password" to cursor.getString(cursor.getColumnIndexOrThrow("password")),
                "created_at" to cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            )
            users.add(user)
        }
        cursor.close()
        return users
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}
