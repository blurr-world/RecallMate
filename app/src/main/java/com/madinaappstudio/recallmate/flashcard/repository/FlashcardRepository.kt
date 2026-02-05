package com.madinaappstudio.recallmate.flashcard.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.madinaappstudio.recallmate.core.models.FlashcardModel
import com.madinaappstudio.recallmate.core.utils.FLASHCARDS_COLLECTION
import com.madinaappstudio.recallmate.core.utils.FLASHCARD_GROUPS_COLLECTION
import com.madinaappstudio.recallmate.core.utils.USERS_COLLECTION
import com.madinaappstudio.recallmate.flashcard.model.FlashcardSetItem

class FlashcardRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun saveFlashcardSetWithCards(
        userId: String,
        flashcardSetTitle: String,
        flashcards: List<FlashcardModel>,
        onResult: (Result<Unit>) -> Unit
    ) {
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)

        val flashcardSetRef = userRef
            .collection(FLASHCARD_GROUPS_COLLECTION)
            .document()

        val flashcardSet = FlashcardSetItem(
            id = flashcardSetRef.id,
            title = flashcardSetTitle,
            createdAt = System.currentTimeMillis(),
            totalCards = flashcards.size.toString()
        )

        flashcardSetRef.set(flashcardSet)
            .addOnSuccessListener {

                val batch = firestore.batch()
                val flashcardsRef = userRef.collection(FLASHCARDS_COLLECTION)

                flashcards.forEach { card ->
                    val doc = flashcardsRef.document()
                    card.id = doc.id
                    card.groupId = flashcardSet.id
                    batch.set(doc, card)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onResult(Result.success(Unit))
                    }
                    .addOnFailureListener {
                        onResult(Result.failure(it))
                    }
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun fetchFlashcardSets(
        userId: String,
        onResult: (Result<List<FlashcardSetItem>>) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FLASHCARD_GROUPS_COLLECTION)
            .orderBy("createdAt",Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val groups = snapshot.toObjects(FlashcardSetItem::class.java)
                onResult(Result.success(groups))
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun fetchFlashcardsBySet(
        userId: String,
        flashcardSetId: String,
        onResult: (Result<List<FlashcardModel>>) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FLASHCARDS_COLLECTION)
            .whereEqualTo("groupId", flashcardSetId)
            .get()
            .addOnSuccessListener { snapshot ->
                val cards = snapshot.documents.map { doc ->
                    doc.toObject(FlashcardModel::class.java)!!.apply {
                        id = doc.id
                    }
                }
                onResult(Result.success(cards))
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun updateFlashcard(
        userId: String,
        flashcard: FlashcardModel,
        onResult: (Result<Unit>) -> Unit
    ) {
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)

        val flashcardSetRef = userRef.collection(FLASHCARDS_COLLECTION)
            .document(flashcard.id)

        flashcardSetRef.set(flashcard)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun deleteFlashcardSet(
        userId: String,
        setId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)

        userRef.collection(FLASHCARDS_COLLECTION)
            .whereEqualTo("groupId", setId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()

                snapshot.documents.forEach {
                    batch.delete(it.reference)
                }

                val groupRef = userRef
                    .collection(FLASHCARD_GROUPS_COLLECTION)
                    .document(setId)

                batch.delete(groupRef)

                batch.commit()
                    .addOnSuccessListener {
                        onResult(Result.success(Unit))
                    }
                    .addOnFailureListener {
                        onResult(Result.failure(it))
                    }
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }
}