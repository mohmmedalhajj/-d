package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val phone: String = "",
    val governorate: String = "",
    val qatType: String = "",
    val notes: String = "",
    val createdDate: String = "",
    val lastUpdateDate: String = ""
)

@Entity(tableName = "supplier_purchases")
data class SupplierPurchase(
    @PrimaryKey val id: String = System.currentTimeMillis().toString(),
    val supplierId: String,
    val supplierName: String,
    val purchaseDate: String,
    val totalAmount: Double = 0.0,
    val notes: String = "",
    val itemsJson: String = "", // JSON array of PurchaseItem
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "supplier_payments")
data class SupplierPayment(
    @PrimaryKey val id: String = System.currentTimeMillis().toString(),
    val supplierId: String,
    val supplierName: String,
    val amount: Double,
    val paymentDate: String,
    val notes: String = "",
    val createdTime: Long = System.currentTimeMillis()
)

data class PurchaseItem(
    val name: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double,
    val notes: String = ""
)

data class SupplierStatistics(
    val supplierId: String,
    val supplierName: String,
    val totalPurchases: Double = 0.0,
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val purchaseCount: Int = 0,
    val paymentCount: Int = 0,
    val lastTransactionDate: String = ""
)

data class SupplierDetail(
    val supplier: Supplier,
    val statistics: SupplierStatistics,
    val purchases: List<SupplierPurchase> = emptyList(),
    val payments: List<SupplierPayment> = emptyList()
)

@Dao
interface SupplierDao {
    @Insert
    suspend fun insertSupplier(supplier: Supplier): Long

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("SELECT * FROM suppliers")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: String): Supplier?

    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :searchTerm || '%'")
    fun searchSuppliers(searchTerm: String): Flow<List<Supplier>>

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: String)
}

@Dao
interface SupplierPurchaseDao {
    @Insert
    suspend fun insertPurchase(purchase: SupplierPurchase): Long

    @Update
    suspend fun updatePurchase(purchase: SupplierPurchase)

    @Delete
    suspend fun deletePurchase(purchase: SupplierPurchase)

    @Query("SELECT * FROM supplier_purchases WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    fun getPurchasesBySupplier(supplierId: String): Flow<List<SupplierPurchase>>

    @Query("SELECT * FROM supplier_purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<SupplierPurchase>>

    @Query("SELECT * FROM supplier_purchases WHERE id = :id")
    suspend fun getPurchaseById(id: String): SupplierPurchase?

    @Query("DELETE FROM supplier_purchases WHERE id = :id")
    suspend fun deletePurchaseById(id: String)

    @Query("SELECT SUM(totalAmount) FROM supplier_purchases WHERE supplierId = :supplierId")
    suspend fun getTotalPurchasesAmount(supplierId: String): Double?
}

@Dao
interface SupplierPaymentDao {
    @Insert
    suspend fun insertPayment(payment: SupplierPayment): Long

    @Update
    suspend fun updatePayment(payment: SupplierPayment)

    @Delete
    suspend fun deletePayment(payment: SupplierPayment)

    @Query("SELECT * FROM supplier_payments WHERE supplierId = :supplierId ORDER BY paymentDate DESC")
    fun getPaymentsBySupplier(supplierId: String): Flow<List<SupplierPayment>>

    @Query("SELECT * FROM supplier_payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<SupplierPayment>>

    @Query("SELECT * FROM supplier_payments WHERE id = :id")
    suspend fun getPaymentById(id: String): SupplierPayment?

    @Query("DELETE FROM supplier_payments WHERE id = :id")
    suspend fun deletePaymentById(id: String)

    @Query("SELECT SUM(amount) FROM supplier_payments WHERE supplierId = :supplierId")
    suspend fun getTotalPaymentsAmount(supplierId: String): Double?
}
