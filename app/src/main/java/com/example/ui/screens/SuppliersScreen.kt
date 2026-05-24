package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.SupplierViewModel
import com.example.data.Supplier
import com.example.data.SupplierStatistics

@Composable
fun SuppliersScreen(
    viewModel: SupplierViewModel,
    onSupplierSelected: (Supplier) -> Unit
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val statistics by viewModel.supplierStatistics.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var supplierToDelete by remember { mutableStateOf<Supplier?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSuppliers()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
        ) {
            // Header
            HeaderSection()

            // Statistics Cards
            StatisticsCards(suppliers, statistics)

            // Search Bar
            SearchBar(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.searchQuery(it) },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            )

            // Suppliers List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(suppliers) { supplier ->
                    SupplierCard(
                        supplier = supplier,
                        statistics = statistics[supplier.id],
                        onSupplierClick = { onSupplierSelected(supplier) },
                        onEditClick = { /* TODO: Edit implementation */ },
                        onDeleteClick = {
                            supplierToDelete = supplier
                            showDeleteConfirmation = true
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
                                "لا توجد موردين",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
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

    // Error Message
    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            // Show snackbar or dialog
            viewModel.clearError()
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddSupplierDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, governorate, qatType, notes ->
                viewModel.addSupplier(name, phone, governorate, qatType, notes)
                showAddDialog = false
            }
        )
    }

    if (showDeleteConfirmation && supplierToDelete != null) {
        DeleteConfirmationDialog(
            supplierName = supplierToDelete!!.name,
            onConfirm = {
                viewModel.deleteSupplier(supplierToDelete!!)
                showDeleteConfirmation = false
                supplierToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                supplierToDelete = null
            }
        )
    }
}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF009639))
            .padding(16.dp)
            .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
    ) {
        Text(
            text = "قسم الموردين",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Composable
private fun StatisticsCards(
    suppliers: List<Supplier>,
    statistics: Map<String, SupplierStatistics>
) {
    val totalSuppliers = suppliers.size
    val totalPurchases = statistics.values.sumOf { it.totalPurchases }
    val totalPaid = statistics.values.sumOf { it.totalPaid }
    val remainingBalance = statistics.values.sumOf { it.remainingBalance }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        LazyColumn {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard(
                        title = "عدد الموردين",
                        value = totalSuppliers.toString(),
                        color = Color(0xFFE3F2FD)
                    )
                    StatisticCard(
                        title = "إجمالي المشتريات",
                        value = "%.2f ر.ي".format(totalPurchases),
                        color = Color(0xFFF3E5F5)
                    )
                    StatisticCard(
                        title = "إجمالي المسدد",
                        value = "%.2f ر.ي".format(totalPaid),
                        color = Color(0xFFF1F8E9)
                    )
                    StatisticCard(
                        title = "إجمالي المستحقات",
                        value = "%.2f ر.ي".format(remainingBalance),
                        color = Color(0xFFFFEBEE)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.End
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = modifier
            .fillMaxWidth()
            .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl),
        placeholder = { Text("البحث عن مورد") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun SupplierCard(
    supplier: Supplier,
    statistics: SupplierStatistics?,
    onSupplierClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSupplierClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .layoutDirection(androidx.compose.ui.unit.LayoutDirection.Rtl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = supplier.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "الهاتف: ${supplier.phone}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF009639))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (statistics != null) {
                    StatisticItem("المشتريات", "%.2f ر.ي".format(statistics.totalPurchases))
                    StatisticItem("المسدد", "%.2f ر.ي".format(statistics.totalPaid))
                    StatisticItem("المتبقي", "%.2f ر.ي".format(statistics.remainingBalance))
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
private fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var governorate by remember { mutableStateOf("") }
    var qatType by remember { mutableStateOf("") }
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
                Text("إضافة مورد جديد", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المورد") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = governorate,
                    onValueChange = { governorate = it },
                    label = { Text("المحافظة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = qatType,
                    onValueChange = { qatType = it },
                    label = { Text("نوع القات") },
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
                        onClick = { onConfirm(name, phone, governorate, qatType, notes) },
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
private fun DeleteConfirmationDialog(
    supplierName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تأكيد الحذف") },
        text = { Text("هل أنت متأكد من حذف المورد: $supplierName؟") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("حذف")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
