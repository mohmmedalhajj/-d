package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Supplier
import com.example.data.SupplierPayment
import com.example.data.SupplierPurchase
import com.example.data.SupplierRepository
import com.example.data.SupplierStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SupplierViewModel(private val repository: SupplierRepository) : ViewModel() {

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _supplierStatistics = MutableStateFlow<Map<String, SupplierStatistics>>(emptyMap())
    val supplierStatistics: StateFlow<Map<String, SupplierStatistics>> = _supplierStatistics.asStateFlow()

    private val _selectedSupplier = MutableStateFlow<Supplier?>(null)
    val selectedSupplier: StateFlow<Supplier?> = _selectedSupplier.asStateFlow()

    private val _purchases = MutableStateFlow<List<SupplierPurchase>>(emptyList())
    val purchases: StateFlow<List<SupplierPurchase>> = _purchases.asStateFlow()

    private val _payments = MutableStateFlow<List<SupplierPayment>>(emptyList())
    val payments: StateFlow<List<SupplierPayment>> = _payments.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadSuppliers()
    }

    fun loadSuppliers() {
        viewModelScope.launch {
            repository.getAllSuppliers().collect { suppliersList ->
                _suppliers.value = suppliersList
                loadStatisticsForSuppliers(suppliersList)
            }
        }
    }

    private fun loadStatisticsForSuppliers(suppliersList: List<Supplier>) {
        viewModelScope.launch {
            val statsMap = mutableMapOf<String, SupplierStatistics>()
            suppliersList.forEach { supplier ->
                repository.getSupplierStatistics(supplier.id)?.let { stats ->
                    statsMap[supplier.id] = stats
                }
            }
            _supplierStatistics.value = statsMap
        }
    }

    fun selectSupplier(supplier: Supplier) {
        viewModelScope.launch {
            _selectedSupplier.value = supplier
            loadPurchasesForSupplier(supplier.id)
            loadPaymentsForSupplier(supplier.id)
        }
    }

    private fun loadPurchasesForSupplier(supplierId: String) {
        viewModelScope.launch {
            repository.getPurchasesBySupplier(supplierId).collect { purchasesList ->
                _purchases.value = purchasesList
            }
        }
    }

    private fun loadPaymentsForSupplier(supplierId: String) {
        viewModelScope.launch {
            repository.getPaymentsBySupplier(supplierId).collect { paymentsList ->
                _payments.value = paymentsList
            }
        }
    }

    fun addSupplier(
        name: String,
        phone: String,
        governorate: String,
        qatType: String,
        notes: String
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "اسم المورد مطلوب"
            return
        }

        val supplier = Supplier(
            name = name,
            phone = phone,
            governorate = governorate,
            qatType = qatType,
            notes = notes,
            createdDate = getCurrentDate(),
            lastUpdateDate = getCurrentDate()
        )

        viewModelScope.launch {
            try {
                repository.addSupplier(supplier)
                _errorMessage.value = null
                loadSuppliers()
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في إضافة المورد: ${e.message}"
            }
        }
    }

    fun updateSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                val updatedSupplier = supplier.copy(lastUpdateDate = getCurrentDate())
                repository.updateSupplier(updatedSupplier)
                _errorMessage.value = null
                loadSuppliers()
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في تحديث المورد: ${e.message}"
            }
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                repository.deleteSupplier(supplier)
                _errorMessage.value = null
                _selectedSupplier.value = null
                loadSuppliers()
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في حذف المورد: ${e.message}"
            }
        }
    }

    fun addPurchase(
        supplierId: String,
        supplierName: String,
        items: List<com.example.data.PurchaseItem>,
        notes: String
    ) {
        val totalAmount = items.sumOf { it.total }
        val purchase = SupplierPurchase(
            supplierId = supplierId,
            supplierName = supplierName,
            purchaseDate = getCurrentDate(),
            totalAmount = totalAmount,
            notes = notes,
            itemsJson = repository.convertPurchaseItemsToJson(items)
        )

        viewModelScope.launch {
            try {
                repository.addPurchase(purchase)
                _errorMessage.value = null
                loadPurchasesForSupplier(supplierId)
                loadStatisticsForSuppliers(listOf(_selectedSupplier.value ?: return@launch))
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في إضافة المشتريات: ${e.message}"
            }
        }
    }

    fun addPayment(
        supplierId: String,
        supplierName: String,
        amount: Double,
        notes: String
    ) {
        if (amount <= 0) {
            _errorMessage.value = "المبلغ يجب أن يكون أكبر من صفر"
            return
        }

        val payment = SupplierPayment(
            supplierId = supplierId,
            supplierName = supplierName,
            amount = amount,
            paymentDate = getCurrentDate(),
            notes = notes
        )

        viewModelScope.launch {
            try {
                repository.addPayment(payment)
                _errorMessage.value = null
                loadPaymentsForSupplier(supplierId)
                loadStatisticsForSuppliers(listOf(_selectedSupplier.value ?: return@launch))
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في إضافة الدفعة: ${e.message}"
            }
        }
    }

    fun deletePurchase(purchase: SupplierPurchase) {
        viewModelScope.launch {
            try {
                repository.deletePurchase(purchase)
                _errorMessage.value = null
                loadPurchasesForSupplier(purchase.supplierId)
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في حذف المشتريات: ${e.message}"
            }
        }
    }

    fun deletePayment(payment: SupplierPayment) {
        viewModelScope.launch {
            try {
                repository.deletePayment(payment)
                _errorMessage.value = null
                loadPaymentsForSupplier(payment.supplierId)
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في حذف الدفعة: ${e.message}"
            }
        }
    }

    fun searchSuppliers(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadSuppliers()
            } else {
                repository.searchSuppliers(query).collect { suppliersList ->
                    _suppliers.value = suppliersList
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun getCurrentDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ar", "SA"))
        return format.format(Date())
    }

    fun getFormattedDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ar", "SA"))
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar", "SA"))
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}
