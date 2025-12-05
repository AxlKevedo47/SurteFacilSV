package com.example.surtefacilsv

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val context: Context) {

    private val localDb = DatabaseHelper(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun registerUser(
        fullName: String,
        email: String,
        password: String,
        userType: String,
        businessName: String? = null,
        dui: String? = null,
        rubro: String? = null,
        address: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser == null) {
                        onResult(false, "No se pudo obtener el usuario de Firebase.")
                        return@addOnCompleteListener
                    }
                    val uid = firebaseUser.uid

                    val userData = hashMapOf<String, Any?>(
                        "uid" to uid,
                        "full_name" to fullName,
                        "email" to email,
                        "user_type" to userType,
                        "created_at" to System.currentTimeMillis(),
                        "business_name" to businessName,
                        "dui" to dui,
                        "rubro" to rubro,
                        "address" to address
                    ).filterValues { it != null }

                    firestore.collection("users").document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            // **CRITICAL FIX**: Check if the local save was successful
                            val wasLocalSaveSuccessful = localDb.addUser(fullName, email, password, userType, businessName, dui, rubro, address)
                            
                            if (wasLocalSaveSuccessful) {
                                // Only report overall success if BOTH cloud and local saves worked
                                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("user_uid", uid).apply()
                                onResult(true, null)
                            } else {
                                // The local save failed. This is the error that was being hidden.
                                val errorMessage = "El usuario se creó en la nube, pero falló al guardar en el dispositivo."
                                Log.e("UserRepository", errorMessage)
                                // For a production app, you might want to delete the user from Auth/Firestore to clean up.
                                onResult(false, errorMessage)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserRepository", "Error saving user to Firestore", e)
                            onResult(false, e.message)
                        }
                } else {
                    Log.e("UserRepository", "Firebase Auth creation failed", task.exception)
                    onResult(false, task.exception?.message)
                }
            }
    }
}
