package com.example.sih_2026.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TrustedContact(val name: String, val relation: String, val phoneNumber: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedContactsScreen(
    onBackClick: () -> Unit,
    onCallVerified: (TrustedContact) -> Unit,
) {
    val contacts = remember {
        mutableStateListOf(
            TrustedContact("Amit (Brother)", "Brother", "+91 98765 43210"),
            TrustedContact("Papa", "Father", "+91 91234 56789"),
            TrustedContact("Mummy", "Mother", "+91 99887 76655"),
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("👥 Trusted Circle (Independent Verification)") },
                    navigationIcon = {
                        TextButton(onClick = onBackClick) {
                            Text("Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                Text(
                    text = "Use Trusted Contacts to independently call and verify a caller's identity when suspicious AI voice characteristics are detected.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(contacts) { contact ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(text = "${contact.relation} • ${contact.phoneNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = { onCallVerified(contact) }) {
                                    Text("Call Real Number")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
