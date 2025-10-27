package com.example.surtefacilsv

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(context: Context) {

    private val localDb = DatabaseHelper(context)
    private val firestore = FirebaseFirestore.getInstance()

    fun registerUser(fullName: String, email: String, password: String): Boolean {
        // Guardar local
        val savedLocal = localDb.addUser(fullName, email, password)

        // Si se guardó, Lo va a subir a Firestore
        if (savedLocal) {
            val userData = hashMapOf(
                "full_name" to fullName,
                "email" to email,
                "password" to password,
                "created_at" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .add(userData)
                .addOnSuccessListener { doc ->
                    Log.d("FIRESTORE", "Usuario subido a Firestore con ID: ${doc.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("FIRESTORE", "Error al subir a Firestore", e)
                }
        }

        return savedLocal
    }
}
