package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.AppViewModel
import com.example.data.*
import com.example.ui.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- State collections ---
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val settingsState by viewModel.settings.collectAsState()

    val inventoryList by viewModel.inventoryFlow.collectAsState()
    val salesList by viewModel.salesFlow.collectAsState()
    val customersList by viewModel.customersFlow.collectAsState()
    val debtTxList by viewModel.debtTransactionsFlow.collectAsState()
    val suppliersList by viewModel.suppliersFlow.collectAsState()
    val supplierTxList by viewModel.supplierTransactionsFlow.collectAsState()
    val expensesList by viewModel.expensesFlow.collectAsState()
    val transfersList by viewModel.transfersFlow.collectAsState()
    val archivesList by viewModel.archivesFlow.collectAsState()
    val backupsList by viewModel.backupsFlow.collectAsState()
    val printLogsList by viewModel.printLogsFlow.collectAsState()

    // --- App theme colors ---
    val darkBackground = Color(0xFF101311)
    val cardBackground = Color(0xFF181C19)
    val emeraldAccent = Color(0xFF009639)
    val redAccent = Color(0xFFE4312B)

    // --- Floating Receipt Preview Dialogue ---
    val queueReceipt by viewModel.activeReceiptPreview.collectAsState()

    // --- Dialog triggers ---
    var showPrinterSetup by remember { mutableStateOf(false) }

    // Swipe gesture variables
    var totalDrag by remember { mutableStateOf(0f) }

    if (!isLoggedIn) {
        // LOGIN SCREEN
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loginError by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header logo & Palestinian banner
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color.White, RoundedCornerShape(70.dp))
                        .padding(2.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "وكالة طوفان الأقصى لأجود أنواع القات الصعدي",
                    color = emeraldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "صاحبها / أحمد منصور",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                PalestinianFlagBanner()
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تسجيل الدخول",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("اسم المستخدم") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = emeraldAccent) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = emeraldAccent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = emeraldAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("كلمة المرور") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = emeraldAccent) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = emeraldAccent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = emeraldAccent
                            )
                        )

                        if (loginError) {
                            Text(
                                "بيانات الدخول غير صحيحة!",
                                color = redAccent,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (viewModel.login(username, password)) {
                                    loginError = false
                                    Toast.makeText(context, "أهلاً بك بكامل الدعم والسرعة", Toast.LENGTH_SHORT).show()
                                } else {
                                    loginError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent)
                        ) {
                            Text("دخول", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                username = "admin"
                                password = "123456"
                                if (viewModel.login("admin", "123456")) {
                                    loginError = false
                                    Toast.makeText(context, "تم الدخول التلقائي للتجربة والتحكم", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("المس هنا للتجربة الفورية والدخول التلقائي", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    } else {
        // MAIN COCONUT APPS DASHBOARD WITH TAB LAYOUT
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Text(
                                text = "وكالة طوفان الأقصى لأجود أنواع القات الصعدي",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "صاحبها / أحمد منصور",
                                fontSize = 11.sp,
                                color = emeraldAccent,
                                fontWeight = FontWeight.Light
                            )
                        }
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.padding(start = 8.dp)) {
                            PalestineFlag(
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(26.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBackground)
                )
            },
            bottomBar = {
                // FIXED Horizontal double-row / scrollable Bottom Navigation containing 11 sections
                Column(modifier = Modifier.background(cardBackground).navigationBarsPadding()) {
                    ScrollableTabRow(
                        selectedTabIndex = activeTab,
                        containerColor = cardBackground,
                        contentColor = emeraldAccent,
                        edgePadding = 12.dp
                    ) {
                        TAB_SECTIONS_META.forEachIndexed { idx, meta ->
                            Tab(
                                selected = activeTab == idx,
                                onClick = { viewModel.setTab(idx) },
                                text = { Text(meta.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                icon = { Icon(imageVector = meta.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                selectedContentColor = emeraldAccent,
                                unselectedContentColor = Color.LightGray
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            // GESTURE NAVIGATION AND SCREEN CONTENT ROUTER WITH HORIZONTAL ANIMATED SWIPING
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(darkBackground)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount.x
                            },
                            onDragEnd = {
                                if (totalDrag > 150f) {
                                    // Swipe Right -> previous (RTL orientation: previous is next index or vice versa)
                                    viewModel.prevTab()
                                } else if (totalDrag < -150f) {
                                    // Swipe Left -> next
                                    viewModel.nextTab()
                                }
                                totalDrag = 0f
                            }
                        )
                    }
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    }
                ) { targetTabIndex ->
                    when (targetTabIndex) {
                        0 -> DashboardScreen(viewModel, inventoryList, salesList, debtTxList, suppliersList, expensesList, transfersList)
                        1 -> InventoryScreen(viewModel, inventoryList, settingsState)
                        2 -> SalesScreen(viewModel, salesList, inventoryList)
                        3 -> AccountsScreen(viewModel, inventoryList, salesList, expensesList, settingsState)
                        4 -> DebtsScreen(viewModel, customersList, debtTxList, settingsState)
                        5 -> SuppliersScreenEnhanced(viewModel, suppliersList, supplierTxList, settingsState)
                        6 -> ExpensesScreen(viewModel, expensesList)
                        7 -> TransfersScreen(viewModel, transfersList)
                        8 -> ReportsScreen(viewModel, inventoryList, salesList, expensesList, debtTxList, suppliersList, transfersList, settingsState)
                        9 -> ArchivesScreen(viewModel, archivesList)
                        10 -> SettingsScreen(viewModel, settingsState, backupsList)
                    }
                }
            }
        }
    }

    // --- Floating Thermal receipt preview layer ---
    queueReceipt?.let { receipt ->
        ThermalReceiptPreviewDialog(
            title = receipt.title,
            contentLines = receipt.lines,
            totalAmount = receipt.total,
            settings = settingsState,
            onConfirmPrint = {
                viewModel.logPrint(true, receipt.category, "تمت الطباعة بنجاح")
                Toast.makeText(context, "جاري إرسال البيانات للطابعة الحرارية...", Toast.LENGTH_SHORT).show()
                viewModel.showReceipt(null)
            },
            onDismiss = { viewModel.showReceipt(null) }
        )
    }
}

// -------------------------------------------------------------
// --- SUB SCREENS ---
// -------------------------------------------------------------

@Composable
fun DashboardScreen(
    appViewModel: AppViewModel,
    inventory: List<QatInventory>,
    sales: List<QatSale>,
    debts: List<DebtTransaction>,
    suppliers: List<Supplier>,
    expenses: List<Expense>,
    transfers: List<FinancialTransfer>
) {
    val totalInventoryValue = inventory.sumOf { it.quantity * it.unitPrice }
    val totalSales = sales.sumOf { it.quantity * it.sellingPrice }
    val totalExpenses = expenses.sumOf { it.amount }
    val totalTransfers = transfers.sumOf { it.amount }
    val totalAddedDebts = debts.filter { it.type == "دين" }.sumOf { it.amount }
    val totalPaidDebts = debts.filter { it.type == "سداد" }.sumOf { it.amount }
    val netDebts = totalAddedDebts - totalPaidDebts

    // Live profits and loss calculation
    var estimatedCost = 0.0
    sales.forEach { sale ->
        val matchedStock = inventory.filter { it.qatType.equals(sale.qatType, ignoreCase = true) }
        val cost = if (matchedStock.isNotEmpty()) matchedStock.map { it.unitPrice }.average() else sale.sellingPrice * 0.7
        estimatedCost += sale.quantity * cost
    }
    val profitResult = totalSales - estimatedCost - totalExpenses
    val netProfit = if (profitResult > 0.0) profitResult else 0.0
    val netLoss = if (profitResult < 0.0) -profitResult else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Elegant Welcome banner with animated flag
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PalestinianFlagBanner()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "مرحباً بك في النظام المحاسبي المتكامل",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "التاريخ: ${appViewModel.getCurrentDateString()} | الوقت: ${appViewModel.getCurrentTimeString()}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            Text(
                "لوحة الإحصائيات التشغيلية والمالية الحالية:",
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Stats Grid built beautifully
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "قيمة المخزون",
                        value = "%.2f ر.ي".format(totalInventoryValue),
                        icon = Icons.Default.Inventory2,
                        color = Color(0xFF009639)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "المبيعات",
                        value = "%.2f ر.ي".format(totalSales),
                        icon = Icons.Default.MonetizationOn,
                        color = Color(0xFF009639)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "الأرباح الصافية",
                        value = "%.2f ر.ي".format(netProfit),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF009639)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "الخسائر",
                        value = "%.2f ر.ي".format(netLoss),
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFE4312B)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "الديون المستحقة",
                        value = "%.2f ر.ي".format(netDebts),
                        icon = Icons.Default.CreditCard,
                        color = Color(0xFFE4312B)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "التحويلات المالية",
                        value = "%.2f ر.ي".format(totalTransfers),
                        icon = Icons.Default.Send,
                        color = Color(0xFF009639)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "الموردين النشطين",
                        value = "${suppliers.size}",
                        icon = Icons.Default.Group,
                        color = Color(0xFF009639)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "أصناف القات المخزنة",
                        value = "${inventory.map { it.qatType }.distinct().size}",
                        icon = Icons.Default.LocalMall,
                        color = Color(0xFF009639)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Right)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -------------------------------------------------------------
// --- TABS DETAILED ---
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(appViewModel: AppViewModel, list: List<QatInventory>, settings: AppSettings) {
    val context = LocalContext.current
    val dailyMetasList by appViewModel.dailyFinanceMetaFlow.collectAsState()
    val todayDate = appViewModel.getCurrentDateString()
    val todayMeta = dailyMetasList.find { it.date == todayDate } ?: DailyFinanceMeta(date = todayDate)

    // Form states
    var qatName by remember { mutableStateOf("") }
    var qatKinf by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var unitPriceStr by remember { mutableStateOf("") }
    var itemNotes by remember { mutableStateOf("") }

    var editingItem by remember { mutableStateOf<QatInventory?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<QatInventory?>(null) }
    var viewDetailsItem by remember { mutableStateOf<QatInventory?>(null) }

    // Daily Tax & Expenses states
    var dailyTaxStr by remember { mutableStateOf("") }
    var outflowMaktabStr by remember { mutableStateOf("") }
    var outflowWorkersStr by remember { mutableStateOf("") }
    var dailyNotes by remember { mutableStateOf("") }

    // Sync input fields with editing item
    LaunchedEffect(editingItem) {
        val item = editingItem
        if (item != null) {
            val parts = item.qatType.split(" | ملاحظات: ")
            val rawType = parts.getOrNull(0) ?: item.qatType
            itemNotes = parts.getOrNull(1) ?: ""

            val typeParts = rawType.split(" (")
            qatName = typeParts.getOrNull(0) ?: rawType
            qatKinf = typeParts.getOrNull(1)?.replace(")", "") ?: ""

            quantityStr = item.quantity.toString()
            unitPriceStr = item.unitPrice.toString()
        } else {
            qatName = ""
            qatKinf = ""
            quantityStr = ""
            unitPriceStr = ""
            itemNotes = ""
        }
    }

    // Sync daily finance inputs when database entity for today is fetched
    LaunchedEffect(todayMeta) {
        dailyTaxStr = if (todayMeta.qatTax > 0.0) todayMeta.qatTax.toString() else ""
        outflowMaktabStr = if (todayMeta.outflowMaktab > 0.0) todayMeta.outflowMaktab.toString() else ""
        outflowWorkersStr = if (todayMeta.outflowWorkers > 0.0) todayMeta.outflowWorkers.toString() else ""
        dailyNotes = todayMeta.notes
    }

    // Calculations
    val totalInventoryValue = list.sumOf { it.quantity * it.unitPrice }
    val totalItemsCount = list.size
    val totalQuantitySum = list.sumOf { it.quantity }
    val lowStockCount = list.filter { it.quantity < 5.0 }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Static Headers ---
        item {
            Text(
                "إدارة مخزون القات الصعدي:",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        // --- STATS GRID ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "قيمة المخزون الجاري",
                        value = "%.2f ر.ي".format(totalInventoryValue),
                        icon = Icons.Default.Inventory2,
                        color = Color(0xFF009639)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي كمية الشحنات",
                        value = "%.2f وحدة".format(totalQuantitySum),
                        icon = Icons.Default.ProductionQuantityLimits,
                        color = Color(0xFF009639)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "عدد الأصناف المقيدة",
                        value = "$totalItemsCount صنف",
                        icon = Icons.Default.FormatListNumberedRtl,
                        color = Color(0xFF009639)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "الأصناف منخفضة الكمية",
                        value = "$lowStockCount أصناف",
                        icon = Icons.Default.Warning,
                        color = if (lowStockCount > 0) Color(0xFFE4312B) else Color(0xFF009639)
                    )
                }
            }
        }

        // --- 1. ITEM ADDITION FORM ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        if (editingItem != null) "تعديل صنف قيد" else "إضافة شحنة جديدة للمخزن",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF009639)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = qatName,
                        onValueChange = { qatName = it },
                        label = { Text("اسم الصنف (مثال: الهمدي الوردي)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = qatKinf,
                        onValueChange = { qatKinf = it },
                        label = { Text("نوع القات (مثال: صعدي سوبر)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("الكمية المتوفرة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("سعر الوحدة للشراء") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = itemNotes,
                        onValueChange = { itemNotes = it },
                        label = { Text("ملاحظات الشحنة") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "تاريخ الإضافة التلقائي: $todayDate",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (editingItem != null) {
                            TextButton(
                                onClick = { editingItem = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء التعديل")
                            }
                        }

                        Button(
                            onClick = {
                                val qValue = quantityStr.toDoubleOrNull() ?: 0.0
                                val pValue = unitPriceStr.toDoubleOrNull() ?: 0.0

                                if (qatName.isBlank() || qatKinf.isBlank() || qValue <= 0.0 || pValue <= 0.0) {
                                    Toast.makeText(context, "الرجاء تعبئة جميع الحقول بشكل صحيح!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Combine fields dynamically to fit into qatType of DB schema
                                val combinedType = if (itemNotes.isNotBlank()) {
                                    "${qatName.trim()} (${qatKinf.trim()}) | ملاحظات: ${itemNotes.trim()}"
                                } else {
                                    "${qatName.trim()} (${qatKinf.trim()})"
                                }

                                val editing = editingItem
                                if (editing != null) {
                                    appViewModel.updateInventory(
                                        editing.copy(
                                            qatType = combinedType,
                                            quantity = qValue,
                                            unitPrice = pValue
                                        )
                                    )
                                    editingItem = null
                                    Toast.makeText(context, "تم تعديل بيانات الشحنة بنجاح", Toast.LENGTH_SHORT).show()
                                } else {
                                    appViewModel.addInventory(combinedType, qValue, pValue, todayMeta.qatTax)
                                    Toast.makeText(context, "تم حفظ الشحنة الجديدة بنجاح بالمخازن", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (editingItem != null) "تعديل" else "حفظ الشحنة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 2. DAILY FINANCE SYSTEM FORM ("مصروفات اليوم") ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "مصروفات اليوم وضريبة القات اليومية",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF009639)
                    )
                    Text(
                        "تدخل قيم المحاسبة مرة واحدة فقط لكل تاريخ يومي.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = dailyTaxStr,
                        onValueChange = { dailyTaxStr = it },
                        label = { Text("ضريبة القات اليومية المعتمدة (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = outflowMaktabStr,
                        onValueChange = { outflowMaktabStr = it },
                        label = { Text("خرج المفرش اليومي (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(value = outflowWorkersStr,
                        onValueChange = { outflowWorkersStr = it },
                        label = { Text("خرج العمال ومساعدتهم المباشرة اليومية (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dailyNotes,
                        onValueChange = { dailyNotes = it },
                        label = { Text("ملاحظات الحساب والخرج الصباحي لليوم") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val taxVal = dailyTaxStr.toDoubleOrNull() ?: 0.0
                            val maktabVal = outflowMaktabStr.toDoubleOrNull() ?: 0.0
                            val workersVal = outflowWorkersStr.toDoubleOrNull() ?: 0.0

                            appViewModel.saveDailyFinanceMeta(taxVal, maktabVal, workersVal, dailyNotes)
                            Toast.makeText(context, "تم حفظ ضريبة اليوم ومصاريف المفرش والعمال بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ الحساب الضريبي والخرج اليومي", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 3. INVENTORY RECORDS TABLE ---
        item {
            Text(
                "سجلات الشحن والوارد المتوفر الجاري:",
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        if (list.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))) {
                    Text("لا توجد كميات أو أصناف مسجلة في المخزون حالياً", modifier = Modifier.padding(16.dp).fillMaxWidth(), color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        items(list) { item ->
            // Extract display values cleanly
            val parts = item.qatType.split(" | ملاحظات: ")
            val rawType = parts.getOrNull(0) ?: item.qatType
            val displayNotes = parts.getOrNull(1) ?: "لا يوجد ملاحظات"

            val typeParts = rawType.split(" (")
            val displayName = typeParts.getOrNull(0) ?: rawType
            val displayKind = typeParts.getOrNull(1)?.replace(")", "") ?: "غير محدد"

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "%.2f ر.ي".format(item.quantity * item.unitPrice),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF009639)
                        )
                        Text(displayName, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("نوع القات: $displayKind | الكمية المتوفرة: ${item.quantity} | السعر: ${item.unitPrice}", fontSize = 12.sp, color = Color.Gray)
                    Text("الضريبة: ${item.qatTax} ر.ي | تاريخ الشحن: ${item.date}", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 5 Action buttons: View, Share, Print, PDF, Edit, Delete
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // 1. Details view dialog ("عرض")
                            IconButton(onClick = { viewDetailsItem = item }) {
                                Icon(Icons.Default.Visibility, contentDescription = "عرض التفاصيل", tint = Color.LightGray)
                            }

                            // 2. Thermal Receipts preview ("طباعة")
                            IconButton(onClick = {
                                appViewModel.showReceipt(
                                    AppViewModel.ReceiptData(
                                        title = "سند مخزون - صنف $displayName",
                                        lines = listOf(
                                            "اسم الصنف" to displayName,
                                            "نوع القات" to displayKind,
                                            "الكمية الواردة" to item.quantity.toString(),
                                            "سعر الوحدة" to "${item.unitPrice} ر.ي",
                                            "الضريبة المحتسبة" to "${item.qatTax} ر.ي",
                                            "التاريخ" to item.date,
                                            "ملاحظات" to displayNotes
                                        ),
                                        total = item.quantity * item.unitPrice,
                                        category = "مخزون"
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Print, contentDescription = "طباعة حرارية", tint = Color.LightGray)
                            }

                            // 3. Share text ("مشاركة")
                            IconButton(onClick = {
                                PdfGenerator.shareViaText(
                                    context,
                                    "سند توريد مخزن: $displayName",
                                    listOf(
                                        "اسم الصنف" to displayName,
                                        "نوع القات" to displayKind,
                                        "الكمية" to item.quantity.toString(),
                                        "السعر لشراء" to "${item.unitPrice} ر.ي",
                                        "الضريبة الموزعة" to "${item.qatTax} ر.ي",
                                        "التفاصيل" to displayNotes
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "مشاركة النص", tint = Color.LightGray)
                            }

                            // 4. PDF download/share ("PDF")
                            IconButton(onClick = {
                                PdfGenerator.generateAndSharePdf(
                                    context = context,
                                    reportTitle = "كشف شحنة مخازن - $displayName",
                                    headers = listOf("المجموع", "الضريبة", "السعر", "الكمية", "الصنف والبيان"),
                                    rows = listOf(
                                        listOf(
                                            "%.2f".format(item.quantity * item.unitPrice),
                                            item.qatTax.toString(),
                                            item.unitPrice.toString(),
                                            item.quantity.toString(),
                                            "$displayName ($displayKind)"
                                        )
                                    )
                                )
                            }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "كشف كقوات PDF", tint = Color.LightGray)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // 5. Edit ("تعديل")
                            IconButton(onClick = { editingItem = item }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل الشحنة", tint = Color(0xFF009639))
                            }
                            // 6. Delete with confirmation dialog ("حذف")
                            IconButton(onClick = { showDeleteConfirm = item }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الشحنة", tint = Color(0xFFE4312B))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- A. DELETE CONFIRMATION DIALOG ---
    showDeleteConfirm?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("تأكيد الحذف", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من حذف هذا العنصر؟", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    appViewModel.deleteInventory(item)
                    showDeleteConfirm = null
                    Toast.makeText(context, "تم حذف الكمية من السجلات", Toast.LENGTH_SHORT).show()
                }) { Text("حذف", color = Color(0xFFE4312B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("إلغاء") }
            }
        )
    }

    // --- B. DETAILS INFO PREVIEW DIALOG ---
    viewDetailsItem?.let { item ->
        val parts = item.qatType.split(" | ملاحظات: ")
        val rawType = parts.getOrNull(0) ?: item.qatType
        val notesText = parts.getOrNull(1) ?: "لا توجد ملاحظات تفصيلية مسجلة لهذه الشحنة"
        val typeParts = rawType.split(" (")
        val displayName = typeParts.getOrNull(0) ?: rawType
        val displayKind = typeParts.getOrNull(1)?.replace(")", "") ?: "غير محدد"

        AlertDialog(
            onDismissRequest = { viewDetailsItem = null },
            title = { Text("تفاصيل الشحنة المخزونة", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("اسم الصنف: $displayName", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("نوع القات: $displayKind", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("الكمية المتوفرة حالياً: ${item.quantity} وحدة", color = Color.White)
                    Text("سعر شراء الوحدة: ${item.unitPrice} ر.ي", color = Color.LightGray)
                    Text("الضريبة المدفوعة: ${item.qatTax} ر.ي", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("قيمة الشحنة الإجمالية: " + "%.2f ر.ي".format(item.quantity * item.unitPrice), color = Color(0xFF009639), fontWeight = FontWeight.Bold)
                    Text("تاريخ قيد الشحنة: ${item.date}", color = Color.LightGray)
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.Gray)
                    Text("ملاحظات:", fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(notesText, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth(), color = Color.LightGray)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewDetailsItem = null }) { Text("إغلاق") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(appViewModel: AppViewModel, list: List<QatSale>, inventory: List<QatInventory>) {
    val context = LocalContext.current
    var qatType by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var isCash by remember { mutableStateOf(true) }
    var customerName by remember { mutableStateOf("") }

    // Dropdown selection state
    var expandedDropdown by remember { mutableStateOf(false) }

    // Dialog state
    var showDeleteConfirm by remember { mutableStateOf<QatSale?>(null) }
    var viewDetailsItem by remember { mutableStateOf<QatSale?>(null) }

    // Compute stats
    val todayDate = appViewModel.getCurrentDateString()
    val currentMonth = todayDate.substring(0, 7) // YYYY-MM
    val todaySalesList = list.filter { it.date == todayDate }
    val monthlySalesList = list.filter { it.date.startsWith(currentMonth) }

    val todaySalesSum = todaySalesList.sumOf { it.quantity * it.sellingPrice }
    val monthlySalesSum = monthlySalesList.sumOf { it.quantity * it.sellingPrice }
    val totalTransactionsCount = list.size

    val totalSalesProfit = list.sumOf { sale ->
        val matchedStock = inventory.filter { it.qatType.equals(sale.qatType, ignoreCase = true) }
        val cost = if (matchedStock.isNotEmpty()) matchedStock.map { it.unitPrice }.average() else sale.sellingPrice * 0.7
        (sale.sellingPrice - cost) * sale.quantity
    }

    // Selected item stock limits
    val availableStock = inventory.filter { it.qatType == qatType }.sumOf { it.quantity }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "إدارة مبيعات وكالة طوفان الأقصى:",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        // --- STATS CARDS GRID ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "مبيعات اليوم الجاري",
                        value = "%.2f ر.ي".format(todaySalesSum),
                        icon = Icons.Default.MonetizationOn,
                        color = Color(0xFF009639)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "مبيعات الشهر الحالي",
                        value = "%.2f ر.ي".format(monthlySalesSum),
                        icon = Icons.Default.CalendarMonth,
                        color = Color(0xFF009639)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "عدد صفقات البيع",
                        value = "$totalTransactionsCount فاتورة",
                        icon = Icons.Default.Receipt,
                        color = Color(0xFF009639)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي الأرباح المقدرة",
                        value = "%.2f ر.ي".format(totalSalesProfit),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF009639)
                    )
                }
            }
        }

        // --- SALE ENTRY FORM ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("سند بيع فوري جديد", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dropdown for selectable Items
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = qatType,
                            onValueChange = { qatType = it },
                            label = { Text("اختر الصنف من المخزن") },
                            trailingIcon = {
                                IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            inventory.map { it.qatType }.distinct().forEach { type ->
                                val parts = type.split(" | ملاحظات: ")
                                val rawType = parts.getOrNull(0) ?: type
                                val typeParts = rawType.split(" (")
                                val displayName = typeParts.getOrNull(0) ?: rawType

                                DropdownMenuItem(
                                    text = { Text(displayName, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                                    onClick = {
                                        qatType = type
                                        expandedDropdown = false
                                        // Auto fill estimated selling price (30% margin)
                                        val match = inventory.firstOrNull { it.qatType == type }
                                        if (match != null) {
                                            priceStr = (match.unitPrice * 1.3).toString()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (qatType.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الكمية المتاحة حالياً بالمخزن: $availableStock وحدة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (availableStock > 0.0) Color(0xFF009639) else Color(0xFFE4312B),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("الكمية المباعة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("سعر البيع المتفق للوحدة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Cash or Debt selection
                    Text(
                        "طريقة تحصيل الثمن:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isCash = false }
                        ) {
                            RadioButton(selected = !isCash, onClick = { isCash = false })
                            Text("آجل (ديون على عميل)")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isCash = true }
                        ) {
                            RadioButton(selected = isCash, onClick = { isCash = true })
                            Text("نقد (كاش)")
                        }
                    }

                    if (!isCash) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("اسم العميل المدين بوضوح") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val qValue = quantityStr.toDoubleOrNull() ?: 0.0
                            val pValue = priceStr.toDoubleOrNull() ?: 0.0

                            if (qatType.isBlank() || qValue <= 0.0 || pValue <= 0.0) {
                                Toast.makeText(context, "الرجاء مراجعة بيانات تعبئة الفاتورة!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (!isCash && customerName.isBlank()) {
                                Toast.makeText(context, "للفواتير الآجلة، يجب تحديد اسم العميل!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Prevent selling more than available stock limit
                            if (qValue > availableStock) {
                                Toast.makeText(context, "الكمية المطلوبة ($qValue) أكبر من المتوفرة في المخزن ($availableStock)!", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            // Perform sale insertion (this decreases stock and adds to sales)
                            appViewModel.addSale(qatType, qValue, pValue, isCash, if (isCash) "نقد" else customerName)
                            Toast.makeText(context, "تم حفظ عملية البيع وخصم المخزون والضرائب بنجاح", Toast.LENGTH_SHORT).show()

                            // Reset
                            qatType = ""
                            quantityStr = ""
                            priceStr = ""
                            customerName = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ الفاتورة والبدء بمحاسبتها", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SALES HISTORIC RECORDS LIST ---
        item {
            Text(
                "جدول سجل المبيعات والتحصيلات الكلية:",
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        if (list.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))) {
                    Text("لا توجد فواتير مبيعات سابقة مسجلة اليوم", modifier = Modifier.padding(16.dp).fillMaxWidth(), color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        items(list) { sale ->
            // Extract display titles from layout
            val parts = sale.qatType.split(" | ملاحظات: ")
            val rawType = parts.getOrNull(0) ?: sale.qatType
            val typeParts = rawType.split(" (")
            val displayName = typeParts.getOrNull(0) ?: rawType
            val displayKind = typeParts.getOrNull(1)?.replace(")", "") ?: "غير محدد"

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "%.2f ر.ي".format(sale.quantity * sale.sellingPrice),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF009639)
                        )
                        Text("مبيعات $displayName (#${sale.id})", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("النوع: $displayKind | الكمية المباعة: ${sale.quantity} | سعر الوحدة: ${sale.sellingPrice}", fontSize = 12.sp, color = Color.Gray)
                    Text("الدفع: ${if (sale.isCash) "كاش (نقدي)" else "آجل للعميل: ${sale.customerName}"} | تاريخ الصفقة: ${sale.date}", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // 1. Details Dialog ("عرض")
                            IconButton(onClick = { viewDetailsItem = sale }) {
                                Icon(Icons.Default.Visibility, contentDescription = "عرض تفاصيل البيع", tint = Color.LightGray)
                            }

                            // 2. Thermal Receipt ("طباعة حرارية")
                            IconButton(onClick = {
                                appViewModel.showReceipt(
                                    AppViewModel.ReceiptData(
                                        title = "فاتورة بيع صنف $displayName",
                                        lines = listOf(
                                            "الحركة رقم" to sale.id.toString(),
                                            "اسم الصنف" to displayName,
                                            "نوع القات" to displayKind,
                                            "الكمية المبيعة" to sale.quantity.toString(),
                                            "سعر البيع" to "${sale.sellingPrice} ر.ي",
                                            "الحالة والعميل" to if (sale.isCash) "نقدي" else "آجل لـ ${sale.customerName}",
                                            "تاريخ الفاتورة" to sale.date
                                        ),
                                        total = sale.quantity * sale.sellingPrice,
                                        category = "مبيعات"
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Print, contentDescription = "طباعة الفاتورة", tint = Color.LightGray)
                            }

                            // 3. Share PDF ("pdf")
                            IconButton(onClick = {
                                PdfGenerator.generateAndSharePdf(
                                    context = context,
                                    reportTitle = "فاتورة مبيعات وكالة طوفان الأقصى - $displayName",
                                    headers = listOf("المجموع", "طريقة السداد", "قيمة البيع", "الكمية", "صنف القات"),
                                    rows = listOf(
                                        listOf(
                                            "%.2f".format(sale.quantity * sale.sellingPrice),
                                            if (sale.isCash) "نقدي" else "آجل: " + sale.customerName,
                                            sale.sellingPrice.toString(),
                                            sale.quantity.toString(),
                                            "$displayName ($displayKind)"
                                        )
                                    )
                                )
                            }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "فاتورة كملف PDF", tint = Color.LightGray)
                            }

                            // 4. Share Text ("مشاركة")
                            IconButton(onClick = {
                                PdfGenerator.shareViaText(
                                    context,
                                    "سند بيع قات صعدي",
                                    listOf(
                                        "الحركة رقم" to sale.id.toString(),
                                        "صنف القات" to displayName,
                                        "النوع" to displayKind,
                                        "الكمية" to sale.quantity.toString(),
                                        "سعر المبيع" to "${sale.sellingPrice} ر.ي",
                                        "العميل والتحصيل" to if (sale.isCash) "كاش بنشاط" else "آجل لزبون: ${sale.customerName}"
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "مشاركة النص للفاتورة", tint = Color.LightGray)
                            }
                        }

                        // 5. Delete with confirmation dialog ("حذف")
                        IconButton(onClick = { showDeleteConfirm = sale }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف الفاتورة", tint = Color(0xFFE4312B))
                        }
                    }
                }
            }
        }
    }

    // --- A. DELETE CONFIRMATION DIALOG ---
    showDeleteConfirm?.let { sale ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("تأكيد الحذف", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من حذف هذا العنصر؟", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    appViewModel.deleteSale(sale)
                    showDeleteConfirm = null
                    Toast.makeText(context, "تم إلغاء وشطب قيد فاتورة المبيعات وإرجاع الكمية للمخازن", Toast.LENGTH_SHORT).show()
                }) { Text("حذف", color = Color(0xFFE4312B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("إلغاء") }
            }
        )
    }

    // --- B. DISPLAY DETAILS DIALOG ---
    viewDetailsItem?.let { sale ->
        val parts = sale.qatType.split(" | ملاحظات: ")
        val rawType = parts.getOrNull(0) ?: sale.qatType
        val typeParts = rawType.split(" (")
        val displayName = typeParts.getOrNull(0) ?: rawType
        val displayKind = typeParts.getOrNull(1)?.replace(")", "") ?: "غير محدد"

        AlertDialog(
            onDismissRequest = { viewDetailsItem = null },
            title = { Text("تفاصيل فاتورة المبيعات", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("الحركة رقم: #${sale.id}", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("اسم الصنف: $displayName", color = Color.White)
                    Text("نوع القات: $displayKind", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("الكمية المباعة: ${sale.quantity} وحدة", color = Color.White)
                    Text("سعر بيع الوحدة: ${sale.sellingPrice} ر.ي", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("المبلغ الإجمالي للفاتورة: " + "%.2f ر.ي".format(sale.quantity * sale.sellingPrice), color = Color(0xFF009639), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("طريقة التحصيل: ${if (sale.isCash) "كاش (نقدي)" else "آجل (دين على العميل)"}", color = Color.White)
                    if (!sale.isCash) {
                        Text("اسم العميل المدين: ${sale.customerName}", color = Color(0xFFE4312B), fontWeight = FontWeight.SemiBold)
                    }
                    Text("تاريخ المعاملة والصفقة: ${sale.date}", color = Color.LightGray)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewDetailsItem = null }) { Text("إغلاق") }
            }
        )
    }
}

// Unified Transaction model for the collective finances ledger
data class UnifiedTx(
    val date: String,
    val type: String, // "بيع", "شراء", "ديون", "مصروفات", "تحويل مالي"
    val description: String,
    val amount: Double,
    val status: String,
    val color: Color
)

@Composable
fun SimpleLineChart(salesData: List<Pair<String, Float>>, modifier: Modifier = Modifier) {
    if (salesData.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("لا توجد مبيعات كافية لعرض المنحنى البياني", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val maxAmount = remember(salesData) { (salesData.maxOfOrNull { it.second } ?: 100f).coerceAtLeast(100f) }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacingLeft = 60f
        val spacingBottom = 40f
        val graphWidth = width - spacingLeft
        val graphHeight = height - spacingBottom

        // Draw horizontal grid lines
        val gridCount = 3
        for (i in 0..gridCount) {
            val y = graphHeight * (1f - i.toFloat() / gridCount.toFloat())
            drawLine(
                color = Color(0xFF2C322E),
                start = Offset(spacingLeft, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            // Left label
            drawContext.canvas.nativeCanvas.drawText(
                "%.0f".format(maxAmount * i.toFloat() / gridCount.toFloat()),
                10f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 22f
                }
            )
        }

        // Compute points
        val points = salesData.mapIndexed { index, pair ->
            val x = spacingLeft + (index.toFloat() / (salesData.size - 1).coerceAtLeast(1).toFloat()) * graphWidth
            val y = graphHeight - (pair.second / maxAmount) * graphHeight
            Offset(x, y)
        }

        // Draw background gradient fill
        if (points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    val cpX = pPrev.x + (pCurr.x - pPrev.x) / 2
                    quadraticTo(cpX, pPrev.y, cpX, pCurr.y)
                    lineTo(pCurr.x, pCurr.y)
                }
                lineTo(points.last().x, graphHeight)
                lineTo(points.first().x, graphHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF009639).copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = graphHeight
                )
            )

            // Draw line
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    val cpX = pPrev.x + (pCurr.x - pPrev.x) / 2
                    quadraticTo(cpX, pPrev.y, cpX, pCurr.y)
                    lineTo(pCurr.x, pCurr.y)
                }
            }

            drawPath(
                path = linePath,
                color = Color(0xFF009639),
                style = Stroke(width = 4f)
            )
        }

        // Draw nodes
        points.forEachIndexed { i, pt ->
            drawCircle(
                color = Color(0xFF009639),
                radius = 8f,
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = pt
            )

            // Bottom date label
            val dateLabel = salesData[i].first.takeLast(5)
            drawContext.canvas.nativeCanvas.drawText(
                dateLabel,
                pt.x,
                height - 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 20f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun SimpleCompareBarChart(revenue: Float, expenses: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxAmount = maxOf(revenue, expenses, 100f)

        val spacing = 50f
        val startX1 = width * 0.3f
        val startX2 = width * 0.7f
        val barWidth = width * 0.25f

        val h1 = (revenue / maxAmount) * (height - spacing)
        val h2 = (expenses / maxAmount) * (height - spacing)

        // Revenue bar (Green)
        drawRoundRect(
            color = Color(0xFF009639),
            topLeft = Offset(startX1 - barWidth / 2f, height - h1 - 20f),
            size = Size(barWidth, h1),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )

        // Expense/Cost bar (Red)
        drawRoundRect(
            color = Color(0xFFE4312B),
            topLeft = Offset(startX2 - barWidth / 2f, height - h2 - 20f),
            size = Size(barWidth, h2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )

        // Values & Labels
        drawContext.canvas.nativeCanvas.drawText(
            "%.0f ر.ي".format(revenue),
            startX1,
            height - h1 - 30f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
        drawContext.canvas.nativeCanvas.drawText(
            "المبيعات والوارد",
            startX1,
            height,
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )

        drawContext.canvas.nativeCanvas.drawText(
            "%.0f ر.ي".format(expenses),
            startX2,
            height - h2 - 30f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
        drawContext.canvas.nativeCanvas.drawText(
            "الصرف والتكاليف",
            startX2,
            height,
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
    }
}

@Composable
fun ExpensesAllocationBar(
    expensesTable: Float,
    dailyTaxes: Float,
    outflowMaktab: Float,
    outflowWorkers: Float,
    modifier: Modifier = Modifier
) {
    val total = expensesTable + dailyTaxes + outflowMaktab + outflowWorkers
    if (total <= 0f) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("لا توجد نفقات أو مصروفات مسجلة لعرض مخطط التوزيع", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val pExp = expensesTable / total
    val pTax = dailyTaxes / total
    val pMak = outflowMaktab / total
    val pWor = outflowWorkers / total

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        ) {
            val width = size.width
            val height = size.height

            val wExp = width * pExp
            val wTax = width * pTax
            val wMak = width * pMak
            val wWor = width * pWor

            var currentX = 0f

            if (wExp > 0f) {
                drawRect(color = Color(0xFFD32F2F), topLeft = Offset(currentX, 0f), size = Size(wExp, height))
                currentX += wExp
            }
            if (wTax > 0f) {
                drawRect(color = Color(0xFF6200EE), topLeft = Offset(currentX, 0f), size = Size(wTax, height))
                currentX += wTax
            }
            if (wMak > 0f) {
                drawRect(color = Color(0xFFE91E63), topLeft = Offset(currentX, 0f), size = Size(wMak, height))
                currentX += wMak
            }
            if (wWor > 0f) {
                drawRect(color = Color(0xFF9C27B0), topLeft = Offset(currentX, 0f), size = Size(wWor, height))
            }
        }

        // Legends
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color(0xFFD32F2F), label = "نثريات", value = expensesTable)
            LegendItem(color = Color(0xFF6200EE), label = "ضرائب", value = dailyTaxes)
            LegendItem(color = Color(0xFFE91E63), label = "المفرش", value = outflowMaktab)
            LegendItem(color = Color(0xFF9C27B0), label = "العمال", value = outflowWorkers)
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text("$label: %.0f ر.ي".format(value), fontSize = 10.sp, color = Color.White)
    }
}

@Composable
fun StatGridCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A17)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF222825))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AccountsScreen(
    appViewModel: AppViewModel,
    inventory: List<QatInventory>,
    sales: List<QatSale>,
    expenses: List<Expense>,
    settings: AppSettings
) {
    val context = LocalContext.current
    val dailyMetasList by appViewModel.dailyFinanceMetaFlow.collectAsState(initial = emptyList())
    val debtTxs by appViewModel.debtTransactionsFlow.collectAsState(initial = emptyList())
    val transfersList by appViewModel.transfersFlow.collectAsState(initial = emptyList())

    // Filter and Period State
    var reportPeriod by remember { mutableStateOf("all") } // "all", "today", "month", "year"
    var searchQuery by remember { mutableStateOf("") }
    var operationFilter by remember { mutableStateOf("all") } // "all", "بيع", "شراء", "ديون", "مصروفات", "تحويل مالي"

    // Transaction Details state
    var viewTxDetail by remember { mutableStateOf<UnifiedTx?>(null) }

    // Date computation
    val todayDate = appViewModel.getCurrentDateString()
    val currentMonth = todayDate.substring(0, 7) // "yyyy-MM"
    val currentYear = todayDate.substring(0, 4)  // "yyyy"

    // Helper filter function
    fun isWithinPeriod(dateStr: String): Boolean {
        return when (reportPeriod) {
            "today" -> dateStr == todayDate
            "month" -> dateStr.startsWith(currentMonth)
            "year" -> dateStr.startsWith(currentYear)
            else -> true
        }
    }

    // Filter entries by date period
    val filteredSales = sales.filter { isWithinPeriod(it.date) }
    val filteredExp = expenses.filter { isWithinPeriod(it.date) }
    val filteredDailyMeta = dailyMetasList.filter { isWithinPeriod(it.date) }
    val filteredDebtTxs = debtTxs.filter { isWithinPeriod(it.date) }
    val filteredTransfers = transfersList.filter { isWithinPeriod(it.date) }
    val filteredInventory = inventory.filter { isWithinPeriod(it.date) }

    // --- Core Financial Calculations ---
    val periodTotalSales = filteredSales.sumOf { it.quantity * it.sellingPrice }

    // Estimated Cost of Goods Sold (Purchases Cost) based on batch prices
    var periodCostOfSold = 0.0
    filteredSales.forEach { sale ->
        val matchedStock = inventory.filter { it.qatType.equals(sale.qatType, ignoreCase = true) }
        val costPr = if (matchedStock.isNotEmpty()) matchedStock.map { it.unitPrice }.average() else sale.sellingPrice * 0.70
        periodCostOfSold += sale.quantity * costPr
    }

    // Current Remaining Stock Value (قيمة المخزون الحالي)
    val totalStockValue = inventory.sumOf { it.quantity * it.unitPrice }

    // Added Inventory value inside period (إجمالي المشتريات)
    val periodTotalPurchases = filteredInventory.sumOf { it.quantity * it.unitPrice }

    // General Expenses
    val periodExpensesTable = filteredExp.sumOf { it.amount }
    val periodDailyOutflows = filteredDailyMeta.sumOf { it.outflowMaktab + it.outflowWorkers }
    val periodTotalExpenses = periodExpensesTable + periodDailyOutflows

    // Taxes
    val periodTotalTaxes = filteredDailyMeta.sumOf { it.qatTax }

    // Direct Cash Balance evaluation:
    // (المبيعات - المشتريات - الضريبة - خرج المفرش - خرج العمال - المصروفات) = صافي الربح أو الخسارة
    val periodDirectProfit = periodTotalSales - periodCostOfSold - periodTotalTaxes - periodDailyOutflows - periodExpensesTable
    val isProfit = periodDirectProfit >= 0.0
    val finalProfit = if (isProfit) periodDirectProfit else 0.0
    val finalLoss = if (!isProfit) -periodDirectProfit else 0.0

    // Outstanding Debt (الديون المستحقة التراكمية لوكالة طوفان الأقصى)
    val totalOutstandingDebts = debtTxs.sumOf { if (it.type == "دين") it.amount else -it.amount }

    // --- Build Unified Activity Ledger ---
    val unifiedTransactions = remember(sales, inventory, debtTxs, expenses, transfersList, dailyMetasList) {
        val list = mutableListOf<UnifiedTx>()

        // 1. Sales
        sales.forEach { sale ->
            val parts = sale.qatType.split(" | ملاحظات: ")
            val displayName = parts.getOrNull(0) ?: sale.qatType
            list.add(
                UnifiedTx(
                    date = sale.date,
                    type = "بيع",
                    description = "فاتورة بيع صنف: $displayName",
                    amount = sale.quantity * sale.sellingPrice,
                    status = if (sale.isCash) "كاش (نقدي)" else "آجل لعميل: ${sale.customerName}",
                    color = Color(0xFF009639)
                )
            )
        }

        // 2. Purchases Stock additions
        inventory.forEach { item ->
            val parts = item.qatType.split(" | ملاحظات: ")
            val displayName = parts.getOrNull(0) ?: item.qatType
            list.add(
                UnifiedTx(
                    date = item.date,
                    type = "شراء",
                    description = "توريد شحنة مخازن صنف: $displayName",
                    amount = item.quantity * item.unitPrice,
                    status = "مكتمل في المخزون",
                    color = Color(0xFFE4312B)
                )
            )
        }

        // 3. Debt transactions
        debtTxs.forEach { dt ->
            if (dt.type == "سداد") {
                list.add(
                    UnifiedTx(
                        date = dt.date,
                        type = "ديون",
                        description = "تحصيل دفعة وسداد دين من العميل: ${dt.customerName} | ${dt.notes}",
                        amount = dt.amount,
                        status = "مقوض نقداً",
                        color = Color(0xFFFFA000)
                    )
                )
            } else if (dt.type == "دين" && !dt.notes.contains("فاتورة بيع آجل")) {
                list.add(
                    UnifiedTx(
                        date = dt.date,
                        type = "ديون",
                        description = "قيد دين مالي يدوي للعميل: ${dt.customerName} | ${dt.notes}",
                        amount = dt.amount,
                        status = "آجل متبقي",
                        color = Color(0xFFCD7F32)
                    )
                )
            }
        }

        // 4. Expenses
        expenses.forEach { exp ->
            list.add(
                UnifiedTx(
                    date = exp.date,
                    type = "مصروفات",
                    description = "نثريات تشغيل: ${exp.description} | ${exp.notes}",
                    amount = exp.amount,
                    status = "مصروف كاش",
                    color = Color(0xFFD32F2F)
                )
            )
        }

        // 5. Transfers
        transfersList.forEach { tr ->
            list.add(
                UnifiedTx(
                    date = tr.date,
                    type = "تحويل مالي",
                    description = "حوالة مالية مصدرة: ${tr.description} | مرسل من: ${tr.senderName}",
                    amount = tr.amount,
                    status = "بواسطة: ${tr.exchangeHouse}",
                    color = Color(0xFF00ACC1)
                )
            )
        }

        // 6. Outflows & Taxes metadata
        dailyMetasList.forEach { meta ->
            if (meta.qatTax > 0.0) {
                list.add(
                    UnifiedTx(
                        date = meta.date,
                        type = "مصروفات",
                        description = "ضريبة القات الصعدي اليومية المعتمدة",
                        amount = meta.qatTax,
                        status = "رسوم ضرائب",
                        color = Color(0xFF6200EE)
                    )
                )
            }
            if (meta.outflowMaktab > 0.0) {
                list.add(
                    UnifiedTx(
                        date = meta.date,
                        type = "مصروفات",
                        description = "خرج المفرش وعجز الأمانة المكتبي اليومي",
                        amount = meta.outflowMaktab,
                        status = "منصرف مكتب",
                        color = Color(0xFFE91E63)
                    )
                )
            }
            if (meta.outflowWorkers > 0.0) {
                list.add(
                    UnifiedTx(
                        date = meta.date,
                        type = "مصروفات",
                        description = "خرج عوائد ومستحقات العمال والمساعدين المباشرة",
                        amount = meta.outflowWorkers,
                        status = "أجور عمالية",
                        color = Color(0xFF9C27B0)
                    )
                )
            }
        }

        list.sortByDescending { it.date }
        list
    }

    // Filter unified ledger chronologically, by search text, and by activity filter
    val filteredUnifiedTransactions = unifiedTransactions.filter { tx ->
        val matchesPeriod = isWithinPeriod(tx.date)
        val matchesType = when (operationFilter) {
            "all" -> true
            "ديون" -> tx.type == "ديون"
            "مصروفات" -> tx.type == "مصروفات"
            else -> tx.type == operationFilter
        }
        val cleanSearch = searchQuery.trim().lowercase()
        val matchesSearch = if (cleanSearch.isEmpty()) {
            true
        } else {
            tx.description.lowercase().contains(cleanSearch) ||
                tx.status.lowercase().contains(cleanSearch) ||
                tx.type.lowercase().contains(cleanSearch) ||
                tx.amount.toString().contains(cleanSearch) ||
                tx.date.contains(cleanSearch)
        }
        matchesPeriod && matchesType && matchesSearch
    }

    // --- Prepare last 7 days of sales for Line Graphing ---
    val last7DaysSalesCurve = remember(sales) {
        val groups = sales.groupBy { it.date }
        val sortedDates = groups.keys.sorted().takeLast(7)
        sortedDates.map { date ->
            val sum = groups[date]?.sumOf { it.quantity * it.sellingPrice } ?: 0.0
            date to sum.toFloat()
        }
    }

    // Clean RTL scrollable Layout
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Tab Selection Header for Custom Periodic Reports ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "اللوحة المالية والموقوف العام للوكالة",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = "محاسبة ديناميكية وتحديث فوري للميزانية والمديونية والضرائب",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Modern Period capsule switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141A17), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val periods = listOf(
                        "all" to "الموازنة الشاملة",
                        "year" to "ميزانية العام",
                        "month" to "تقرير الشهر",
                        "today" to "حسابات اليوم"
                    )
                    periods.forEach { (key, label) ->
                        val isSel = reportPeriod == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSel) Color(0xFF009639) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { reportPeriod = key }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else Color.Gray,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // --- STATS GRID DISPLAY ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatGridCard(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي المبيعات",
                        value = "%.2f ر.ي".format(periodTotalSales),
                        icon = Icons.Default.MonetizationOn,
                        accentColor = Color(0xFF009639)
                    )
                    StatGridCard(
                        modifier = Modifier.weight(1f),
                        title = "قيمة المخازن الجارية",
                        value = "%.2f ر.ي".format(totalStockValue),
                        icon = Icons.Default.Inventory2,
                        accentColor = Color(0xFF33B679)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatGridCard(
                        modifier = Modifier.weight(1f),
                        title = "تكاليف ومشتريات",
                        value = "%.2f ر.ي".format(periodCostOfSold),
                        icon = Icons.Default.ShoppingBag,
                        accentColor = Color(0xFFE4312B)
                    )
                    StatGridCard(
                        modifier = Modifier.weight(1f),
                        title = "الديون المستحقة ع زبائن",
                        value = "%.2f ر.ي".format(totalOutstandingDebts),
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = Color(0xFFFFA000)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatGridCard(
                        modifier = Modifier.weight(1f),
                        title = "صافي المصروفات",
                        value = "%.2f ر.ي".format(periodTotalExpenses),
                        icon = Icons.Default.Receipt,
                        accentColor = Color(0xFFE91E63)
                    )
                    StatGridCard(
                        modifier = Modifier.weight(1f),
                        title = "الضرائب المقتطعة",
                        value = "%.2f ر.ي".format(periodTotalTaxes),
                        icon = Icons.Default.Analytics,
                        accentColor = Color(0xFF6200EE)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatGridCard(
                        modifier = Modifier.weight(1f),
                        title = "صافي الأرباح",
                        value = "%.2f ر.ي".format(finalProfit),
                        icon = Icons.Default.TrendingUp,
                        accentColor = Color(0xFF009639)
                    )
                    StatGridCard(
                        modifier = Modifier.weight(1f),
                        title = "العجز والخسارة",
                        value = "%.2f ر.ي".format(finalLoss),
                        icon = Icons.Default.TrendingDown,
                        accentColor = Color(0xFFE4312B)
                    )
                }
            }
        }

        // --- DIRECT SUMMARY MATHEMATICS BLOCK ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A17)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF222825))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "معادلة الملخص المالي المباشر",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF009639),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val formulaRows = listOf(
                        "إجمالي إيراد المبيعات المحسوب" to ("+ %.2f ر.ي".format(periodTotalSales) to Color(0xFF33B679)),
                        "مشتريات وتكاليف القات الصعدي" to ("- %.2f ر.ي".format(periodCostOfSold) to Color(0xFFE4312B)),
                        "ضرائب مصلحة القات اليومية" to ("- %.2f ر.ي".format(periodTotalTaxes) to Color(0xFF6200EE)),
                        "المخرج المكتبي ومصاريف المفرش" to ("- %.2f ر.ي".format(periodDailyOutflows) to Color(0xFFE91E63)),
                        "المصروفات النثرية والعمالية" to ("- %.2f ر.ي".format(periodExpensesTable) to Color(0xFFD32F2F)),
                    )

                    formulaRows.forEach { (label, data) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(data.first, color = data.second, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(label, color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF2C322E))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "%.2f ر.ي".format(periodDirectProfit),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (isProfit) Color(0xFF33B679) else Color(0xFFE4312B)
                        )
                        Text(
                            text = if (isProfit) "صافي الأرباح التشغيلية للفترة:" else "العجز الموقوف المترتب للفترة:",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // --- DYNAMIC LIVE CHARTS INTERACTIVES ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A17)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF222825))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "مؤشرات ورسوم بيانية مباشرة",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Line curve of last 7 sales days
                    Text(
                        "تطور المبيعات اليومية لآخر ٧ أيام صفقة",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    SimpleLineChart(
                        salesData = last7DaysSalesCurve,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Bar chart comparing intake/outtake
                    Text(
                        "مقارنة الإيرادات بالمصاريف والأعباء للفترة",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    SimpleCompareBarChart(
                        revenue = periodTotalSales.toFloat(),
                        expenses = (periodCostOfSold + periodTotalExpenses + periodTotalTaxes).toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Stack allocation
                    Text(
                        "توزيع بنود وأعباء الصرف والخرج المالي للفترة",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ExpensesAllocationBar(
                        expensesTable = periodExpensesTable.toFloat(),
                        dailyTaxes = periodTotalTaxes.toFloat(),
                        outflowMaktab = periodDailyOutflows.toFloat() * 0.4f, // approximate split
                        outflowWorkers = periodDailyOutflows.toFloat() * 0.6f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- ACTIONS SECTION ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1412)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2421))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Export PDF button
                    Button(
                        onClick = {
                            PdfGenerator.generateAndSharePdf(
                                context,
                                "التقرير المالي العام لوكالة طوفان الأقصى",
                                listOf("الحساب والقيمة المالية", "رصد الحساب والبيان"),
                                listOf(
                                    listOf("%.2f ر.ي".format(periodTotalSales), "إجمالي إيراد المبيعات والتحصيلات"),
                                    listOf("%.2f ر.ي".format(periodCostOfSold), "تكاليف ومشتريات القات الصعدي"),
                                    listOf("%.2f ر.ي".format(totalStockValue), "قيمة البضاعة المخزنة الحالية"),
                                    listOf("%.2f ر.ي".format(totalOutstandingDebts), "ديون مستحقة ع زبائن جارية"),
                                    listOf("%.2f ر.ي".format(periodTotalExpenses), "صافي المصروفات والأقساط"),
                                    listOf("%.2f ر.ي".format(periodTotalTaxes), "إجمالي الضرائب والخصومات"),
                                    listOf("%.2f ر.ي".format(finalProfit), "صافي الأرباح التشغيلية"),
                                    listOf("%.2f ر.ي".format(finalLoss), "إجمالي الخسائر والخصومات")
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A221E))
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ملف PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share Text
                    Button(
                        onClick = {
                            PdfGenerator.shareViaText(
                                context,
                                "خلاصة التقرير المالي للفترة المحددة",
                                listOf(
                                    "إيراد المبيعات" to "%.2f ر.ي".format(periodTotalSales),
                                    "قيمة الشراء والتكاليف" to "%.2f ر.ي".format(periodCostOfSold),
                                    "رصيد المخزون الحالي" to "%.2f ر.ي".format(totalStockValue),
                                    "إجمالي ديون الوكالة" to "%.2f ر.ي".format(totalOutstandingDebts),
                                    "خرج المفرش والعمال" to "%.2f ر.ي".format(periodDailyOutflows),
                                    "مصروفات ونثريات" to "%.2f ر.ي".format(periodExpensesTable),
                                    "إجمالي الضرائب" to "%.2f ر.ي".format(periodTotalTaxes),
                                    "صافي الربح المكتسب" to "%.2f ر.ي".format(finalProfit),
                                    "العجز المترتب" to "%.2f ر.ي".format(finalLoss)
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A221E))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة النص", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Print Thermal Receipt
                    Button(
                        onClick = {
                            appViewModel.showReceipt(
                                AppViewModel.ReceiptData(
                                    title = "رصد موازنة وحسابات طوفان الأقصى",
                                    lines = listOf(
                                        "إجمالي المبيعات" to "%.2f".format(periodTotalSales),
                                        "تكاليف القات" to "%.2f".format(periodCostOfSold),
                                        "ديون الوكالة" to "%.2f".format(totalOutstandingDebts),
                                        "الضرائب اليومية" to "%.2f".format(periodTotalTaxes),
                                        "خرج المفرش والعمل" to "%.2f".format(periodDailyOutflows),
                                        "النثريات المصروفة" to "%.2f".format(periodExpensesTable),
                                        "صافي الأرباح" to "%.2f".format(finalProfit)
                                    ),
                                    total = finalProfit,
                                    category = "الحسابات للفترة"
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حراري فوري", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- UNIFIED TRANSACTIONS LIST LEDGER ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "سجل العمليات والتدفقات المالية الموحد",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Right
                )
                Text(
                    "جدول مالي شامل يتضمن عمليات البيع، الشراء، الديون، المصاريف والنثريات",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Unified Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث فوري في البيانات الحركية والعملاء...", fontSize = 12.sp, color = Color.Gray) },
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009639),
                        unfocusedBorderColor = Color(0xFF222825)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Modern Horizontal scroll categories filter for unified ledger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val opTypes = listOf(
                        "all" to "الكل",
                        "بيع" to "مبيعات",
                        "شراء" to "مشتريات",
                        "ديون" to "ديون وتحصيل",
                        "مصروفات" to "نفقات وضرائب",
                        "تحويل مالي" to "تحويلات"
                    )
                    opTypes.forEach { (key, label) ->
                        val isSel = operationFilter == key
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSel) Color(0xFF009639).copy(alpha = 0.2f) else Color(0xFF141A17),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) Color(0xFF009639) else Color(0xFF222825),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { operationFilter = key }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                label,
                                color = if (isSel) Color(0xFF009639) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Render Ledger records
        if (filteredUnifiedTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد حركة مالية تطابق شروط البحث والفلترة المطلوبة", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            items(filteredUnifiedTransactions) { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewTxDetail = tx },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121614)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1D221F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Amount with color
                            Text(
                                text = "%.2f ر.ي".format(tx.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = tx.color
                            )

                            // TypeBadge
                            Box(
                                modifier = Modifier
                                    .background(tx.color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tx.type,
                                    color = tx.color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tx.description,
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "الحالة: " + tx.status, fontSize = 10.sp, color = Color.Gray)
                            Text(text = tx.date, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // --- POPUP DETAILS DIALOG FOR THE CHOSEN LEDGER TRANSACTION ---
    viewTxDetail?.let { tx ->
        AlertDialog(
            onDismissRequest = { viewTxDetail = null },
            title = {
                Text(
                    text = "تفاصيل القيد المالي الموحّد",
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "نوع الحركة: " + tx.type, color = tx.color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "البيان والتفاصيل:", color = Color.Gray, fontSize = 11.sp)
                    Text(text = tx.description, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "المبلغ المالي:", color = Color.Gray, fontSize = 11.sp)
                    Text(text = "%.2f ر.ي".format(tx.amount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "حالة القيد والتحصيل: " + tx.status, color = Color.LightGray, fontSize = 12.sp)
                    Text(text = "تاريخ إتمام القيد: " + tx.date, color = Color.LightGray, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewTxDetail = null }) {
                    Text("إغلاق", color = Color(0xFF009639))
                }
            }
        )
    }
}

@Composable
fun AccountRow(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("%.2f ر.ي".format(value), color = Color.White)
        Text(label, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(appViewModel: AppViewModel, customers: List<Customer>, debtTxs: List<DebtTransaction>, settings: AppSettings) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyDebtors by remember { mutableStateOf(false) }

    // Dialog flags and target states
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var showAddTxDialog by remember { mutableStateOf(false) }

    // Bulk / Group SMS
    var showGroupSmsDialog by remember { mutableStateOf(false) }

    // Customer edit states
    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showDeleteCustomerDialog by remember { mutableStateOf(false) }

    // Transaction edit and delete states
    var txToEdit by remember { mutableStateOf<DebtTransaction?>(null) }
    var txToDelete by remember { mutableStateOf<DebtTransaction?>(null) }

    // Filter by period for accounts statement: "all", "today", "month", "year"
    var selectedPeriod by remember { mutableStateOf("all") }

    // Global Statistics Calculations
    val totalDebtsGlobal = debtTxs.filter { it.type == "دين" }.sumOf { it.amount }
    val totalPaidGlobal = debtTxs.filter { it.type == "سداد" }.sumOf { it.amount }
    val totalRemainingGlobal = totalDebtsGlobal - totalPaidGlobal

    // Filter customers list
    val filteredCustomers = customers.filter { client ->
        val nameMatch = client.name.contains(searchQuery, ignoreCase = true)
        val phoneMatch = client.phone.contains(searchQuery, ignoreCase = true)
        val matchesSearch = nameMatch || phoneMatch

        val customerTxs = debtTxs.filter { it.customerName == client.name }
        val cAdded = customerTxs.filter { it.type == "دين" }.sumOf { it.amount }
        val cPaid = customerTxs.filter { it.type == "سداد" }.sumOf { it.amount }
        val cRemaining = cAdded - cPaid

        val matchesDebtor = !showOnlyDebtors || (cRemaining > 0.1)

        matchesSearch && matchesDebtor
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedCustomer == null) {
            // LIST ALL CUSTOMERS & ADDING CUSTOMER
            item {
                Text(
                    "نظام إدارة وعمليات ديون وكالة طوفان الأقصى:",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            // Stat Cards Grid
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("عدد العملاء", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${customers.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("إجمالي الديون التراكمية", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("%.0f ر.ي".format(totalDebtsGlobal), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE4312B))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("إجمالي المسدد والمحصّل", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("%.0f ر.ي".format(totalPaidGlobal), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("صافي المتبقي بالصندوق خارجاً", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("%.0f ر.ي".format(totalRemainingGlobal), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFF00))
                            }
                        }
                    }
                }
            }

            // Simple Customer adding Form
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("إضافة ملف زبون / عميل جديد", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("اسم العميل ثنائياً أو ثلاثياً") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الهاتف / الجوال (للمراسلات)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("سكن العميل وعنوانه") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("حدود الائتمان وملاحظات الضمان") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (customerName.isBlank()) {
                                    Toast.makeText(context, "الرجاء تحديد الاسم لحفظ ملف العميل الجديد!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                appViewModel.addCustomer(customerName, phone, address, notes)
                                Toast.makeText(context, "تم حفظ العميل بنجاح", Toast.LENGTH_SHORT).show()

                                customerName = ""
                                phone = ""
                                address = ""
                                notes = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تسجيل وتثبيت ملف العميل")
                        }
                    }
                }
            }

            // Search and Advanced Filters Layout
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث عن عميل بالاسم أو الهاتف...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF009639),
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showOnlyDebtors = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!showOnlyDebtors) Color(0xFF009639) else Color(0xFF181C19)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("الجميع (${customers.size})", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showOnlyDebtors = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showOnlyDebtors) Color(0xFFE4312B) else Color(0xFF181C19)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            val debtorsCount = customers.filter { client ->
                                val txs = debtTxs.filter { it.customerName == client.name }
                                val debt = txs.filter { it.type == "دين" }.sumOf { it.amount }
                                val paid = txs.filter { it.type == "سداد" }.sumOf { it.amount }
                                (debt - paid) > 0.1
                            }.size
                            Text("المدينون فقط ($debtorsCount)", fontSize = 12.sp)
                        }
                    }

                    // Export / Group Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val reportTitle = "كشف وكالة طوفان الأقصى للديون الشاملة لكافة العملاء"
                                val headers = listOf("صافي المتبقي", "مسدد العميل", "حجم ديونه الكلي", "الجوال", "اسم الزبون")
                                val rows = customers.map { client ->
                                    val txs = debtTxs.filter { it.customerName == client.name }
                                    val debt = txs.filter { it.type == "دين" }.sumOf { it.amount }
                                    val paid = txs.filter { it.type == "سداد" }.sumOf { it.amount }
                                    val rem = debt - paid
                                    listOf(
                                        "%.2f".format(rem),
                                        "%.2f".format(paid),
                                        "%.2f".format(debt),
                                        client.phone.ifBlank { "-" },
                                        client.name
                                    )
                                }
                                PdfGenerator.generateAndSharePdf(context, reportTitle, headers, rows)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("كشف كلي (PDF)", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { showGroupSmsDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تذكير جماعي (SMS)", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }

            // Customer List Render
            if (filteredCustomers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
                    ) {
                        Text(
                            "لا توجد نتائج مطابقة لفلترة البحث الجارية",
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredCustomers) { customer ->
                    // Balances calculations of specific customer
                    val customerTxs = debtTxs.filter { it.customerName == customer.name }
                    val totalAdded = customerTxs.filter { it.type == "دين" }.sumOf { it.amount }
                    val totalPaid = customerTxs.filter { it.type == "سداد" }.sumOf { it.amount }
                    val remaining = totalAdded - totalPaid

                    // Last action details
                    val lastTx = customerTxs.maxByOrNull { it.date }
                    val lastActivity = if (lastTx != null) {
                        "${lastTx.type} بتاريخ ${lastTx.date} بقيمة ${lastTx.amount} ر.ي"
                    } else {
                        "لا توجد عمليات مقيدة بعد"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCustomer = customer },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "المتبقي: %.2f ر.ي".format(remaining),
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining > 0.1) Color(0xFFE4312B) else Color(0xFF009639)
                                )
                                Text(customer.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الهاتف: ${customer.phone.ifBlank { "غير مسجل" }} | العنوان: ${customer.address.ifBlank { "غير مسجل" }}", fontSize = 12.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي السداد: %.0f ر.ي".format(totalPaid), fontSize = 12.sp, color = Color.Gray)
                                Text("إجمالي المديونية: %.0f ر.ي".format(totalAdded), fontSize = 12.sp, color = Color.Gray)
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.DarkGray)
                            Text("آخر حركة: $lastActivity", fontSize = 11.sp, color = Color(0xFF009639))
                        }
                    }
                }
            }
        } else {
            // ACCOUNT STATEMENT SCREEN OF SELECTED CLIENT
            val client = selectedCustomer!!
            val clientTxs = debtTxs.filter { it.customerName == client.name }
            val totalAdded = clientTxs.filter { it.type == "دين" }.sumOf { it.amount }
            val totalPaid = clientTxs.filter { it.type == "سداد" }.sumOf { it.amount }
            val remaining = totalAdded - totalPaid

            // Filtering based on selectedPeriod for account report ("all", "today", "month", "year")
            val todayDate = appViewModel.getCurrentDateString()
            val monthPrefix = todayDate.substring(0, 7)
            val yearPrefix = todayDate.substring(0, 4)

            val filteredClientTxs = when (selectedPeriod) {
                "today" -> clientTxs.filter { it.date == todayDate }
                "month" -> clientTxs.filter { it.date.startsWith(monthPrefix) }
                "year" -> clientTxs.filter { it.date.startsWith(yearPrefix) }
                else -> clientTxs
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { selectedCustomer = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181C19)),
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("رجوع للزبائن")
                        }
                    }

                    Text(
                        "كشف حساب: ${client.name}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }

            // Client info Card & Actions
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("بيانات الزبون ومعلومات الاتصال", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("رقم الهاتف: ${client.phone.ifBlank { "غير مسجل" }}", color = Color.LightGray, fontSize = 13.sp)
                        Text("عنوان السكن: ${client.address.ifBlank { "غير مسجل" }}", color = Color.LightGray, fontSize = 13.sp)
                        Text("ملاحظات/الائتمان: ${client.notes.ifBlank { "لا توجد" }}", color = Color.LightGray, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEditCustomerDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تعديل العميل", fontSize = 11.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { showDeleteCustomerDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE4312B))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFE4312B))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف كلي", fontSize = 11.sp, color = Color(0xFFE4312B), maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Client Balance Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101311)),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("الملخص المالي العام للزبون", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("%.2f ر.ي".format(totalAdded), color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("إجمالي الديون التراكمية:")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("%.2f ر.ي".format(totalPaid), color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("إجمالي المقبوض والمسدد:")
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("%.2f ر.ي".format(remaining), fontWeight = FontWeight.Bold, color = if (remaining > 0.1) Color(0xFFE4312B) else Color(0xFF009639))
                            Text("الرصيد المتبقي المطلوب:", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddTxDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text("+ حركة حساب مالية", fontSize = 11.sp)
                            }

                            // Dynamic Print Preview Ledger
                            IconButton(
                                onClick = {
                                    val rxLines = mutableListOf<Pair<String, String>>()
                                    rxLines.add("اسم العميل" to client.name)
                                    if (client.phone.isNotBlank()) rxLines.add("رقم الهاتف" to client.phone)
                                    if (client.address.isNotBlank()) rxLines.add("السكن والعنوان" to client.address)
                                    rxLines.add("----------------" to "----------------")
                                    rxLines.add("إجمالي الديون" to "%.2f".format(totalAdded))
                                    rxLines.add("إجمالي المسدد" to "%.2f".format(totalPaid))
                                    rxLines.add("----------------" to "----------------")
                                    rxLines.add("تفاصيل الحركات" to "الأخيرة")
                                    filteredClientTxs.forEach { tx ->
                                        rxLines.add("${tx.date} [${tx.type}]" to "%.2f ر.ي".format(tx.amount))
                                        if (tx.notes.isNotBlank()) {
                                            rxLines.add("  - بيان : ${tx.notes}" to "")
                                        }
                                    }
                                    rxLines.add("----------------" to "----------------")
                                    rxLines.add("الرصيد المتبقي" to "%.2f".format(remaining))

                                    appViewModel.showReceipt(
                                        AppViewModel.ReceiptData(
                                            title = "كشف حساب: ${client.name}\nطوفان الأقصى للقات الصعدي",
                                            lines = rxLines,
                                            total = remaining,
                                            category = "الديون والعملاء"
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .background(Color(0xFF181C19), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = Color.LightGray)
                            }

                            // WhatsApp
                            IconButton(
                                onClick = {
                                    val formattedTxs = filteredClientTxs.joinToString("\n") { tx ->
                                        "← تاريخ [${tx.date}] | نوع: *${tx.type}* | قيمة: *${"%.2f".format(tx.amount)} ر.ي* | بيان: ${tx.notes}"
                                    }
                                    val whatsappText = """
*وكالة طوفان الأقصى لأجود أنواع القات الصعدي*
*صاحب الوكالة / أحمد منصور*
---------------------------------------
*الأخ الكريم / ${client.name}*
*هاتف:* ${client.phone.ifBlank { "غير متوفر" }}
*عنوان العميل:* ${client.address.ifBlank { "غير متوفر" }}
---------------------------------------
*📊 كشف الحساب والوضعية المالية الجارية:*
• *إجمالي الديون التراكمية:* ${"%.2f".format(totalAdded)} ر.ي
• *إجمالي المسدد والمقبوض:* ${"%.2f".format(totalPaid)} ر.ي
• *سند الرصيد المتبقي ذمتكم:* ${"%.2f".format(remaining)} ر.ي
---------------------------------------
*📋 تفاصيل سجل العمليات التاريخية بالتفصيل:*
$formattedTxs
---------------------------------------
*ملاحظات إضافية:*
${client.notes.ifBlank { "لا توجد ملاحظات عامة" }}

*يرجى التكرم بالاطلاع ومراجعة الحساب والبدء بالإيفاء بالرصيد المتبقي لتسوية حسابكم.*
*نشكر لكم أمانتكم وحسن معاملتكم المستمرة معنا.*
                                    """.trimIndent()
                                    try {
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=${client.phone}&text=${Uri.encode(whatsappText)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, whatsappText)
                                            }
                                            val chooser = Intent.createChooser(intent, "تم نسخ التقرير المالي، شاركه عبر:").apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(chooser)
                                        } catch (ex: Exception) {
                                            Toast.makeText(context, "لم نتمكن من بدء عملية المشاركة، يرجى التحقق من توفر تطبيقات المراسلة.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .background(Color(0xFF181C19), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    }
                }
            }

            // Periodic filter selection for ledger ("all", "today", "month", "year")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF181C19), RoundedCornerShape(8.dp)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val periods = listOf("all" to "الكل", "today" to "اليوم", "month" to "هذا الشهر", "year" to "هذا العام")
                    periods.forEach { (key, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selectedPeriod == key) Color(0xFF009639) else Color.Transparent, RoundedCornerShape(4.dp))
                                .clickable { selectedPeriod = key }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selectedPeriod == key) Color.White else Color.Gray)
                        }
                    }
                }
            }

            // Accounting actions specific for Statement export
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val headers = listOf("الملاحظات", "النوع", "المبلغ (ر.ي)", "التاريخ")
                            val rows = filteredClientTxs.map { tx ->
                                listOf(
                                    tx.notes.ifBlank { "بدون بيان" },
                                    tx.type,
                                    "%.2f".format(tx.amount),
                                    tx.date
                                )
                            }
                            val pdfTitle = "كشف حساب العميل: ${client.name}\nإجمالي الديون: %.2f | إجمالي المسدد: %.2f | المتبقي: %.2f".format(totalAdded, totalPaid, remaining)
                            PdfGenerator.generateAndSharePdf(context, pdfTitle, headers, rows)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181C19)),
                        modifier = Modifier.weight(1f).border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير كشف PDF", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val formattedTxs = filteredClientTxs.take(7).joinToString("\n") { tx ->
                                "تاريخ ${tx.date} ${tx.type}: ${tx.amount} ر.ي"
                            }
                            val smsText = """
وكالة طوفان الأقصى لأجود أنواع القات الصعدي

الأخ / ${client.name}

إجمالي الدين: $totalAdded ر.ي
إجمالي المسدد: $totalPaid ر.ي
المتبقي: $remaining ر.ي

تفاصيل العمليات:
$formattedTxs

يرجى مراجعة كشف الحساب وتسوية المبلغ المتبقي.

مع خالص الشكر والتقدير.
أحمد منصور
                            """.trimIndent()
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${client.phone}")
                                    putExtra("sms_body", smsText)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, smsText)
                                    }
                                    val chooser = Intent.createChooser(intent, "إرسال رسالة SMS عبر:").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(chooser)
                                } catch (ex: Exception) {
                                    Toast.makeText(context, "لم نتمكن من فتح تطبيق الرسائل النصية المباشر. يرجى مراجعة نظام الصلاحيات.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181C19)),
                        modifier = Modifier.weight(1f).border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مشاركة كشف SMS", fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    "تاريخ السجل والحركات المالية المقيدة:",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            if (filteredClientTxs.isEmpty()) {
                item {
                    Text("لا توجد حركات سحب أو سداد مسجلة بعد للفترة المحددة للعميل", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center)
                }
            }

            items(filteredClientTxs) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101311)),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "%.2f ر.ي".format(tx.amount),
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == "دين") Color(0xFFE4312B) else Color(0xFF009639)
                            )
                            Text("حركة تقييد: " + tx.type, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Text("بيان وملاحظة: ${tx.notes.ifBlank { "بلا بيان أو شرح" }}", fontSize = 12.sp, color = Color.LightGray)
                        Text("تاريخ التقييد: ${tx.date}", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { txToEdit = tx },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("تعديل", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { txToDelete = tx },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE4312B))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFE4312B))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("حذف", fontSize = 11.sp, color = Color(0xFFE4312B))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS FOR CUSTOMER & TRANS ACTIONS ---

    // 1. ADD NEW TRANSACTION (DEBT OR PAYMENT)
    if (showAddTxDialog && selectedCustomer != null) {
        var txType by remember { mutableStateOf("سداد") }
        var amountStr by remember { mutableStateOf("") }
        var txNotes by remember { mutableStateOf("") }
        var txDate by remember { mutableStateOf(appViewModel.getCurrentDateString()) }

        AlertDialog(
            onDismissRequest = { showAddTxDialog = false },
            title = {
                Text(
                    text = "تسجيل حركة حساب جديدة لـ ${selectedCustomer?.name}",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("نوع الحركة المالية الجديدة:")
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { txType = "سداد" }) {
                            RadioButton(selected = txType == "سداد", onClick = { txType = "سداد" })
                            Text("تسجيل سداد دفعة")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { txType = "دين" }) {
                            RadioButton(selected = txType == "دين", onClick = { txType = "دين" })
                            Text("دين إضافي مقيد")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("قيمة المعاملة بالعملة (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = txNotes,
                        onValueChange = { txNotes = it },
                        label = { Text("تفاصيل وبيان الحركة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = txDate,
                        onValueChange = { txDate = it },
                        label = { Text("التاريخ (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = amountStr.toDoubleOrNull() ?: 0.0
                        if (value <= 0.0) {
                            Toast.makeText(context, "الرجاء تحديد قيمة الحركة المالية بشكل صحيح!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        appViewModel.addDebtTransaction(selectedCustomer!!.name, value, txType, txNotes, txDate)
                        showAddTxDialog = false
                        Toast.makeText(context, "تم قيد وتثبيت الحركة بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTxDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // 2. EDIT CUSTOMER DATA
    if (showEditCustomerDialog && selectedCustomer != null) {
        var editPhone by remember { mutableStateOf(selectedCustomer!!.phone) }
        var editAddress by remember { mutableStateOf(selectedCustomer!!.address) }
        var editNotes by remember { mutableStateOf(selectedCustomer!!.notes) }

        AlertDialog(
            onDismissRequest = { showEditCustomerDialog = false },
            title = { Text("تعديل بيانات العميل - ${selectedCustomer!!.name}", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(horizontalAlignment = Alignment.End) {
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("رقم الهاتف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("السكن والعنوان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("ملاحظات / ضمانات") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = selectedCustomer!!.copy(
                            phone = editPhone,
                            address = editAddress,
                            notes = editNotes
                        )
                        appViewModel.updateCustomer(updated)
                        selectedCustomer = updated
                        showEditCustomerDialog = false
                        Toast.makeText(context, "تم حفظ وتحديث بيانات العميل", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                ) {
                    Text("تعديل وحفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCustomerDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // 3. DELETE CUSTOMER
    if (showDeleteCustomerDialog && selectedCustomer != null) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomerDialog = false },
            title = { Text("تأكيد حذف العميل", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من رغبتك في حذف ملف العميل [${selectedCustomer!!.name}] وكافة العمليات المرتبطة بحسابه بالكامل من الذاكرة؟", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(
                    onClick = {
                        val txsToDelete = debtTxs.filter { it.customerName == selectedCustomer!!.name }
                        txsToDelete.forEach { appViewModel.deleteDebtTransaction(it) }
                        appViewModel.deleteCustomer(selectedCustomer!!)
                        selectedCustomer = null
                        showDeleteCustomerDialog = false
                        Toast.makeText(context, "تم حذف المورد وحساباته بالكامل", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE4312B))
                ) {
                    Text("حذف الملف نهائياً")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCustomerDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // 4. BULK / GROUP SMS
    if (showGroupSmsDialog) {
        val groupBodyText = """
وكالة طوفان الأقصى لأجود أنواع القات الصعدي
الأخوة زبائننا الكرام،
نود تذكيركم بلطف بمراجعة حساباتكم الجارية والديون المتبقية لديكم وتسويتها في أقرب وقت ممكن.
شاكرين لكم حسن تعاونكم الدائم وثقتكم بنا.
- أحمد منصور
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showGroupSmsDialog = false },
            title = { Text("إرسال رسالة تذكير جماعية", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(horizontalAlignment = Alignment.End) {
                    Text("صيغة الرسالة الجماعية الرسمية لجميع العمليات بدون إفشاء الأسرار الفردية:")
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101311)),
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Text(groupBodyText, modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Right)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, groupBodyText)
                            }
                            val chooser = Intent.createChooser(intent, "إرسال الرسالة الجماعية عبر:").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(chooser)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل بدء تطبيق مشاركة الرسائل!", Toast.LENGTH_SHORT).show()
                        }
                        showGroupSmsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                ) {
                    Text("إرسال جماعي")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroupSmsDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // 5. TRANSACTION EDIT
    if (txToEdit != null) {
        var editAmount by remember { mutableStateOf(txToEdit!!.amount.toString()) }
        var editType by remember { mutableStateOf(txToEdit!!.type) }
        var editNotes by remember { mutableStateOf(txToEdit!!.notes) }
        var editDate by remember { mutableStateOf(txToEdit!!.date) }

        AlertDialog(
            onDismissRequest = { txToEdit = null },
            title = { Text("تعديل تفاصيل الحركة المالية", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(horizontalAlignment = Alignment.End) {
                    Text("نوع الحركة:")
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { editType = "سداد" }) {
                            RadioButton(selected = editType == "سداد", onClick = { editType = "سداد" })
                            Text("سداد مبلغ")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { editType = "دين" }) {
                            RadioButton(selected = editType == "دين", onClick = { editType = "دين" })
                            Text("دين مقيد")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("المبلغ المالي (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editDate,
                        onValueChange = { editDate = it },
                        label = { Text("التاريخ (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("تفاصيل وملاحظات الحركة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = editAmount.toDoubleOrNull() ?: 0.0
                        if (amountVal <= 0.0) {
                            Toast.makeText(context, "الرجاء تحديد قيمة مالية صالحة!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val updated = txToEdit!!.copy(
                            amount = amountVal,
                            type = editType,
                            notes = editNotes,
                            date = editDate
                        )
                        appViewModel.updateDebtTransaction(updated)
                        txToEdit = null
                        Toast.makeText(context, "تم حفظ التعديلات بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                ) {
                    Text("حفظ التغييرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToEdit = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // 6. TRANSACTION DELETE CONFIRM
    if (txToDelete != null) {
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("حذف الحركة المالية", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من رغبتك في حذف حركة ( ${txToDelete!!.type} ) بقيمة ( ${"%.2f".format(txToDelete!!.amount)} ر.ي ) المسجلة بتاريخ ${txToDelete!!.date}؟ لا يمكن استرجاع الحركة بعد ذلك.", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(
                    onClick = {
                        appViewModel.deleteDebtTransaction(txToDelete!!)
                        txToDelete = null
                        Toast.makeText(context, "تم حذف الحركة المالية بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE4312B))
                ) {
                    Text("تأكيد الحذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(appViewModel: AppViewModel, suppliers: List<Supplier>, transactions: List<SupplierTransaction>, settings: AppSettings) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
    var showPurchasingForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedSupplier == null) {
            item {
                Text(
                    "إدارة الموردين وأسعار التوريد:",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            // Create new supplier card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("إضافة مورد طازج خارجي", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("اسم المورد بالكامل") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("هاتف المورد") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("مكان السكن / محافظة") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    Toast.makeText(context, "الرجاء تسجيل اسم المورد أولاً لتقييده!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                appViewModel.addSupplier(name, phone, address, notes)
                                Toast.makeText(context, "تم حفظ المورد في قائمة الموثوقين", Toast.LENGTH_SHORT).show()

                                name = ""
                                phone = ""
                                address = ""
                                notes = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ المورد")
                        }
                    }
                }
            }

            item {
                Text(
                    "الموردين الشركاء بالوكالة:",
                    color = Color.LightGray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            items(suppliers) { sup ->
                val supTxs = transactions.filter { it.supplierName == sup.name }
                val remainingDebt = supTxs.sumOf { it.debtRemaining } - supTxs.filter { it.type == "سداد" }.sumOf { it.amountPaid }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSupplier = sup },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "متبقي للمورد: %.2f ر.ي".format(remainingDebt),
                                fontWeight = FontWeight.Bold,
                                color = if (remainingDebt > 0) Color(0xFFE4312B) else Color(0xFF009639)
                            )
                            Text(sup.name, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text("المدينة: ${sup.address} | قاطرة الوصف: ${sup.notes}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            // DETAIL LEDGER OF SUPPLIER
            val sup = selectedSupplier!!
            val supTxs = transactions.filter { it.supplierName == sup.name }
            val totalRemaining = supTxs.sumOf { it.debtRemaining } - supTxs.filter { it.type == "سداد" }.sumOf { it.amountPaid }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { selectedSupplier = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181C19))
                    ) {
                        Text("قائمة الموردين")
                    }
                    Text(
                        "كشف مديونية: ${sup.name}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("إحصائية الحساب الجاري للمورد", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("%.2f ر.ي".format(totalRemaining), color = if (totalRemaining > 0) Color(0xFFE4312B) else Color(0xFF009639), fontWeight = FontWeight.Bold)
                            Text("المديونية المتبقية له:")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showPurchasingForm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                            ) {
                                Text("+ تقييد شراء وتوريد / سداد")
                            }

                            IconButton(onClick = {
                                PdfGenerator.shareViaText(
                                    context,
                                    "كشف شراكة المورد: " + sup.name,
                                    listOf(
                                        "المديونية" to "%.2f ر.ي".format(totalRemaining),
                                        "محافظة" to sup.address,
                                        "هاتف المورد" to sup.phone
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "سجل الفواتير وعمليات السداد السابقة:",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            items(supTxs) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101311)),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "%.2f ر.ي".format(tx.amountPaid + tx.debtRemaining),
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == "شراء") Color(0xFFE4312B) else Color(0xFF009639)
                            )
                            Text("نوع الحركة: " + tx.type, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        if (tx.type == "شراء") {
                            Text("الصنف: ${tx.qatType} | الكمية: ${tx.quantity} | السعر: ${tx.unitPrice}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text("المدفوع: ${tx.amountPaid} | المتبقي ديناً: ${tx.debtRemaining}", fontSize = 11.sp, color = Color.Gray)
                        Text("التاريخ: ${tx.date}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    // Purchasing / Payments Dialogue for specific Supplier
    if (showPurchasingForm && selectedSupplier != null) {
        var purchaseType by remember { mutableStateOf("شراء") }
        var qatType by remember { mutableStateOf("") }
        var quantityStr by remember { mutableStateOf("") }
        var unitPriceStr by remember { mutableStateOf("") }
        var paidStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPurchasingForm = false },
            title = { Text("تقييد معاملة توريد مالية لـ: ${selectedSupplier?.name}", fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("نوع الحركة:")
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { purchaseType = "سداد" }) {
                            RadioButton(selected = purchaseType == "سداد", onClick = { purchaseType = "سداد" })
                            Text("سداد ديون مستحقة له")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { purchaseType = "شراء" }) {
                            RadioButton(selected = purchaseType == "شراء", onClick = { purchaseType = "شراء" })
                            Text("شراء كميات بالآجل / نقد")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (purchaseType == "شراء") {
                        OutlinedTextField(
                            value = qatType,
                            onValueChange = { qatType = it },
                            label = { Text("نوع وصنف القات الصعدي") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("الكمية") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = unitPriceStr,
                            onValueChange = { unitPriceStr = it },
                            label = { Text("سعر التوريد المتفق") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = paidStr,
                        onValueChange = { paidStr = it },
                        label = { Text(if (purchaseType == "سداد") "المبلغ المدفوع كلياً" else "المبلغ المدفوع الان كاش") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountPaid = paidStr.toDoubleOrNull() ?: 0.0
                        if (purchaseType == "سداد") {
                            if (amountPaid <= 0.0) {
                                Toast.makeText(context, "الرجاء كسر القيمة بشكل صحيح!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            appViewModel.addSupplierTransaction(selectedSupplier!!.name, "", 0.0, 0.0, amountPaid, "سداد")
                        } else {
                            val quantity = quantityStr.toDoubleOrNull() ?: 0.0
                            val unitPrice = unitPriceStr.toDoubleOrNull() ?: 0.0

                            if (qatType.isBlank() || quantity <= 0.0 || unitPrice <= 0.0) {
                                Toast.makeText(context, "الرجاء استكمال بيانات الفاتورة!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            appViewModel.addSupplierTransaction(selectedSupplier!!.name, qatType, quantity, unitPrice, amountPaid, "شراء")
                        }

                        showPurchasingForm = false
                        Toast.makeText(context, "تم حفظ العملية وخصم/إضافة البيانات بنجاح تام", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                ) {
                    Text("تأكيد وحفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchasingForm = false }) { Text("إلغاء") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(appViewModel: AppViewModel, list: List<Expense>) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var valueStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showDeleteConfirm by remember { mutableStateOf<Expense?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "قيد المصروفات والنثريات اليومية:",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        // Add Expense Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("تقييد مصروف جديد", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("الوصف (عمال، مفرش، أكل، نقل...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("قيمة المصروف بالريال اليمني") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val amount = valueStr.toDoubleOrNull() ?: 0.0
                            if (description.isBlank() || amount <= 0.0) {
                                Toast.makeText(context, "يرجى من فضلك ملء حقول المصروفات بقيم صحيحة!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            appViewModel.addExpense(description, amount, notes)
                            Toast.makeText(context, "تم حفظ وقيد المصروف في كشوفات اليوم", Toast.LENGTH_SHORT).show()

                            description = ""
                            valueStr = ""
                            notes = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تقييد المصروف")
                    }
                }
            }
        }

        item {
            Text(
                "جدول سجل المصروفات المقيدة المتراكمة:",
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        if (list.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))) {
                    Text("لا توجد مصروفات مسجلة اليوم مسبقاً", modifier = Modifier.padding(16.dp).fillMaxWidth(), color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        items(list) { expense ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "%.2f ر.ي".format(expense.amount),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE4312B)
                        )
                        Text(expense.description, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("الملاحظات المقيدة: ${expense.notes} | التاريخ: ${expense.date}", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                appViewModel.showReceipt(
                                    AppViewModel.ReceiptData(
                                        title = "سند صرف - " + expense.description,
                                        lines = listOf(
                                            "نوع المصروف" to expense.description,
                                            "ملاحظات الصرف" to expense.notes,
                                            "تاريخ الحركة" to expense.date
                                        ),
                                        total = expense.amount,
                                        category = "المصروفات"
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = Color.LightGray)
                            }

                            IconButton(onClick = {
                                PdfGenerator.generateAndSharePdf(
                                    context,
                                    "تقرير مصروفات تشغيلية",
                                    listOf("الملاحظات", "القيمة المالية", "تاريخ الصرف", "بيان المصروف"),
                                    listOf(
                                        listOf(
                                            expense.notes,
                                            "%.2f ر.ي".format(expense.amount),
                                            expense.date,
                                            expense.description
                                        )
                                    )
                                )
                            }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.LightGray)
                            }
                        }

                        IconButton(onClick = { showDeleteConfirm = expense }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE4312B))
                        }
                    }
                }
            }
        }
    }

    showDeleteConfirm?.let { expense ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("تأكيد الحذف", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من حذف هذا العنصر؟", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    appViewModel.deleteExpense(expense)
                    showDeleteConfirm = null
                    Toast.makeText(context, "تم إلغاء وشطب قيد المصروف", Toast.LENGTH_SHORT).show()
                }) { Text("حذف", color = Color(0xFFE4312B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("إلغاء") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(appViewModel: AppViewModel, list: List<FinancialTransfer>) {
    val context = LocalContext.current
    var amountStr by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("أحمد منصور") }
    var exchangeHouse by remember { mutableStateOf("عبدالله المشرقي") }

    var showDeleteConfirm by remember { mutableStateOf<FinancialTransfer?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "رصد الحوالات المالية والصرافة:",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        // Add Transfer Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("إضافة قيد حوالة مرسلة جديدة", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("مبلغ الحوالة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("الوصف والبيان التفصيلي") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sender,
                        onValueChange = { sender = it },
                        label = { Text("المرسل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = exchangeHouse,
                        onValueChange = { exchangeHouse = it },
                        label = { Text("شركة الصرافة المعتمدة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val valAmount = amountStr.toDoubleOrNull() ?: 0.0
                            if (valAmount <= 0.0 || desc.isBlank()) {
                                Toast.makeText(context, "الرجاء مراجعة بيانات الحوالة بدقة والملء الكامل للمدخلات!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            appViewModel.addTransfer(valAmount, desc, sender, exchangeHouse)
                            Toast.makeText(context, "تم حفظ وقيد الحوالة المالية بنجاح بالشبكة", Toast.LENGTH_SHORT).show()

                            amountStr = ""
                            desc = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("قيد وتثبيت الحوالة")
                    }
                }
            }
        }

        item {
            Text(
                "سجلات الحوالات المالية السابقة:",
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        if (list.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))) {
                    Text("لا توجد حوالات صادرة مقيدة اليوم مسبقاً", modifier = Modifier.padding(16.dp).fillMaxWidth(), color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        items(list) { transfer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "%.2f ر.ي".format(transfer.amount),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF009639)
                        )
                        Text("حوالة: صرافة ${transfer.exchangeHouse}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("البيان المقيد: ${transfer.description}", fontSize = 12.sp, color = Color.Gray)
                    Text("المرسل: ${transfer.senderName} | التاريخ: ${transfer.date}", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                appViewModel.showReceipt(
                                    AppViewModel.ReceiptData(
                                        title = "سند حوالة - " + transfer.exchangeHouse,
                                        lines = listOf(
                                            "المرسل" to transfer.senderName,
                                            "الصراف المعتمد" to transfer.exchangeHouse,
                                            "البيان والصفة" to transfer.description,
                                            "تاريخ الحوالة" to transfer.date
                                        ),
                                        total = transfer.amount,
                                        category = "التحويلات"
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = Color.LightGray)
                            }

                            IconButton(onClick = {
                                PdfGenerator.generateAndSharePdf(
                                    context,
                                    "تقرير حوالات حركات صرافة",
                                    listOf("المرسل والشارح", "المرسل إليه / الصراف", "قيمة الحوالة", "التاريخ"),
                                    listOf(
                                        listOf(
                                            transfer.senderName + " (" + transfer.description + ")",
                                            transfer.exchangeHouse,
                                            "%.2f ر.ي".format(transfer.amount),
                                            transfer.date
                                        )
                                    )
                                )
                            }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.LightGray)
                            }
                        }

                        IconButton(onClick = { showDeleteConfirm = transfer }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE4312B))
                        }
                    }
                }
            }
        }
    }

    showDeleteConfirm?.let { transfer ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("تأكيد الحذف", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من حذف هذا العنصر؟", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    appViewModel.deleteTransfer(transfer)
                    showDeleteConfirm = null
                    Toast.makeText(context, "تم إلغاء وشطب قيد الحوالة تماماً", Toast.LENGTH_SHORT).show()
                }) { Text("حذف", color = Color(0xFFE4312B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun ReportsScreen(
    appViewModel: AppViewModel,
    inventory: List<QatInventory>,
    sales: List<QatSale>,
    expenses: List<Expense>,
    debts: List<DebtTransaction>,
    suppliers: List<Supplier>,
    transfers: List<FinancialTransfer>,
    settings: AppSettings
) {
    val context = LocalContext.current
    var selectedReportFilter by remember { mutableStateOf("يومية") } // يومية / شهرية / سنوية

    val filterDateString = when (selectedReportFilter) {
        "يومية" -> appViewModel.getCurrentDateString()
        "شهرية" -> appViewModel.getCurrentDateString().substring(0, 7) // YYYY-MM
        else -> appViewModel.getCurrentDateString().substring(0, 4) // YYYY
    }

    // Filter matching data lists
    val filteredSales = sales.filter { it.date.startsWith(filterDateString) }
    val filteredExpenses = expenses.filter { it.date.startsWith(filterDateString) }
    val filteredTransfers = transfers.filter { it.date.startsWith(filterDateString) }
    val filteredAddedDebts = debts.filter { it.date.startsWith(filterDateString) && it.type == "دين" }
    val filteredPaidDebts = debts.filter { it.date.startsWith(filterDateString) && it.type == "سداد" }

    val totalSales = filteredSales.sumOf { it.quantity * it.sellingPrice }
    val totalExpenses = filteredExpenses.sumOf { it.amount }
    val totalTransfers = filteredTransfers.sumOf { it.amount }
    val totalAddedDebtsValue = filteredAddedDebts.sumOf { it.amount }
    val totalPaidDebtsValue = filteredPaidDebts.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "التقارير التحليلية والعمليات:",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("تحديد نطاق تاريخ التقرير والتحليل:", fontWeight = FontWeight.SemiBold, color = Color(0xFF009639))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectedReportFilter = "سنوية" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedReportFilter == "سنوية") Color(0xFF009639) else Color(0xFF101311))
                        ) { Text("سنوية") }

                        Button(
                            onClick = { selectedReportFilter = "شهرية" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedReportFilter == "شهرية") Color(0xFF009639) else Color(0xFF101311))
                        ) { Text("شهرية") }

                        Button(
                            onClick = { selectedReportFilter = "يومية" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedReportFilter == "يومية") Color(0xFF009639) else Color(0xFF101311))
                        ) { Text("يومية") }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("خلاصة التقرير المالي الجاري ($selectedReportFilter)", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                    Spacer(modifier = Modifier.height(12.dp))

                    AccountRow("إجمالي المبيعات", totalSales)
                    AccountRow("إجمالي المصروفات والنثريات", totalExpenses)
                    AccountRow("إجمالي الحوالات الموجهة", totalTransfers)
                    AccountRow("إجمالي الديون الجديدة المضافة", totalAddedDebtsValue)
                    AccountRow("إجمالي سداد الديون المحصلة", totalPaidDebtsValue)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        PdfGenerator.shareViaText(
                            context,
                            "تقرير إحصائيات الدورة المالية ($selectedReportFilter)",
                            listOf(
                                "نطاق الملف" to filterDateString,
                                "قيمة المبيعات" to "%.2f ر.ي".format(totalSales),
                                "المصروفات العامة" to "%.2f ر.ي".format(totalExpenses),
                                "حجم الحوالات" to "%.2f ر.ي".format(totalTransfers),
                                "قيد الديون الصادرة" to "%.2f ر.ي".format(totalAddedDebtsValue),
                                "تحصيل الديون" to "%.2f ر.ي".format(totalPaidDebtsValue)
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181C19))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مشاركة")
                }

                Button(
                    onClick = {
                        PdfGenerator.generateAndSharePdf(
                            context,
                            "تقرير دوري مالي للوكالة - نطاق " + selectedReportFilter + " " + filterDateString,
                            listOf("القيمة المالية المقيدة", "اسم مؤشر الحساب الجاري"),
                            listOf(
                                listOf("%.2f ر.ي".format(totalSales), "إجمالي مبيعات القات خلال الفترة"),
                                listOf("%.2f ر.ي".format(totalExpenses), "إجمالي المصروفات والنثريات"),
                                listOf("%.2f ر.ي".format(totalTransfers), "إجمالي التحويلات للمصارف"),
                                listOf("%.2f ر.ي".format(totalAddedDebtsValue), "مجموع الديون المقيدة للوكالة ع زبون"),
                                listOf("%.2f ر.ي".format(totalPaidDebtsValue), "مجموع تحصيل المبالغ والديون من مدين")
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181C19))
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تقرير PDF")
                }

                Button(
                    onClick = {
                        appViewModel.showReceipt(
                            AppViewModel.ReceiptData(
                                title = "سند تقارير دوري فئة $selectedReportFilter",
                                lines = listOf(
                                    "نطاق التحليل" to filterDateString,
                                    "مجموع مبيعات" to "%.2f".format(totalSales),
                                    "مصروفات تشغيل" to "%.2f".format(totalExpenses),
                                    "حوالات مرسلة" to "%.2f".format(totalTransfers),
                                    "ديون جديدة مقيدة" to "%.2f".format(totalAddedDebtsValue)
                                ),
                                total = totalSales - totalExpenses,
                                category = "التقارير"
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("معاينة")
                }
            }
        }
    }
}

@Composable
fun ArchivesScreen(appViewModel: AppViewModel, list: List<DailyArchive>) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "الأرشيف المحاسبي التراكمي:",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("الأرشيف والمطابقة اليدوية لليوم", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("يقوم النظام تلقائياً وبأمان تام بأرشفة الحسابات والأصناف والتحصيلات في تمام الساعة 12:00 منتصف الليل وحفظها محلياً وبدء كشف حسابات يوم جديد.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Right)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { appViewModel.triggerManualArchive() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("القيام بالأرشفة والمزامنة لليوم يدوياً الآن")
                    }
                }
            }
        }

        item {
            Text(
                "كشوفات الأيام المؤرشفة بالذاكرة الحجرية:",
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        if (list.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))) {
                    Text("لا توجد أيام مؤرشفة بالذاكرة بعد (مخصصة للسلامة)", modifier = Modifier.padding(16.dp).fillMaxWidth(), color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        items(list) { archive ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "صافي ربح الأرشفة: %.2f ر.ي".format(archive.totalProfit - archive.totalLoss),
                            fontWeight = FontWeight.Bold,
                            color = if (archive.totalProfit >= archive.totalLoss) Color(0xFF009639) else Color(0xFFE4312B)
                        )
                        Text("أرشيف تاريخ: ${archive.archiveDate}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("مخزون حذر: ${archive.totalInventoryValue} | إجمالي المبيعات المؤرشفة: ${archive.totalSales}", fontSize = 12.sp, color = Color.Gray)
                    Text("الديون المستقرة: ${archive.totalDebts} | النثريات المصروفة: ${archive.totalExpenses}", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {
                            appViewModel.showReceipt(
                                AppViewModel.ReceiptData(
                                    title = "تقرير أرشيف اليوم المؤرخ - " + archive.archiveDate,
                                    lines = listOf(
                                        "المبيعات" to "%.2f".format(archive.totalSales),
                                        "المخزون" to "%.2f".format(archive.totalInventoryValue),
                                        "الأرباح" to "%.2f".format(archive.totalProfit),
                                        "الديون" to "%.2f".format(archive.totalDebts),
                                        "المصروفات" to "%.2f".format(archive.totalExpenses)
                                    ),
                                    total = archive.totalProfit - archive.totalLoss,
                                    category = "الأرشيف"
                                )
                            )
                        }) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = Color.LightGray)
                        }

                        IconButton(onClick = {
                            PdfGenerator.generateAndSharePdf(
                                context,
                                "تقرير تصفية حسابات اليوم مؤرشفة ومطابقة",
                                listOf("صفة الحساب المؤرشف", "موازنة الحساب المالي"),
                                listOf(
                                    listOf("%.2f ر.ي".format(archive.totalSales), "أرشيف مبيعات اليوم"),
                                    listOf("%.2f ر.ي".format(archive.totalInventoryValue), "أرشيف قيمة المخزون الجاري"),
                                    listOf("%.2f ر.ي".format(archive.totalProfit), "الأرباح الصافية المحوسبة"),
                                    listOf("%.2f ر.ي".format(archive.totalExpenses), "نفقات ومصاريف العمل"),
                                    listOf("%.2f ر.ي".format(archive.totalDebts), "الدائنة والديون الصافية")
                                )
                            )
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(appViewModel: AppViewModel, settings: AppSettings, backups: List<BackupRecord>) {
    val context = LocalContext.current
    var showPrinterSetupDialog by remember { mutableStateOf(false) }
    var showConfirmLogoutDialog by remember { mutableStateOf(false) }

    var showDeleteConfirmBackup by remember { mutableStateOf<BackupRecord?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        item {
            Text(
                "الإعدادات وأمن حماية قواعد البيانات:",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        // Setup Wireless Bluetooth Thermal Printer Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("إدارة طباعة البلوتوث الطابعة الحرارية", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("طابعة البلوتوث الحالية: ${if (settings.printerName.isBlank()) "غير معينة بعد" else "${settings.printerName} (${settings.printerAddress})"}", color = Color.White, fontSize = 12.sp)
                    Text("حجم عرض الورق المعتمد: ${settings.paperWidth} | حجم الخط: ${settings.fontSize} pt", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showPrinterSetupDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("البحث وربط طابعتك الحرارية الآن تلقائياً")
                    }
                }
            }
        }

        // Database backups management block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("النسخ الاحتياطي والأمن السحابي المحلي 100%", fontWeight = FontWeight.Bold, color = Color(0xFF009639))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("يقوم النظام بسلامة مطلقة بإنشاء نسخة احتياطية محلية بالكامل لهاتفك كل 7 أيام للوقاية من تلف وفقدان الأجهزة والهاردكور.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Right)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { appViewModel.triggerLocalBackup() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إنشاء نسخة احتياطية محلية فوراً الآن")
                    }
                }
            }
        }

        item {
            Text(
                "سجلات النسخ الاحتياطية المتوفرة للاستعادة البسيطة:",
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        if (backups.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))) {
                    Text("لا توجد ملفات نسخ احتياطية سابقة متاحة في الذاكرة حالياً", modifier = Modifier.padding(14.dp).fillMaxWidth(), color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        items(backups) { backup ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181C19))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(backup.fileName, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Right)
                    Text("وقت الحفظ والتامين: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(backup.backupTime))}", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { appViewModel.restoreBackup(backup) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
                        ) {
                            Text("استعادة واستبدال لقواعد البيانات", fontSize = 11.sp)
                        }

                        IconButton(onClick = { showDeleteConfirmBackup = backup }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE4312B))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { showConfirmLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE4312B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تسجيل الخروج الآمن من الحساب")
            }
        }
    }

    // Wireless Bluetooth Thermal setup Dialogue
    if (showPrinterSetupDialog) {
        PrinterSetupDialog(
            currentSettings = settings,
            onSaveSettings = { appViewModel.saveSettings(it) },
            onDismiss = { showPrinterSetupDialog = false },
            onLogPrint = { appViewModel.logPrint(it.isSuccess, it.category, it.errorMessage) }
        )
    }

    // Confirm Logout Dialuge
    if (showConfirmLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmLogoutDialog = false },
            title = { Text("تأكيد تسجيل الخروج", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من رغبتك في تسجيل الخروج وتأمين الجلسة وصلاحيتك المقيدة؟", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    appViewModel.logout()
                    showConfirmLogoutDialog = false
                    Toast.makeText(context, "تم قفل الجلسة الآمنة للوكالة بنجاح", Toast.LENGTH_SHORT).show()
                }) { Text("تسجيل الخروج", color = Color(0xFFE4312B)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmLogoutDialog = false }) { Text("إبقاء الجلسة") }
            }
        )
    }

    // Confirm Backup Delete Dialog
    if (showDeleteConfirmBackup != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmBackup = null },
            title = { Text("تأكيد الحذف", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من حذف هذا العنصر؟", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmBackup?.let { backup ->
                        appViewModel.deleteBackup(backup)
                    }
                    showDeleteConfirmBackup = null
                    Toast.makeText(context, "تم حذف ملف النسخة الاحتياطية بنجاح من الذاكرة", Toast.LENGTH_SHORT).show()
                }) { Text("حذف", color = Color(0xFFE4312B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmBackup = null }) { Text("إلغاء") }
            }
        )
    }
}

// -------------------------------------------------------------
// --- META COMPONENT TABS ---
// -------------------------------------------------------------

data class TabMeta(
    val label: String,
    val icon: ImageVector
)

val TAB_SECTIONS_META = listOf(
    TabMeta("الرئيسية", Icons.Default.Home),
    TabMeta("المخزون", Icons.Default.Inventory2),
    TabMeta("المبيعات", Icons.Default.ShoppingCart),
    TabMeta("الحسابات", Icons.Default.AccountBalance),
    TabMeta("الديون", Icons.Default.CreditCard),
    TabMeta("الموردين", Icons.Default.Group),
    TabMeta("المصروفات", Icons.Default.Receipt),
    TabMeta("التحويلات", Icons.Default.Send),
    TabMeta("التقارير", Icons.Default.BarChart),
    TabMeta("الأرشيف", Icons.Default.History),
    TabMeta("الإعدادات", Icons.Default.Settings)
)
