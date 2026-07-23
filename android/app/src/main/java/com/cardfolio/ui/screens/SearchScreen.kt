package com.cardfolio.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cardfolio.data.ApiClient
import com.cardfolio.data.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onCardClick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Card>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }

    // Debounce search
    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            searched = false
            return@LaunchedEffect
        }
        delay(400)  // debounce
        loading = true
        searched = true
        try {
            val resp = withContext(Dispatchers.IO) {
                ApiClient.api.searchCards(query)
            }
            results = resp.items
        } catch (_: Exception) {
            results = emptyList()
        }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("搜索名片") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("搜索公司、姓名、电话...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (loading) {
                Text("搜索中...", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (searched && !loading && results.isEmpty()) {
                Text("未找到匹配的名片", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (results.isNotEmpty()) {
                Text("找到 ${results.size} 条结果", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelMedium)
                LazyColumn {
                    items(results) { card ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCardClick(card.id) }
                                .padding(vertical = 12.dp),
                        ) {
                            Text(card.person_name ?: "未知", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${card.company ?: ""} ${card.title ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
