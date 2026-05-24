package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.data.PrintLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PrinterDevice(
    val name: String,
    val address: String,
    val isThermal: Boolean,
    val widthClass: String = "58mm"
)

val SIMULATED_BLUETOOTH_DEVICES = listOf(
    PrinterDevice("MTP-II (Thermal-58)", "00:11:22:33:44:55", true, "58mm"),
    PrinterDevice("Xprinter XP-80C (Thermal-80)", "88:99:AA:BB:CC:DD", true, "80mm"),
    PrinterDevice("PT-210 (Thermal-58)", "12:34:56:78:9A:BC", true, "58mm"),
    PrinterDevice("Sony WH-1000XM4", "AA:BB:CC:11:22:33", false),
    PrinterDevice("M23-Galaxy S24", "FE:DC:BA:98:76:54", false),
    PrinterDevice("Office Jet Copier", "99:88:77:66:55:44", false)
)

@Composable
fun PrinterSetupDialog(
    currentSettings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onDismiss: () -> Unit,
    onLogPrint: (PrintLog) -> Unit
) {
    var scanning by remember { mutableStateOf(false) }
    var discoveredDevices by remember { mutableStateOf<List<PrinterDevice>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var selectedDevice by remember { mutableStateOf<PrinterDevice?>(null) }
    var paperWidth by remember { mutableStateOf(currentSettings.paperWidth) }
    var fontSize by remember { mutableStateOf(currentSettings.fontSize) }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun startScan() {
        scope.launch {
            scanning = true
            statusMessage = "جاري البحث عن أجهزة بلوتوث قريبة..."
            isError = false
            delay(1500) // simulated scan delay
            discoveredDevices = SIMULATED_BLUETOOTH_DEVICES
            scanning = false
            statusMessage = "اكتمل البحث. اختر الطابعة الحرارية من القائمة:"
        }
    }

    LaunchedEffect(Unit) {
        startScan()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إعدادات الطابعة الحرارية",
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "حجم ورق الطباعة:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { paperWidth = "80mm" }
                        ) {
                            RadioButton(selected = paperWidth == "80mm", onClick = { paperWidth = "80mm" })
                            Text("80mm")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { paperWidth = "58mm" }
                        ) {
                            RadioButton(selected = paperWidth == "58mm", onClick = { paperWidth = "58mm" })
                            Text("58mm")
                        }
                    }
                }

                item {
                    Text(
                        text = "حجم الخط:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (fontSize < 24) fontSize += 2 }) {
                            Icon(Icons.Default.Add, contentDescription = "تكبير")
                        }
                        Text(text = "$fontSize pt", modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { if (fontSize > 10) fontSize -= 2 }) {
                            Icon(Icons.Default.Remove, contentDescription = "تصغير")
                        }
                    }
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { startScan() },
                            enabled = !scanning,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            if (scanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text("إعادة البحث")
                            }
                        }
                        Text(
                            text = "أجهزة البلوتوث المتاحة:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (statusMessage.isNotBlank()) {
                    item {
                        Text(
                            text = statusMessage,
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                items(discoveredDevices) { device ->
                    val isSelected = selectedDevice == device || (currentSettings.printerAddress == device.address && selectedDevice == null)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!device.isThermal) {
                                    isError = true
                                    statusMessage = "الجهاز المحدد ليس طابعة حرارية مدعومة، يرجى اختيار طابعة حرارية صحيحة."
                                    selectedDevice = null
                                    onLogPrint(
                                        PrintLog(
                                            category = "بلوتوث",
                                            isSuccess = false,
                                            errorMessage = "اختر جهاز غير متوافق: ${device.name}"
                                        )
                                    )
                                } else {
                                    isError = false
                                    selectedDevice = device
                                    statusMessage = "تم اختيار الطابعة الحرارية بنجاح: ${device.name}"
                                    paperWidth = device.widthClass
                                    onLogPrint(
                                        PrintLog(
                                            category = "بلوتوث",
                                            isSuccess = true,
                                            errorMessage = "توصيل ناجح بالحرارية ${device.name}"
                                        )
                                    )
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (device.isThermal) Icons.Default.Print else Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (device.isThermal) Color(0xFF009639) else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(device.name, fontWeight = FontWeight.Bold)
                                    Text(device.address, fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            if (device.isThermal) {
                                Badge(containerColor = Color(0xFF009639), contentColor = Color.White) {
                                    Text("طابعة حرارية", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalSettings = currentSettings.copy(
                        paperWidth = paperWidth,
                        fontSize = fontSize,
                        printerName = selectedDevice?.name ?: currentSettings.printerName,
                        printerAddress = selectedDevice?.address ?: currentSettings.printerAddress
                    )
                    onSaveSettings(finalSettings)
                    onDismiss()
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ThermalReceiptPreviewDialog(
    title: String,
    contentLines: List<Pair<String, String>>,
    totalAmount: Double,
    settings: AppSettings,
    onConfirmPrint: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "معاينة السند الحراري",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "نوع الورق: ${settings.paperWidth} - الخط: ${settings.fontSize}pt",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // The Ticket Card Styled simulating thermal paper
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Jagged edge representation
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        ) {
                            val w = size.width
                            val steps = 30
                            val stepW = w / steps
                            val path = Path()
                            path.moveTo(0f, 6f)
                            for (i in 0..steps) {
                                val x = i * stepW
                                val y = if (i % 2 == 0) 0f else 6f
                                path.lineTo(x, y)
                            }
                            path.lineTo(w, 6f)
                            path.close()
                            drawPath(path, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Header
                        Text(
                            text = "وكالة طوفان الأقصى لأجود أنواع القات الصعدي",
                            fontWeight = FontWeight.Bold,
                            fontSize = (settings.fontSize + 2).sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            lineHeight = 22.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "صاحبها / أحمد منصور",
                            fontWeight = FontWeight.Bold,
                            fontSize = settings.fontSize.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            fontFamily = FontFamily.Monospace
                        )

                        // Simulated small Palestine Flag
                        PalestineFlag(
                            modifier = Modifier
                                .width(50.dp)
                                .height(30.dp)
                        )
                        Text(
                            text = "فلسطين حرة",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE4312B),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "------------------------------------", color = Color.DarkGray)
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = (settings.fontSize + 1).sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(text = "------------------------------------", color = Color.DarkGray)

                        // Data Table
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            contentLines.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = value,
                                        fontSize = settings.fontSize.sp,
                                        color = Color.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = label,
                                        fontSize = settings.fontSize.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Text(text = "====================================", color = Color.DarkGray)

                        // Total Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "%.2f ر.ي".format(totalAmount),
                                fontSize = (settings.fontSize + 2).sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "الإجمالي:",
                                fontSize = (settings.fontSize + 2).sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "شكراً لتعاملكم معنا",
                            fontSize = (settings.fontSize - 2).sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Bottom Jagged Cut
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        ) {
                            val w = size.width
                            val steps = 30
                            val stepW = w / steps
                            val path = Path()
                            path.moveTo(0f, 0f)
                            for (i in 0..steps) {
                                val x = i * stepW
                                val y = if (i % 2 == 0) 6f else 0f
                                path.lineTo(x, y)
                            }
                            path.lineTo(w, 0f)
                            path.close()
                            drawPath(path, color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmPrint()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009639))
            ) {
                Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("اطبع الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق المعاينة")
            }
        }
    )
}
