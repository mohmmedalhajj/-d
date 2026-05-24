package com.example.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class DatabaseBackupPayload(
    val inventory: List<QatInventory> = emptyList(),
    val sales: List<QatSale> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val debtTransactions: List<DebtTransaction> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val supplierTransactions: List<SupplierTransaction> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val financialTransfers: List<FinancialTransfer> = emptyList(),
    val dailyArchives: List<DailyArchive> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val dailyFinanceMetas: List<DailyFinanceMeta> = emptyList()
)

class AppRepository(
    private val context: Context,
    private val appDao: AppDao
) {
    fun appDao(): AppDao = appDao

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(DatabaseBackupPayload::class.java)

    // --- Flow Expositions ---
    val settingsFlow: Flow<AppSettings?> = appDao.getSettingsFlow()
    val inventoryFlow: Flow<List<QatInventory>> = appDao.getAllInventoryFlow()
    val salesFlow: Flow<List<QatSale>> = appDao.getAllSalesFlow()
    val customersFlow: Flow<List<Customer>> = appDao.getAllCustomersFlow()
    val debtTransactionsFlow: Flow<List<DebtTransaction>> = appDao.getAllDebtTransactionsFlow()
    val suppliersFlow: Flow<List<Supplier>> = appDao.getAllSuppliersFlow()
    val supplierTransactionsFlow: Flow<List<SupplierTransaction>> = appDao.getAllSupplierTransactionsFlow()
    val expensesFlow: Flow<List<Expense>> = appDao.getAllExpensesFlow()
    val transfersFlow: Flow<List<FinancialTransfer>> = appDao.getAllTransfersFlow()
    val archivesFlow: Flow<List<DailyArchive>> = appDao.getAllArchivesFlow()
    val backupsFlow: Flow<List<BackupRecord>> = appDao.getAllBackupsFlow()
    val printLogsFlow: Flow<List<PrintLog>> = appDao.getAllPrintLogsFlow()
    val dailyFinanceMetaFlow: Flow<List<DailyFinanceMeta>> = appDao.getAllDailyFinanceMetaFlow()

    // --- Settings ---
    suspend fun saveSettings(settings: AppSettings) {
        appDao.insertSettings(settings)
    }

    suspend fun getSettingsDirect(): AppSettings {
        return appDao.getSettingsDirect() ?: AppSettings().also {
            appDao.insertSettings(it)
        }
    }

    // --- Daily Finance Meta ---
    suspend fun saveDailyFinanceMeta(meta: DailyFinanceMeta) = appDao.insertDailyFinanceMeta(meta)
    suspend fun getDailyFinanceMetaDirect(date: String): DailyFinanceMeta? = appDao.getDailyFinanceMetaDirect(date)
    fun getDailyFinanceMetaFlow(date: String): Flow<DailyFinanceMeta?> = appDao.getDailyFinanceMetaFlow(date)

    private val gson = Gson()

    fun purchaseItemsToJson(items: List<PurchaseItem>): String {
        return gson.toJson(items)
    }

    fun jsonToPurchaseItems(json: String): List<PurchaseItem> {
        return try {
            val type = object : TypeToken<List<PurchaseItem>>() {}.type
            gson.fromJson<List<PurchaseItem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Seed Data ---
    suspend fun seedDefaultDataIfNeeded() {
        withContext(Dispatchers.IO) {
            // Seed default settings if empty
            if (appDao.getSettingsDirect() == null) {
                appDao.insertSettings(AppSettings(id = 1, taxAmount = 200.0))
            }

            // Seed default supplier "هاشم البراق" if empty
            val suppliers = appDao.getAllSuppliersDirect()
            if (suppliers.none { it.name == "هاشم البراق" }) {
                appDao.insertSupplier(
                    Supplier(
                        name = "هاشم البراق",
                        phone = "777000000",
                        address = "محافظة صعدة",
                        notes = "نوع القات: قات صعدي"
                    )
                )
            }
        }
    }

    // --- Inventory Functions ---
    suspend fun addInventory(item: QatInventory) = appDao.insertInventory(item)
    suspend fun updateInventory(item: QatInventory) = appDao.updateInventory(item)
    suspend fun deleteInventory(item: QatInventory) = appDao.deleteInventory(item)

    // --- Sales Functions ---
    suspend fun addSale(sale: QatSale) {
        withContext(Dispatchers.IO) {
            appDao.insertSale(sale)
            // Deduct from stock: we match by qatType. Since inventory is listed by date/batch, 
            // we deduct quantity from the latest stock entry with matched qatType, or we can simply 
            // record the sale. The user asks: "عند الحفظ خصم الكمية من المخزون وتحديث الحسابات والأرباح والتقارير"
            // Let's implement active stock deduction! We'll search for inventory entries of that qatType and adjust.
            val inventoryList = appDao.getAllInventoryDirect().filter { it.qatType.equals(sale.qatType, ignoreCase = true) }
            var remainingToDeduct = sale.quantity
            for (stockItem in inventoryList) {
                if (remainingToDeduct <= 0.0) break
                if (stockItem.quantity > 0.0) {
                    val deduct = minOf(stockItem.quantity, remainingToDeduct)
                    val updatedStock = stockItem.copy(quantity = stockItem.quantity - deduct)
                    appDao.updateInventory(updatedStock)
                    remainingToDeduct -= deduct
                }
            }

            // If it is debt, record a debt transaction for the customer
            if (!sale.isCash && sale.customerName.isNotBlank()) {
                // Ensure customer exists
                val customers = appDao.getAllCustomersDirect()
                if (customers.none { it.name == sale.customerName }) {
                    appDao.insertCustomer(Customer(name = sale.customerName, phone = "", notes = "أضيف تلقائياً عبر فاتورة آجل"))
                }
                appDao.insertDebtTransaction(
                    DebtTransaction(
                        customerName = sale.customerName,
                        amount = sale.quantity * sale.sellingPrice,
                        type = "دين",
                        date = sale.date,
                        notes = "فاتورة بيع آجل صنف: ${sale.qatType} - كمية: ${sale.quantity}"
                    )
                )
            }
        }
    }

    suspend fun deleteSale(sale: QatSale) {
        withContext(Dispatchers.IO) {
            appDao.deleteSale(sale)
            // Restore inventory if possible
            val inventoryList = appDao.getAllInventoryDirect().filter { it.qatType.equals(sale.qatType, ignoreCase = true) }
            if (inventoryList.isNotEmpty()) {
                val latestStock = inventoryList.first()
                appDao.updateInventory(latestStock.copy(quantity = latestStock.quantity + sale.quantity))
            }
        }
    }

    // --- Customers & Debts ---
    suspend fun addCustomer(customer: Customer) = appDao.insertCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = appDao.deleteCustomer(customer)
    suspend fun addDebtTransaction(tx: DebtTransaction) = appDao.insertDebtTransaction(tx)
    suspend fun deleteDebtTransaction(tx: DebtTransaction) = appDao.deleteDebtTransaction(tx)
    fun getCustomerDebtTransactionsFlow(name: String) = appDao.getCustomerDebtTransactionsFlow(name)

    // --- Suppliers ---
    suspend fun addSupplier(supplier: Supplier) = appDao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = appDao.insertSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = appDao.deleteSupplier(supplier)
    suspend fun addSupplierTransaction(tx: SupplierTransaction) {
        withContext(Dispatchers.IO) {
            appDao.insertSupplierTransaction(tx)
            // If it is a purchase (شراء), let's ALSO automatically add it to Inventory!
            if (tx.type == "شراء" && tx.quantity > 0.0) {
                // Get general settings for tax
                val settings = getSettingsDirect()
                appDao.insertInventory(
                    QatInventory(
                        qatType = tx.qatType,
                        quantity = tx.quantity,
                        unitPrice = tx.unitPrice,
                        date = tx.date,
                        qatTax = settings.taxAmount
                    )
                )
            }
        }
    }
    suspend fun deleteSupplierTransaction(tx: SupplierTransaction) = appDao.deleteSupplierTransaction(tx)
    fun getSupplierTransactionsFlow(name: String) = appDao.getSupplierTransactionsFlow(name)

    // --- Expenses ---
    suspend fun addExpense(expense: Expense) = appDao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = appDao.deleteExpense(expense)

    // --- Transfers ---
    suspend fun addTransfer(transfer: FinancialTransfer) = appDao.insertTransfer(transfer)
    suspend fun deleteTransfer(transfer: FinancialTransfer) = appDao.deleteTransfer(transfer)

    // --- Archives ---
    suspend fun addArchive(archive: DailyArchive) = appDao.insertArchive(archive)

    // --- Print Log ---
    suspend fun addPrintLog(log: PrintLog) = appDao.insertPrintLog(log)

    // --- JSON Backup / Restore implementation ---
    suspend fun createLocalBackup(): Result<BackupRecord> = withContext(Dispatchers.IO) {
        try {
            val payload = DatabaseBackupPayload(
                inventory = appDao.getAllInventoryDirect(),
                sales = appDao.getAllSalesDirect(),
                customers = appDao.getAllCustomersDirect(),
                debtTransactions = appDao.getAllDebtTransactionsDirect(),
                suppliers = appDao.getAllSuppliersDirect(),
                supplierTransactions = appDao.getAllSupplierTransactionsDirect(),
                expenses = appDao.getAllExpensesDirect(),
                financialTransfers = appDao.getAllTransfersDirect(),
                dailyArchives = appDao.getAllArchivesDirect(),
                appSettings = getSettingsDirect(),
                dailyFinanceMetas = appDao.getAllDailyFinanceMetaDirect()
            )

            val jsonString = backupAdapter.toJson(payload)
            val backupFolder = File(context.filesDir, "backups")
            if (!backupFolder.exists()) {
                backupFolder.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val fileName = "tofan_backup_$timestamp.json"
            val backupFile = File(backupFolder, fileName)
            backupFile.writeText(jsonString)

            val record = BackupRecord(
                backupTime = System.currentTimeMillis(),
                backupFilePath = backupFile.absolutePath,
                fileName = fileName
            )
            appDao.insertBackup(record)
            Result.success(record)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBackupPreview(record: BackupRecord): Result<DatabaseBackupPayload> = withContext(Dispatchers.IO) {
        try {
            val file = File(record.backupFilePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("ملف النسخة الاحتياطية غير موجود"))
            }
            val jsonString = file.readText()
            val payload = backupAdapter.fromJson(jsonString) ?: return@withContext Result.failure(Exception("صيغة الملف غير صحيحة"))
            Result.success(payload)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(record: BackupRecord): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val previewRes = getBackupPreview(record)
            if (previewRes.isFailure) {
                return@withContext Result.failure(previewRes.exceptionOrNull() ?: Exception("فشل قراءة الملف"))
            }
            val payload = previewRes.getOrNull() ?: return@withContext Result.failure(Exception("ملف فارغ"))

            // Overwrite database
            // Clear existing operations
            appDao.deleteAllInventory()
            appDao.deleteAllSales()
            appDao.deleteAllDebtTransactions()
            appDao.deleteAllSupplierTransactions()
            appDao.deleteAllExpenses()
            appDao.deleteAllTransfers()
            appDao.deleteAllDailyFinanceMeta()

            // Repopulate
            payload.inventory.forEach { appDao.insertInventory(it) }
            payload.sales.forEach { appDao.insertSale(it) }
            payload.customers.forEach { appDao.insertCustomer(it) }
            payload.debtTransactions.forEach { appDao.insertDebtTransaction(it) }
            payload.suppliers.forEach { appDao.insertSupplier(it) }
            payload.supplierTransactions.forEach { appDao.insertSupplierTransaction(it) }
            payload.expenses.forEach { appDao.insertExpense(it) }
            payload.financialTransfers.forEach { appDao.insertTransfer(it) }
            payload.dailyArchives.forEach { appDao.insertArchive(it) }
            payload.dailyFinanceMetas.forEach { appDao.insertDailyFinanceMeta(it) }
            appDao.insertSettings(payload.appSettings)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBackupRecord(record: BackupRecord) = withContext(Dispatchers.IO) {
        try {
            val file = File(record.backupFilePath)
            if (file.exists()) {
                file.delete()
            }
            appDao.deleteBackup(record)
        } catch (e: Exception) {
            appDao.deleteBackup(record)
        }
    }

    // --- Archive Process ---
    suspend fun archiveTodayData(dateString: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Get today's operations
            val inventory = appDao.getAllInventoryDirect()
            val sales = appDao.getAllSalesDirect().filter { it.date == dateString }
            val expenses = appDao.getAllExpensesDirect().filter { it.date == dateString }
            val transfers = appDao.getAllTransfersDirect().filter { it.date == dateString }
            val debts = appDao.getAllDebtTransactionsDirect().filter { it.date == dateString && it.type == "دين" }

            val totalInventoryValue = inventory.sumOf { it.quantity * it.unitPrice }
            val totalSales = sales.sumOf { it.quantity * it.sellingPrice }
            val totalExpenses = expenses.sumOf { it.amount }
            val totalTransfers = transfers.sumOf { it.amount }
            val totalDebts = debts.sumOf { it.amount }

            // Calculate profits: Sales - cost of sold items - expenses - tax.
            // Cost of sold items is estimated from Sales * unit price of matched stock, or simple estimate
            var totalCost = 0.0
            sales.forEach { sale ->
                val matchedInventory = inventory.filter { it.qatType.equals(sale.qatType, ignoreCase = true) }
                val avgCost = if (matchedInventory.isNotEmpty()) {
                    matchedInventory.map { it.unitPrice }.average()
                } else {
                    sale.sellingPrice * 0.7 // fallback 70% cost
                }
                totalCost += sale.quantity * avgCost
            }

            // Total tax of sales: 
            val generalTax = getSettingsDirect().taxAmount
            val profit = totalSales - totalCost - totalExpenses

            val detailsPayload = DatabaseBackupPayload(
                inventory = inventory,
                sales = sales,
                expenses = expenses,
                financialTransfers = transfers,
                appSettings = getSettingsDirect()
            )
            val detailsJson = backupAdapter.toJson(detailsPayload)

            val archive = DailyArchive(
                archiveDate = dateString,
                totalInventoryValue = totalInventoryValue,
                totalSales = totalSales,
                totalProfit = if (profit > 0.0) profit else 0.0,
                totalLoss = if (profit < 0.0) -profit else 0.0,
                totalDebts = totalDebts,
                totalExpenses = totalExpenses,
                totalTransfers = totalTransfers,
                detailsJson = detailsJson
            )

            appDao.insertArchive(archive)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
