package com.madinaappstudio.recallmate.auth.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.madinaappstudio.recallmate.core.models.UserModel
import com.madinaappstudio.recallmate.core.utils.USERS_COLLECTION

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchUser(
        userId: String, onResult: (Result<UserModel>) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION).document(userId).get().addOnSuccessListener { doc ->
                val user = doc.toObject(UserModel::class.java)
                if (user != null) {
                    onResult(Result.success(user))
                } else {
                    onResult(Result.failure(Exception("User not found")))
                }
            }.addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun setUser(
        userId: String, userModel: UserModel, onResult: (Result<Unit>) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION).document(userId).set(userModel, SetOptions.merge())
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }.addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun updateUser(
        userId: String, userModel: UserModel, onResult: (Result<Unit>) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION).document(userId).set(userModel, SetOptions.merge())
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }.addOnFailureListener {
                onResult(Result.failure(it))
            }
    }
}