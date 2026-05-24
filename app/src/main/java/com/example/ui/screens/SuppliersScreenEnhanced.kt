package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.AppViewModel
import com.example.data.*
import com.example.ui.components.PdfExportManager
import com.example.ui.components.WhatsAppShareManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SuppliersScreenEnhanced(
    appViewModel: AppViewModel,
    suppliers: List<Supplier>,
    transactions: List<SupplierTransaction>,
    settings: AppSettings
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterDate by remember { mutableStateOf("") }

    // State for new supplier form
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var supplierGov by remember { mutableStateOf("") }
    var supplierQat by remember { mutableStateOf("") }
    var supplierNotes by remember { mutableStateOf("") }

    // Default supplier check
    LaunchedEffect(suppliers) {
        if (suppliers.isEmpty() && supplierName.isEmpty()) {
            supplierName = "هاشم البراق"
            supplierPhone = "777000000"
            supplierGov = "محافظة صعدة"
            supplierQat = "قات صعدي"
            supplierNotes = ""
        }
    }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.isBlank()) suppliers
        else suppliers.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
            it.phone.contains(trimmed, ignoreCase = true) ||
            it.address.contains(trimmed, ignoreCase = true) ||
            it.notes.contains(trimmed, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101311))
            .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
    ) {
        if (selectedSupplier == null) {
            // List View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    HeaderSection()
                }

                item {
                    StatisticsCardsSection(suppliers, transactions)
                }

                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClear = { searchQuery = "" }
                    )
                }

                item {
                    AddSupplierButton(onClick = {
                        editingSupplier = null
                        supplierName = ""
                        supplierPhone = ""
                        supplierGov = ""
                        supplierQat = ""
                        supplierNotes = ""
                        showAddDialog = true
                    })
                }

                items(filteredSuppliers) { supplier ->
                    SupplierCardEnhanced(
                        supplier = supplier,
                        transactions = transactions.filter { it.supplierName == supplier.name },
                        onSelect = { selectedSupplier = it },
                        onEdit = {
                            editingSupplier = supplier
                            supplierName = supplier.name
                            supplierPhone = supplier.phone
                            supplierGov = supplier.address
                            supplierQat = supplier.notes
                            supplierNotes = ""
                            showAddDialog = true
                        },
                        onDelete = {
                            selectedSupplier = supplier
                            showDeleteConfirm = true
                        },
                        onShare = {
                            val supplierTransactions = transactions.filter { it.supplierName == supplier.name }
                            val totalPurchases = supplierTransactions
                                .filter { it.type == "شراء" }
                                .sumOf { it.amountPaid }
                            val totalPaid = supplierTransactions
                                .filter { it.type == "سداد" }
                                .sumOf { it.amountPaid }

                            val whatsapp = WhatsAppShareManager(context)
                            val success = whatsapp.shareSupplierReport(
                                supplier.name,
                                supplier.phone,
                                totalPurchases,
                                totalPaid,
                                totalPurchases - totalPaid,
                                supplierTransactions.map { tx ->
                                    if (tx.type == "شراء") {
                                        "${tx.date} شراء ${tx.qatType} - ${tx.amountPaid} ر.ي - ${tx.notes}"
                                    } else {
                                        "${tx.date} سداد ${tx.amountPaid} ر.ي - ${tx.notes}"
                                    }
                                }
                            )
                            if (!success) {
                                Toast.makeText(context, "فشل مشاركة الواتساب، تأكد من تثبيت واتساب", Toast.LENGTH_LONG).show()
                            }
                        },
                        onExport = {
                            val pdfManager = PdfExportManager(context)
                            val supplierTransactions = transactions.filter { it.supplierName == supplier.name }
                            val totalPurchases = supplierTransactions
                                .filter { it.type == "شراء" }
                                .sumOf { it.amountPaid }
                            val totalPaid = supplierTransactions
                                .filter { it.type == "سداد" }
                                .sumOf { it.amountPaid }

                            val reportLines = supplierTransactions.map { tx ->
                                if (tx.type == "شراء") {
                                    "${tx.date}: شراء ${tx.qatType} - كمية ${tx.quantity} - إجمالي ${tx.quantity * tx.unitPrice} ر.ي"
                                } else {
                                    "${tx.date}: سداد ${tx.amountPaid} ر.ي - ${tx.notes}"
                                }
                            }

                            val file = pdfManager.createSupplierPdfReport(
                                supplier.name,
                                supplier.phone,
                                totalPurchases,
                                totalPaid,
                                totalPurchases - totalPaid,
                                reportLines
                            )
                            if (file == null) {
                                Toast.makeText(context, "فشل تصدير PDF. حاول مرة أخرى", Toast.LENGTH_LONG).show()
                            }
                        },
                        onPrint = {
                            if (settings.printerName.isBlank()) {
                                Toast.makeText(context, "يرجى تعيين الطابعة الحرارية أولاً من الإعدادات", Toast.LENGTH_LONG).show()
                                return@SupplierCardEnhanced
                            }
                            val supplierTransactions = transactions.filter { it.supplierName == supplier.name }
                            val totalPurchases = supplierTransactions
                                .filter { it.type == "شراء" }
                                .sumOf { it.amountPaid }
                            val totalPaid = supplierTransactions
                                .filter { it.type == "سداد" }
                                .sumOf { it.amountPaid }
                            val lines = listOf(
                                "الاسم" to supplier.name,
                                "الهاتف" to supplier.phone,
                                "إجمالي المشتريات" to "%.2f ر.ي".format(totalPurchases),
                                "إجمالي المسدد" to "%.2f ر.ي".format(totalPaid),
                                "المتبقي" to "%.2f ر.ي".format(totalPurchases - totalPaid)
                            )
                            appViewModel.showReceipt(
                                AppViewModel.ReceiptData(
                                    title = "كشف حساب المورد",
                                    lines = lines,
                                    total = totalPurchases - totalPaid,
                                    category = "موردين"
                                )
                            )
                        }
                    )
                }

                if (suppliers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "لا توجد موردين. اضغط + لإضافة مورد جديد",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            // Detail View
            SupplierDetailViewEnhanced(
                supplier = selectedSupplier!!,
                transactions = transactions.filter { it.supplierName == selectedSupplier!!.name },
                onBack = { selectedSupplier = null },
                onAddPurchase = { showPurchaseDialog = true },
                onAddPayment = { showPaymentDialog = true },
                onDelete = { showDeleteConfirm = true }
            )
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = Color(0xFF009639)
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة مورد", tint = Color.White)
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddSupplierDialogEnhanced(
            supplier = editingSupplier,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, gov, qat, notes ->
                if (editingSupplier != null) {
                    appViewModel.saveSupplier(
                        editingSupplier!!.copy(
                            name = name,
                            phone = phone,
                            address = gov,
                            qatType = qat,
                            notes = notes
                        )
                    )
                } else {
                    appViewModel.addSupplier(name, phone, gov, qat, notes)
                }
                editingSupplier = null
                showAddDialog = false
            }
        )
    }

    if (showDeleteConfirm && selectedSupplier != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف المورد: ${selectedSupplier!!.name}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        appViewModel.deleteSupplier(selectedSupplier!!)
                        showDeleteConfirm = false
                        selectedSupplier = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showPurchaseDialog && selectedSupplier != null) {
        AddPurchaseDialogEnhanced(
            supplierName = selectedSupplier!!.name,
            onDismiss = { showPurchaseDialog = false },
            onConfirm = { items, paidNow, notes ->
                val totalAmount = items.sumOf { it.total }
                val summary = items.joinToString(" | ") { "${it.name}: ${it.quantity} × ${it.unitPrice} = ${it.total}" }
                appViewModel.addSupplierTransaction(
                    supplierName = selectedSupplier!!.name,
                    qatType = items.joinToString(", ") { it.name },
                    quantity = items.sumOf { it.quantity },
                    unitPrice = if (items.sumOf { it.quantity } > 0) totalAmount / items.sumOf { it.quantity } else 0.0,
                    paid = paidNow,
                    type = "شراء",
                    notes = summary + if (notes.isNotBlank()) " - $notes" else "",
                    items = items
                )
                showPurchaseDialog = false
            }
        )
    }

    if (showPaymentDialog && selectedSupplier != null) {
        AddPaymentDialogEnhanced(
            supplierName = selectedSupplier!!.name,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, notes ->
                appViewModel.addSupplierTransaction(
                    supplierName = selectedSupplier!!.name,
                    qatType = "دفعة",
                    quantity = 0.0,
                    unitPrice = 0.0,
                    paid = amount,
                    type = "سداد",
                    notes = notes,
                    items = emptyList()
                )
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF009639), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "قسم الموردين",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "إدارة الموردين والمشتريات والمدفوعات",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun StatisticsCardsSection(
    suppliers: List<Supplier>,
    transactions: List<SupplierTransaction>
) {
    val totalSuppliers = suppliers.size
    val totalPurchases = transactions
        .filter { it.type == "شراء" }
        .sumOf { it.quantity * it.unitPrice }
    val totalPaid = transactions
        .filter { it.type == "سداد" }
        .sumOf { it.amountPaid }
    val remainingBalance = totalPurchases - totalPaid

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticCardSmall("عدد الموردين", totalSuppliers.toString(), Color(0xFFE3F2FD))
            StatisticCardSmall("إجمالي المشتريات", "%.0f ر.ي".format(totalPurchases), Color(0xFFF3E5F5))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticCardSmall("إجمالي المسدد", "%.0f ر.ي".format(totalPaid), Color(0xFFF1F8E9))
            StatisticCardSmall("المستحقات", "%.0f ر.ي".format(remainingBalance), Color(0xFFFFEBEE))
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("بحث بالاسم أو الهاتف أو المحافظة") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF009639),
                    focusedLabelColor = Color(0xFF009639),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color(0xFF101311)
                ),
                singleLine = true,
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Close, contentDescription = "إلغاء البحث", tint = Color.Gray)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun StatisticCardSmall(title: String, value: String, bgColor: Color) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun AddSupplierButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text("إضافة مورد جديد")
    }
}

@Composable
private fun SupplierCardEnhanced(
    supplier: Supplier,
    transactions: List<SupplierTransaction>,
    onSelect: (Supplier) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
    onPrint: () -> Unit
) {
    val totalPurchases = transactions
        .filter { it.type == "شراء" }
        .sumOf { it.quantity * it.unitPrice }
    val totalPaid = transactions
        .filter { it.type == "سداد" }
        .sumOf { it.amountPaid }
    val remaining = totalPurchases - totalPaid

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(supplier) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        supplier.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        supplier.phone,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF03A9F4))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("المشتريات", "%.0f ر.ي".format(totalPurchases))
                StatItem("المسدد", "%.0f ر.ي".format(totalPaid))
                StatItem("المتبقي", "%.0f ر.ي".format(remaining))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(36.dp)
                        .weight(1f)
                        .background(Color(0xFF25D366).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.Share, contentDescription = "واتساب", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onExport,
                    modifier = Modifier
                        .size(36.dp)
                        .weight(1f)
                        .background(Color(0xFF1976D2).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.Download, contentDescription = "PDF", tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onPrint,
                    modifier = Modifier
                        .size(36.dp)
                        .weight(1f)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.Print, contentDescription = "طباعة", tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun SupplierDetailViewEnhanced(
    supplier: Supplier,
    transactions: List<SupplierTransaction>,
    onBack: () -> Unit,
    onAddPurchase: () -> Unit,
    onAddPayment: () -> Unit,
    onDelete: () -> Unit
) {
    val totalPurchases = transactions
        .filter { it.type == "شراء" }
        .sumOf { it.quantity * it.unitPrice }
    val totalPaid = transactions
        .filter { it.type == "سداد" }
        .sumOf { it.amountPaid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text(supplier.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }

        item {
            SupplierInfoCardDetailed(supplier)
        }

        item {
            FinancialSummaryCardDetailed(totalPurchases, totalPaid)
        }

        item {
            ActionButtonsRow(onAddPurchase, onAddPayment)
        }

        if (transactions.isNotEmpty()) {
            item {
                Text("سجل العمليات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            items(transactions) { transaction ->
                TransactionItemCard(transaction)
            }
        }
    }
}

@Composable
private fun SupplierInfoCardDetailed(supplier: Supplier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoItemRow("الاسم", supplier.name)
            InfoItemRow("الهاتف", supplier.phone)
            InfoItemRow("المحافظة", supplier.address)
            InfoItemRow("نوع القات", supplier.notes)
        }
    }
}

@Composable
private fun InfoItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(value, fontSize = 12.sp, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun FinancialSummaryCardDetailed(totalPurchases: Double, totalPaid: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("الملخص المالي", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("%.2f ر.ي".format(totalPurchases), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("إجمالي المشتريات", fontSize = 12.sp, color = Color.Gray)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("%.2f ر.ي".format(totalPaid), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("إجمالي المسدد", fontSize = 12.sp, color = Color.Gray)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("%.2f ر.ي".format(totalPurchases - totalPaid), fontSize = 12.sp, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                Text("الرصيد المتبقي", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(onAddPurchase: () -> Unit, onAddPayment: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onAddPurchase,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("شراء جديد", fontSize = 12.sp)
        }
        Button(
            onClick = onAddPayment,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("دفعة جديدة", fontSize = 12.sp)
        }
    }
}

@Composable
private fun TransactionItemCard(transaction: SupplierTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(transaction.date, fontSize = 11.sp, color = Color.Gray)
                Text(
                    if (transaction.type == "شراء") "شراء" else "دفعة",
                    fontSize = 11.sp,
                    color = if (transaction.type == "شراء") Color(0xFF1976D2) else Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                "%.2f ر.ي".format(transaction.amountPaid),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (transaction.notes.isNotEmpty()) {
                Text(
                    transaction.notes,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AddSupplierDialogEnhanced(
    supplier: Supplier? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var gov by remember { mutableStateOf(supplier?.address ?: "") }
    var qat by remember { mutableStateOf(supplier?.qatType ?: "") }
    var notes by remember { mutableStateOf(supplier?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    if (supplier == null) "إضافة مورد جديد" else "تعديل بيانات المورد",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المورد") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = gov,
                    onValueChange = { gov = it },
                    label = { Text("المحافظة") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = qat,
                    onValueChange = { qat = it },
                    label = { Text("نوع القات") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = { onConfirm(name, phone, gov, qat, notes) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                    ) {
                        Text(if (supplier == null) "إضافة" else "حفظ")
                    }
                }
            }
        }
    }
}

private data class PurchaseRow(
    val name: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val notes: String = ""
)

@Composable
private fun AddPurchaseDialogEnhanced(
    supplierName: String,
    onDismiss: () -> Unit,
    onConfirm: (List<PurchaseItem>, Double, String) -> Unit
) {
    val rows = remember { mutableStateListOf(PurchaseRow()) }
    var paidNow by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val items = rows.map { row ->
        val quantity = row.quantity.toDoubleOrNull() ?: 0.0
        val unitPrice = row.unitPrice.toDoubleOrNull() ?: 0.0
        PurchaseItem(row.name, quantity, unitPrice, quantity * unitPrice, row.notes)
    }
    val totalAmount = items.sumOf { it.total }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("إضافة عملية شراء للمورد: $supplierName", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                rows.forEachIndexed { index, row ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color(0xFF121613), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الصنف ${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                            if (rows.size > 1) {
                                TextButton(onClick = { rows.removeAt(index) }) {
                                    Text("حذف", color = Color(0xFFE53935))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = row.name,
                            onValueChange = { rows[index] = row.copy(name = it) },
                            label = { Text("اسم الصنف") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF009639),
                                focusedLabelColor = Color(0xFF009639),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = row.quantity,
                                onValueChange = { rows[index] = row.copy(quantity = it) },
                                label = { Text("الكمية") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF009639),
                                    focusedLabelColor = Color(0xFF009639),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = row.unitPrice,
                                onValueChange = { rows[index] = row.copy(unitPrice = it) },
                                label = { Text("سعر الوحدة") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF009639),
                                    focusedLabelColor = Color(0xFF009639),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = row.notes,
                            onValueChange = { rows[index] = row.copy(notes = it) },
                            label = { Text("ملاحظات الصنف") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF009639),
                                focusedLabelColor = Color(0xFF009639),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { rows.add(PurchaseRow()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة صنف آخر", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("المجموع الجزئي: %.2f ر.ي".format(totalAmount), color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paidNow,
                    onValueChange = { paidNow = it },
                    label = { Text("المبلغ المدفوع الآن") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات عامة للفاتورة") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            val finalPaid = paidNow.toDoubleOrNull() ?: 0.0
                            onConfirm(items, finalPaid, notes)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                    ) {
                        Text("حفظ العملية")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPaymentDialogEnhanced(
    supplierName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("إضافة دفعة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ (ر.ي)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        focusedLabelColor = Color(0xFF009639),
                        focusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull() ?: 0.0
                            if (amountValue > 0) {
                                onConfirm(amountValue, notes)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                    ) {
                        Text("إضافة")
                    }
                }
            }
        }
    }
}
