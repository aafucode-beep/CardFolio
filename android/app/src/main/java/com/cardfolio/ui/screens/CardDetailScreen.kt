package com.cardfolio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardfolio.data.ApiClient
import com.cardfolio.data.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    onBack: () -> Unit,
) {
    var card by remember { mutableStateOf<Card?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(cardId) {
        try {
            card = withContext(Dispatchers.IO) { ApiClient.api.getCard(cardId) }
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("名片详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        }
    ) { padding ->
        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            card != null -> {
                CardDetailContent(
                    card = card!!,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun CardDetailContent(card: Card, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Name + Title
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    card.person_name ?: "未知",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (!card.title.isNullOrBlank()) {
                    Text(card.title, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                if (!card.company.isNullOrBlank()) {
                    Text(card.company, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(card.category.displayName) },
                )
            }
        }

        // Contact info
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("联系方式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                card.phone?.forEach { phone ->
                    ContactRow(Icons.Default.Call, phone) {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }
                }
                card.email?.forEach { email ->
                    ContactRow(Icons.Default.Email, email) {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:$email"))
                        context.startActivity(intent)
                    }
                }
                if (!card.website.isNullOrBlank()) {
                    ContactRow(Icons.Default.Language, card.website) {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(card.website))
                        context.startActivity(intent)
                    }
                }
                if (!card.address.isNullOrBlank()) {
                    ContactRow(Icons.Default.LocationOn, card.address) {}
                }
                if (!card.country.isNullOrBlank()) {
                    ContactRow(Icons.Default.LocationOn, "国家: ${card.country}") {}
                }
            }
        }

        // Notes
        if (!card.notes.isNullOrBlank()) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("备注", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(card.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (card.needs_review) {
            Text(
                "⚠️ 此名片 OCR 置信度较低，建议核对信息",
                color = Color(0xFFF59E0B),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.height(20.dp), tint = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(
            text,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (onClick != {}) MaterialTheme.colorScheme.primary else Color.Unspecified,
        )
        if (onClick != {}) {
            androidx.compose.material3.TextButton(onClick = onClick) { Text("打开") }
        }
    }
}
