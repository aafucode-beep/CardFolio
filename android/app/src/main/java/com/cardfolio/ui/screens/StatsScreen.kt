package com.cardfolio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.cardfolio.data.CategoryStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    var stats by remember { mutableStateOf<List<CategoryStats>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            stats = withContext(Dispatchers.IO) { ApiClient.api.getCategoryStats() }
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("统计") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                stats.isEmpty() -> Text("暂无数据", color = Color.Gray)
                else -> {
                    val total = stats.sumOf { it.count }
                    val maxCount = stats.maxOf { it.count }
                    val labels = mapOf(
                        "supplier" to "供应商",
                        "competitor" to "竞争对手",
                        "domestic" to "国内客户",
                        "international" to "国外客户",
                        "other" to "其他",
                    )

                    Text("共 $total 张名片", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    stats.forEach { stat ->
                        val label = labels[stat.category] ?: stat.category
                        val ratio = if (maxCount > 0) stat.count.toFloat() / maxCount else 0f

                        Column {
                            Text(
                                "$label: ${stat.count} 张",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
