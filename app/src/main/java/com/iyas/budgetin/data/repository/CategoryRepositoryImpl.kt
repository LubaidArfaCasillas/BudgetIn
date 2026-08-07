package com.iyas.budgetin.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iyas.budgetin.data.model.Category
import com.iyas.budgetin.domain.repository.CategoryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    private val userId: String?
        get() = auth.currentUser?.uid

    private fun getCategoriesCollection() = userId?.let { uid ->
        firestore.collection("users").document(uid).collection("categories")
    }

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val collection = getCategoriesCollection()
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = collection
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Jangan crash jika error karena user dihapus/logout
                    // Cukup kirim list kosong dan abaikan error permission
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull {
                        try {
                            it.toObject(Category::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(categories)
                }
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun addCategory(category: Category): Result<Unit> {
        val collection = getCategoriesCollection() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val docRef = collection.document()
            val newCategory = category.copy(id = docRef.id)
            docRef.set(newCategory).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        val collection = getCategoriesCollection() ?: return Result.failure(Exception("User not logged in"))

        return try {
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
