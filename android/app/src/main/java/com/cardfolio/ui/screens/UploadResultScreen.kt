package com.cardfolio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardfolio.data.ApiClient
import com.cardfolio.data.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun UploadResultScreen(
    cardId: String,
    onDone: () -> Unit,
) {
    var card by remember { mutableStateOf<Card?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(cardId) {
        try {
            card = withContext(Dispatchers.IO) { ApiClient.api.getCard(cardId) }
        } catch (_: Exception) {}
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("加载识别结果...")
        } else if (card != null) {
            val c = card!!
            val success = !c.needs_review

            Icon(
                if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (success) Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.height(64.dp),
            )
            Spacer(Modifier.height(16.dp))

            Text(
                if (success) "识别成功！" else "需人工核对",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow("姓名", c.person_name)
                    InfoRow("职位", c.title)
                    InfoRow("公司", c.company)
                    InfoRow("电话", c.phone?.joinToString(", "))
                    InfoRow("邮箱", c.email?.joinToString(", "))
                    InfoRow("地址", c.address)
                    InfoRow("国家", c.country)
                    InfoRow("分类", c.category.displayName)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("完成")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
