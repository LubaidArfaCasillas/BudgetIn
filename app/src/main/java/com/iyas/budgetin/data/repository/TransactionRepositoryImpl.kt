package com.iyas.budgetin.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.domain.repository.TransactionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TransactionRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    private val userId: String?
        get() = auth.currentUser?.uid

    private fun getTransactionsCollection() = userId?.let { uid ->
        firestore.collection("users").document(uid).collection("transactions")
    }

    override fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        val collection = getTransactionsCollection()
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = collection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Jangan crash jika error karena user dihapus/logout
                    // Cukup kirim list kosong dan abaikan error permission
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val transactions = snapshot.documents.mapNotNull { 
                        try {
                            it.toObject(Transaction::class.java) 
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(transactions)
                }
            }
            
        awaitClose { subscription.remove() }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        val collection = getTransactionsCollection() ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            val docRef = collection.document()
            val newTransaction = transaction.copy(id = docRef.id)
            docRef.set(newTransaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        val collection = getTransactionsCollection() ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            collection.document(transaction.id).set(transaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        val collection = getTransactionsCollection() ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getBalance(): Flow<Double> {
        return getTransactions().map { transactions ->
            transactions.sumOf { 
                if (it.type == com.iyas.budgetin.data.model.TransactionType.INCOME) it.amount else -it.amount 
            }
        }
    }
}
