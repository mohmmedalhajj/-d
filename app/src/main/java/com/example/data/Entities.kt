package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class QatInventory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val qatType: String,
    val quantity: Double,
    val unitPrice: Double,
    val date: String, // YYYY-MM-DD
    val qatTax: Double = 0.0,
    val outflowMaktab: Double = 0.0,
    val outflowWorkers: Double = 0.0
)

@Entity(tableName = "sales")
data class QatSale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val qatType: String,
    val quantity: Double,
    val sellingPrice: Double,
    val isCash: Boolean = true, // true for cash, false for debt (آجل)
    val customerName: String = "", // for debt, linking to Customer
    val date: String // YYYY-MM-DD
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val name: String,
    val phone: String = "",
    val address: String = "",
    val notes: String = ""
)

@Entity(tableName = "debt_transactions")
data class DebtTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val amount: Double,
    val type: String, // "دين" (debt added) or "سداد" (payment received)
    val date: String, // YYYY-MM-DD
    val notes: String = ""
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey val name: String,
    val phone: String = "",
    val address: String = "",
    val notes: String = ""
)

@Entity(tableName = "supplier_transactions")
data class SupplierTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierName: String,
    val qatType: String = "",
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0,
    val amountPaid: Double = 0.0,
    val debtRemaining: Double = 0.0,
    val type: String, // "شراء" (purchase) or "سداد" (payment sent)
    val date: String, // YYYY-MM-DD
    val notes: String = "",
    val itemsJson: String = ""
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val notes: String = ""
)

@Entity(tableName = "financial_transfers")
data class FinancialTransfer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val description: String,
    val senderName: String = "أحمد منصور",
    val exchangeHouse: String = "عبدالله المشرقي"
)

@Entity(tableName = "daily_archives")
data class DailyArchive(
    @PrimaryKey val archiveDate: String, // YYYY-MM-DD
    val totalInventoryValue: Double = 0.0,
    val totalSales: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val totalDebts: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalTransfers: Double = 0.0,
    val detailsJson: String = ""
)

@Entity(tableName = "backup_records")
data class BackupRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val backupTime: Long,
    val backupFilePath: String,
    val fileName: String
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val taxAmount: Double = 0.0,
    val printerName: String = "",
    val printerAddress: String = "",
    val paperWidth: String = "58mm", // "58mm" or "80mm"
    val fontSize: Int = 14,
    val rtlEnabled: Boolean = true
)

@Entity(tableName = "print_logs")
data class PrintLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // e.g., "مبيعات", "ديون", "موردين"
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean,
    val errorMessage: String = ""
)

@Entity(tableName = "daily_finance_meta")
data class DailyFinanceMeta(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val qatTax: Double = 0.0,
    val outflowMaktab: Double = 0.0,
    val outflowWorkers: Double = 0.0,
    val notes: String = ""
)
