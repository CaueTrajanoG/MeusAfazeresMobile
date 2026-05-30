package com.example.meusafazeres.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Text(
        text = "Meus Afazeres",
        style = MaterialTheme.typography.headlineLarge,
        fontSize = 64.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 32.dp)
    )
}
