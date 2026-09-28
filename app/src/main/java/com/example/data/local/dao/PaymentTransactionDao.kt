package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PaymentTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentTransactionDao {
    @Query("SELECT * FROM payment_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE transactionNumber = :txnNo LIMIT 1")
    suspend fun getTransactionByNumber(txnNo: String): PaymentTransactionEntity?

    @Query("SELECT * FROM payment_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): PaymentTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaymentTransactionEntity): Long

    @Query("SELECT COUNT(*) FROM payment_transactions")
    fun getTransactionCount(): Flow<Int>

    @Query("DELETE FROM payment_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
}
