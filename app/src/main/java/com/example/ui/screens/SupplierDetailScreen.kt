package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.SupplierViewModel
import com.example.data.Supplier
import com.example.data.SupplierPayment
import com.example.data.SupplierPurchase

@Composable
fun SupplierDetailScreen(
    viewModel: SupplierViewModel,
    supplier: Supplier,
    onBack: () -> Unit
) {
    val purchases by viewModel.purchases.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val statistics by viewModel.supplierStatistics.collectAsState()

    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(supplier.id) {
        viewModel.selectSupplier(supplier)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
    ) {
        // Header
        DetailHeaderSection(supplier.name, onBack)

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Supplier Info Card
                SupplierInfoCard(supplier)
            }

            item {
                // Financial Summary
                statistics[supplier.id]?.let { stats ->
                    FinancialSummaryCard(stats)
                }
            }

            item {
                // Action Buttons
                ActionButtonsRow(
                    onAddPurchase = { showAddPurchaseDialog = true },
                    onAddPayment = { showAddPaymentDialog = true }
                )
            }

            item {
                // Purchases Section
                if (purchases.isNotEmpty()) {
                    PurchasesSection(
                        purchases = purchases,
                        onDelete = { viewModel.deletePurchase(it) }
                    )
                }
            }

            item {
                // Payments Section
                if (payments.isNotEmpty()) {
                    PaymentsSection(
                        payments = payments,
                        onDelete = { viewModel.deletePayment(it) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddPurchaseDialog) {
        AddPurchaseDialog(
            supplierName = supplier.name,
            onDismiss = { showAddPurchaseDialog = false },
            onConfirm = { items, notes ->
                // viewModel.addPurchase(supplier.id, supplier.name, items, notes)
                showAddPurchaseDialog = false
            }
        )
    }

    if (showAddPaymentDialog) {
        AddPaymentDialog(
            supplierName = supplier.name,
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { amount, notes ->
                viewModel.addPayment(supplier.id, supplier.name, amount, notes)
                showAddPaymentDialog = false
            }
        )
    }
}

@Composable
private fun DetailHeaderSection(supplierName: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF009639))
            .padding(16.dp)
            .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = supplierName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
            }
        }
    }
}

@Composable
private fun SupplierInfoCard(supplier: Supplier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
        ) {
            Text("بيانات المورد", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow("الاسم", supplier.name)
            InfoRow("الهاتف", supplier.phone)
            InfoRow("المحافظة", supplier.governorate)
            InfoRow("نوع القات", supplier.qatType)
            if (supplier.notes.isNotEmpty()) {
                InfoRow("الملاحظات", supplier.notes)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = value, fontSize = 14.sp, color = Color.Black)
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun FinancialSummaryCard(statistics: com.example.data.SupplierStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
        ) {
            Text("الملخص المالي", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            FinancialRow("إجمالي المشتريات", "%.2f ر.ي".format(statistics.totalPurchases))
            FinancialRow("إجمالي المسدد", "%.2f ر.ي".format(statistics.totalPaid))
            FinancialRow("الرصيد المتبقي", "%.2f ر.ي".format(statistics.remainingBalance), isRemaining = true)
        }
    }
}

@Composable
private fun FinancialRow(label: String, value: String, isRemaining: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isRemaining) Color(0xFFD32F2F) else Color.Black
        )
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun ActionButtonsRow(
    onAddPurchase: () -> Unit,
    onAddPayment: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onAddPurchase,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("إضافة شراء")
        }
        Button(
            onClick = onAddPayment,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("إضافة دفعة")
        }
    }
}

@Composable
private fun PurchasesSection(
    purchases: List<SupplierPurchase>,
    onDelete: (SupplierPurchase) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
        ) {
            Text("سجل المشتريات", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            for (purchase in purchases) {
                PurchaseItem(purchase, onDelete)
            }
        }
    }
}

@Composable
private fun PurchaseItem(purchase: SupplierPurchase, onDelete: (SupplierPurchase) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDelete(purchase) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = purchase.purchaseDate, fontSize = 12.sp, color = Color.Gray)
            Text(text = "%.2f ر.ي".format(purchase.totalAmount), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PaymentsSection(
    payments: List<SupplierPayment>,
    onDelete: (SupplierPayment) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
        ) {
            Text("سجل المدفوعات", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            for (payment in payments) {
                PaymentItem(payment, onDelete)
            }
        }
    }
}

@Composable
private fun PaymentItem(payment: SupplierPayment, onDelete: (SupplierPayment) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDelete(payment) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = payment.paymentDate, fontSize = 12.sp, color = Color.Gray)
            Text(text = "%.2f ر.ي".format(payment.amount), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AddPurchaseDialog(
    supplierName: String,
    onDismiss: () -> Unit,
    onConfirm: (List<com.example.data.PurchaseItem>, String) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val items = remember { mutableStateOf<List<com.example.data.PurchaseItem>>(emptyList()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
            ) {
                Text("إضافة عملية شراء", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("اسم الصنف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("الكمية") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text("سعر الوحدة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
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
                            if (itemName.isNotEmpty() && quantity.isNotEmpty() && unitPrice.isNotEmpty()) {
                                val qty = quantity.toDoubleOrNull() ?: 0.0
                                val price = unitPrice.toDoubleOrNull() ?: 0.0
                                val item = com.example.data.PurchaseItem(
                                    name = itemName,
                                    quantity = qty,
                                    unitPrice = price,
                                    total = qty * price,
                                    notes = notes
                                )
                                items.value = items.value + item
                                onConfirm(items.value, notes)
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

@Composable
private fun AddPaymentDialog(
    supplierName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
            ) {
                Text("إضافة دفعة", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ (ر.ي)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
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
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null && amountValue > 0) {
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
