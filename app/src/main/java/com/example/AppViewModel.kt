package com.example

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs: SharedPreferences =
        application.getSharedPreferences("tofan_al_aqsa_prefs", Context.MODE_PRIVATE)

    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(application, database.appDao())

    // --- Authentication ---
    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // --- Bottom Navigation Tab Index ---
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // --- State Flows from Repository ---
    val settings = repository.settingsFlow
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val inventoryFlow = repository.inventoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val salesFlow = repository.salesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customersFlow = repository.customersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val debtTransactionsFlow = repository.debtTransactionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val suppliersFlow = repository.suppliersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val supplierTransactionsFlow = repository.supplierTransactionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val expensesFlow = repository.expensesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transfersFlow = repository.transfersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivesFlow = repository.archivesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val backupsFlow = repository.backupsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val printLogsFlow = repository.printLogsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dailyFinanceMetaFlow = repository.dailyFinanceMetaFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Receipt Preview Queues ---
    val activeReceiptPreview = MutableStateFlow<ReceiptData?>(null)

    fun showReceipt(receipt: ReceiptData?) {
        activeReceiptPreview.value = receipt
    }

    data class ReceiptData(
        val title: String,
        val lines: List<Pair<String, String>>,
        val total: Double,
        val category: String
    )

    init {
        viewModelScope.launch {
            repository.seedDefaultDataIfNeeded()
            setupAutoArchiveTask()
            setupWeeklyBackupCheck()
        }
    }

    // --- Authentication Actions ---
    fun login(username: String, pass: String): Boolean {
        if (username.trim() == "admin" && pass == "123456") {
            sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    fun logout() {
        sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
        _isLoggedIn.value = false
        _activeTab.value = 0
    }

    // --- Tab Selection Swipe Helpers ---
    fun setTab(index: Int) {
        val clamped = index.coerceIn(0, 10)
        _activeTab.value = clamped
    }

    fun nextTab() {
        if (_activeTab.value < 10) _activeTab.value++
    }

    fun prevTab() {
        if (_activeTab.value > 0) _activeTab.value--
    }

    // --- AppSettings Actions ---
    fun saveSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
    }

    // --- Daily Finance Meta Actions ---
    fun saveDailyFinanceMeta(qatTax: Double, outflowMaktab: Double, outflowWorkers: Double, notes: String) {
        viewModelScope.launch {
            val date = getCurrentDateString()
            val existing = repository.getDailyFinanceMetaDirect(date)
            val updated = existing?.copy(
                qatTax = qatTax,
                outflowMaktab = outflowMaktab,
                outflowWorkers = outflowWorkers,
                notes = notes
            ) ?: DailyFinanceMeta(
                date = date,
                qatTax = qatTax,
                outflowMaktab = outflowMaktab,
                outflowWorkers = outflowWorkers,
                notes = notes
            )
            repository.saveDailyFinanceMeta(updated)
        }
    }

    // --- Inventory Operations ---
    fun addInventory(qatType: String, quantity: Double, unitPrice: Double, tax: Double = 0.0) {
        viewModelScope.launch {
            val date = getCurrentDateString()
            repository.addInventory(
                QatInventory(
                    qatType = qatType,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    date = date,
                    qatTax = tax
                )
            )
        }
    }

    fun updateInventory(item: QatInventory) {
        viewModelScope.launch {
            repository.updateInventory(item)
        }
    }

    fun deleteInventory(item: QatInventory) {
        viewModelScope.launch {
            repository.deleteInventory(item)
        }
    }

    // --- Sales Operations ---
    fun addSale(qatType: String, quantity: Double, sellingPrice: Double, isCash: Boolean, customerName: String) {
        viewModelScope.launch {
            val date = getCurrentDateString()
            repository.addSale(
                QatSale(
                    qatType = qatType,
                    quantity = quantity,
                    sellingPrice = sellingPrice,
                    isCash = isCash,
                    customerName = customerName,
                    date = date
                )
            )
        }
    }

    fun deleteSale(sale: QatSale) {
        viewModelScope.launch {
            repository.deleteSale(sale)
        }
    }

    // --- Customer Operations ---
    fun addCustomer(name: String, phone: String, address: String, notes: String) {
        viewModelScope.launch {
            repository.addCustomer(Customer(name, phone, address, notes))
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // --- Debts & Customer Account Statements ---
    fun addDebtTransaction(customerName: String, amount: Double, type: String, notes: String, date: String = getCurrentDateString()) {
        viewModelScope.launch {
            repository.addDebtTransaction(
                DebtTransaction(
                    customerName = customerName,
                    amount = amount,
                    type = type,
                    date = date,
                    notes = notes
                )
            )
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.addCustomer(customer)
        }
    }

    fun updateDebtTransaction(tx: DebtTransaction) {
        viewModelScope.launch {
            repository.addDebtTransaction(tx)
        }
    }

    fun deleteDebtTransaction(tx: DebtTransaction) {
        viewModelScope.launch {
            repository.deleteDebtTransaction(tx)
        }
    }

    // --- Supplier Operations ---
    fun addSupplier(name: String, phone: String, address: String, qatType: String, notes: String) {
        viewModelScope.launch {
            repository.addSupplier(Supplier(name, phone, address, qatType, notes))
        }
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.updateSupplier(supplier)
        }
    }

    fun addSupplierTransaction(
        supplierName: String,
        qatType: String,
        quantity: Double,
        unitPrice: Double,
        paid: Double,
        type: String,
        notes: String = "",
        items: List<PurchaseItem> = emptyList()
    ) {
        viewModelScope.launch {
            val date = getCurrentDateString()
            val totalCost = quantity * unitPrice
            val remaining = if (type == "شراء") totalCost - paid else 0.0
            repository.addSupplierTransaction(
                SupplierTransaction(
                    supplierName = supplierName,
                    qatType = qatType,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    amountPaid = paid,
                    debtRemaining = remaining,
                    type = type,
                    date = date,
                    notes = notes,
                    itemsJson = repository.purchaseItemsToJson(items)
                )
            )
        }
    }

    fun deleteSupplierTransaction(tx: SupplierTransaction) {
        viewModelScope.launch {
            repository.deleteSupplierTransaction(tx)
        }
    }

    // --- Expense Operations ---
    fun addExpense(description: String, amount: Double, notes: String) {
        viewModelScope.launch {
            val date = getCurrentDateString()
            repository.addExpense(Expense(description = description, amount = amount, date = date, notes = notes))
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- Financial Transfer Operations ---
    fun addTransfer(amount: Double, description: String, sender: String, exchange: String) {
        viewModelScope.launch {
            val date = getCurrentDateString()
            repository.addTransfer(
                FinancialTransfer(
                    amount = amount,
                    date = date,
                    description = description,
                    senderName = sender,
                    exchangeHouse = exchange
                )
            )
        }
    }

    fun deleteTransfer(transfer: FinancialTransfer) {
        viewModelScope.launch {
            repository.deleteTransfer(transfer)
        }
    }

    // --- Printing Log ---
    fun logPrint(success: Boolean, category: String, message: String) {
        viewModelScope.launch {
            repository.addPrintLog(PrintLog(category = category, isSuccess = success, errorMessage = message))
        }
    }

    // --- JSON Backup/Restore Operations ---
    fun triggerLocalBackup() {
        viewModelScope.launch {
            val res = repository.createLocalBackup()
            if (res.isSuccess) {
                Toast.makeText(getApplication(), "تم إنشاء النسخة الاحتياطية بنجاح بنظام ومسار آمن", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplication(), "فشل إنشاء النسخة الاحتياطية: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun restoreBackup(record: BackupRecord) {
        viewModelScope.launch {
            val res = repository.restoreBackup(record)
            if (res.isSuccess) {
                Toast.makeText(getApplication(), "تم استرجاع قواعد البيانات والعمليات بالكامل!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(getApplication(), "فشل الاسترجاع: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteBackup(record: BackupRecord) {
        viewModelScope.launch {
            repository.deleteBackupRecord(record)
        }
    }

    // --- Clock Helpers ---
    fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    }

    fun getCurrentTimeString(): String {
        return SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
    }

    // --- Midnight Auto-Archive Task ---
    private fun setupAutoArchiveTask() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                if (hour == 0 && minute == 0) {
                    val dateString = getCurrentDateString()
                    val archives = repository.appDao().getAllArchivesDirect()
                    val alreadyArchivedToday = archives.any { it.archiveDate == dateString }

                    if (!alreadyArchivedToday) {
                        repository.archiveTodayData(dateString)
                        // Trigger a backup together on auto-archive
                        repository.createLocalBackup()
                    }
                }
                delay(60000) // check every minute
            }
        }
    }

    // --- Backup Periodic (Every 7 Days) Task ---
    private fun setupWeeklyBackupCheck() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastBackupTime = sharedPrefs.getLong("last_backup_time", 0L)
            val now = System.currentTimeMillis()
            val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L

            if (now - lastBackupTime >= sevenDaysMs) {
                repository.createLocalBackup()
                sharedPrefs.edit().putLong("last_backup_time", now).apply()
            }
        }
    }

    fun triggerManualArchive() {
        viewModelScope.launch {
            val date = getCurrentDateString()
            val res = repository.archiveTodayData(date)
            if (res.isSuccess) {
                Toast.makeText(getApplication(), "تم أرشفة ومطابقة حسابات اليوم بنجاح محلياً!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplication(), "فشل الأرشفة: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
