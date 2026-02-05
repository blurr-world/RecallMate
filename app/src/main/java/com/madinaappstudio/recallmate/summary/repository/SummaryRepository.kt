package com.madinaappstudio.recallmate.summary.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.madinaappstudio.recallmate.core.models.SummaryModel
import com.madinaappstudio.recallmate.core.utils.SUMMARIES_COLLECTION
import com.madinaappstudio.recallmate.core.utils.USERS_COLLECTION

class SummaryRepository() {
    private val firestore = FirebaseFirestore.getInstance()

    fun fetchSummary(
        userId: String,
        summaryId: String,
        onResult: (Result<SummaryModel>) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION).document(userId)
            .collection(SUMMARIES_COLLECTION).document(summaryId).get()
            .addOnSuccessListener { doc ->
                val summary = doc.toObject(SummaryModel::class.java)
                if (summary != null) {
                    onResult(Result.success(summary))
                } else {
                    onResult(Result.failure(Exception("Summary not found")))
                }
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun fetchAllSummary(
        userId: String,
        onResult: (Result<List<SummaryModel>>) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION).document(userId)
            .collection(SUMMARIES_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshots ->
                val summaryList = snapshots.toObjects(SummaryModel::class.java)
                onResult(Result.success(summaryList))
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun setSummary(
        userId: String,
        summaryModel: SummaryModel,
        onResult: (Result<Unit>) -> Unit
    ) {
        val docRef = firestore.collection(USERS_COLLECTION).document(userId)
            .collection(SUMMARIES_COLLECTION).document()
        summaryModel.id = docRef.id
        docRef.set(summaryModel)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun deleteSummary(
        userId: String,
        summaryId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val docRef = firestore.collection(USERS_COLLECTION).document(userId)
            .collection(SUMMARIES_COLLECTION).document(summaryId)

        docRef.delete()
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }
}