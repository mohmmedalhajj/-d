package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ---AppSettings ---
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)

    // --- Inventory ---
    @Query("SELECT * FROM inventory ORDER BY date DESC, id DESC")
    fun getAllInventoryFlow(): Flow<List<QatInventory>>

    @Query("SELECT * FROM inventory ORDER BY date DESC, id DESC")
    suspend fun getAllInventoryDirect(): List<QatInventory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: QatInventory)

    @Update
    suspend fun updateInventory(item: QatInventory)

    @Delete
    suspend fun deleteInventory(item: QatInventory)

    @Query("DELETE FROM inventory")
    suspend fun deleteAllInventory()

    // --- Sales ---
    @Query("SELECT * FROM sales ORDER BY date DESC, id DESC")
    fun getAllSalesFlow(): Flow<List<QatSale>>

    @Query("SELECT * FROM sales ORDER BY date DESC, id DESC")
    suspend fun getAllSalesDirect(): List<QatSale>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: QatSale)

    @Update
    suspend fun updateSale(sale: QatSale)

    @Delete
    suspend fun deleteSale(sale: QatSale)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()

    // --- Customers & Debts ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomersDirect(): List<Customer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM debt_transactions ORDER BY date DESC, id DESC")
    fun getAllDebtTransactionsFlow(): Flow<List<DebtTransaction>>

    @Query("SELECT * FROM debt_transactions WHERE customerName = :customerName ORDER BY date DESC, id DESC")
    fun getCustomerDebtTransactionsFlow(customerName: String): Flow<List<DebtTransaction>>

    @Query("SELECT * FROM debt_transactions ORDER BY date DESC, id DESC")
    suspend fun getAllDebtTransactionsDirect(): List<DebtTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtTransaction(transaction: DebtTransaction)

    @Delete
    suspend fun deleteDebtTransaction(transaction: DebtTransaction)

    @Query("DELETE FROM debt_transactions")
    suspend fun deleteAllDebtTransactions()

    // --- Suppliers ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliersFlow(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    suspend fun getAllSuppliersDirect(): List<Supplier>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("SELECT * FROM supplier_transactions ORDER BY date DESC, id DESC")
    fun getAllSupplierTransactionsFlow(): Flow<List<SupplierTransaction>>

    @Query("SELECT * FROM supplier_transactions WHERE supplierName = :supplierName ORDER BY date DESC, id DESC")
    fun getSupplierTransactionsFlow(supplierName: String): Flow<List<SupplierTransaction>>

    @Query("SELECT * FROM supplier_transactions ORDER BY date DESC, id DESC")
    suspend fun getAllSupplierTransactionsDirect(): List<SupplierTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierTransaction(transaction: SupplierTransaction)

    @Delete
    suspend fun deleteSupplierTransaction(transaction: SupplierTransaction)

    @Query("DELETE FROM supplier_transactions")
    suspend fun deleteAllSupplierTransactions()

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    suspend fun getAllExpensesDirect(): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    // --- Transfers ---
    @Query("SELECT * FROM financial_transfers ORDER BY date DESC, id DESC")
    fun getAllTransfersFlow(): Flow<List<FinancialTransfer>>

    @Query("SELECT * FROM financial_transfers ORDER BY date DESC, id DESC")
    suspend fun getAllTransfersDirect(): List<FinancialTransfer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: FinancialTransfer)

    @Delete
    suspend fun deleteTransfer(transfer: FinancialTransfer)

    @Query("DELETE FROM financial_transfers")
    suspend fun deleteAllTransfers()

    // --- Daily Archives ---
    @Query("SELECT * FROM daily_archives ORDER BY archiveDate DESC")
    fun getAllArchivesFlow(): Flow<List<DailyArchive>>

    @Query("SELECT * FROM daily_archives ORDER BY archiveDate DESC")
    suspend fun getAllArchivesDirect(): List<DailyArchive>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: DailyArchive)

    @Delete
    suspend fun deleteArchive(archive: DailyArchive)

    // --- Backup records ---
    @Query("SELECT * FROM backup_records ORDER BY backupTime DESC")
    fun getAllBackupsFlow(): Flow<List<BackupRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(record: BackupRecord)

    @Delete
    suspend fun deleteBackup(record: BackupRecord)

    // --- Print Logs ---
    @Query("SELECT * FROM print_logs ORDER BY timestamp DESC")
    fun getAllPrintLogsFlow(): Flow<List<PrintLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrintLog(log: PrintLog)

    // --- Daily Finance Meta ---
    @Query("SELECT * FROM daily_finance_meta ORDER BY date DESC")
    fun getAllDailyFinanceMetaFlow(): Flow<List<DailyFinanceMeta>>

    @Query("SELECT * FROM daily_finance_meta ORDER BY date DESC")
    suspend fun getAllDailyFinanceMetaDirect(): List<DailyFinanceMeta>

    @Query("SELECT * FROM daily_finance_meta WHERE date = :date LIMIT 1")
    suspend fun getDailyFinanceMetaDirect(date: String): DailyFinanceMeta?

    @Query("SELECT * FROM daily_finance_meta WHERE date = :date LIMIT 1")
    fun getDailyFinanceMetaFlow(date: String): Flow<DailyFinanceMeta?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyFinanceMeta(meta: DailyFinanceMeta)

    @Query("DELETE FROM daily_finance_meta")
    suspend fun deleteAllDailyFinanceMeta()
}
