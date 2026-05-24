package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SupplierRepository(
    private val supplierDao: SupplierDao,
    private val purchaseDao: SupplierPurchaseDao,
    private val paymentDao: SupplierPaymentDao
) {
    private val gson = Gson()

    fun getAllSuppliers(): Flow<List<Supplier>> = supplierDao.getAllSuppliers()

    fun searchSuppliers(searchTerm: String): Flow<List<Supplier>> = supplierDao.searchSuppliers(searchTerm)

    suspend fun addSupplier(supplier: Supplier) {
        supplierDao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(supplier: Supplier) {
        supplierDao.updateSupplier(supplier)
    }

    suspend fun deleteSupplier(supplier: Supplier) {
        supplierDao.deleteSupplier(supplier)
    }

    suspend fun getSupplierById(id: String): Supplier? = supplierDao.getSupplierById(id)

    suspend fun addPurchase(purchase: SupplierPurchase) {
        purchaseDao.insertPurchase(purchase)
    }

    suspend fun updatePurchase(purchase: SupplierPurchase) {
        purchaseDao.updatePurchase(purchase)
    }

    suspend fun deletePurchase(purchase: SupplierPurchase) {
        purchaseDao.deletePurchase(purchase)
    }

    fun getPurchasesBySupplier(supplierId: String): Flow<List<SupplierPurchase>> =
        purchaseDao.getPurchasesBySupplier(supplierId)

    fun getAllPurchases(): Flow<List<SupplierPurchase>> = purchaseDao.getAllPurchases()

    suspend fun getPurchaseById(id: String): SupplierPurchase? = purchaseDao.getPurchaseById(id)

    suspend fun addPayment(payment: SupplierPayment) {
        paymentDao.insertPayment(payment)
    }

    suspend fun updatePayment(payment: SupplierPayment) {
        paymentDao.updatePayment(payment)
    }

    suspend fun deletePayment(payment: SupplierPayment) {
        paymentDao.deletePayment(payment)
    }

    fun getPaymentsBySupplier(supplierId: String): Flow<List<SupplierPayment>> =
        paymentDao.getPaymentsBySupplier(supplierId)

    fun getAllPayments(): Flow<List<SupplierPayment>> = paymentDao.getAllPayments()

    suspend fun getPaymentById(id: String): SupplierPayment? = paymentDao.getPaymentById(id)

    suspend fun getSupplierStatistics(supplierId: String): SupplierStatistics? {
        val supplier = getSupplierById(supplierId) ?: return null
        val totalPurchases = purchaseDao.getTotalPurchasesAmount(supplierId) ?: 0.0
        val totalPaid = paymentDao.getTotalPaymentsAmount(supplierId) ?: 0.0

        return SupplierStatistics(
            supplierId = supplierId,
            supplierName = supplier.name,
            totalPurchases = totalPurchases,
            totalPaid = totalPaid,
            remainingBalance = totalPurchases - totalPaid,
            purchaseCount = 0,
            paymentCount = 0,
            lastTransactionDate = ""
        )
    }

    suspend fun getSupplierDetail(supplierId: String): SupplierDetail? {
        val supplier = getSupplierById(supplierId) ?: return null
        val statistics = getSupplierStatistics(supplierId) ?: return null
        val purchases = getPurchasesBySupplier(supplierId).map { it }.toList()
        val payments = getPaymentsBySupplier(supplierId).map { it }.toList()

        return SupplierDetail(
            supplier = supplier,
            statistics = statistics,
            purchases = purchases,
            payments = payments
        )
    }

    fun convertPurchaseItemsToJson(items: List<PurchaseItem>): String {
        return gson.toJson(items)
    }

    fun convertJsonToPurchaseItems(json: String): List<PurchaseItem> {
        return try {
            val type = object : TypeToken<List<PurchaseItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
