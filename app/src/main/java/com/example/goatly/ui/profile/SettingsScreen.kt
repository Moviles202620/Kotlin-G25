package com.example.goatly.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goatly.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangePassword: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.W800) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
            )
        },
        containerColor = AppColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = AppColors.Surface,
                shadowElevation = 4.dp
            ) {
                Column {
                    Text(
                        "SEGURIDAD",
                        fontSize = 12.sp,
                        letterSpacing = 1.4.sp,
                        color = AppColors.GreyText,
                        fontWeight = FontWeight.W800,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    HorizontalDivider(color = AppColors.Border)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChangePassword() }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppColors.DarkText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Cambiar contraseña",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = AppColors.GreyText
                        )
                    }
                }
            }
        }
    }
}
