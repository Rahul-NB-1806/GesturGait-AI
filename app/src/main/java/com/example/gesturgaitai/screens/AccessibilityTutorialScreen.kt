package com.example.gesturgaitai.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gesturgaitai.components.AppleBackground
import com.example.gesturgaitai.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityTutorialScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AppleBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Setup Guide", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.TouchApp,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Enable Passive Monitoring",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    "To analyze your motor patterns, GesturGait needs accessibility permission to collect metadata during use.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(40.dp))

                TutorialStep(
                    number = "1",
                    icon = Icons.Default.Settings,
                    title = "Open Settings",
                    description = "Tap the button below to go to Android Accessibility settings."
                )

                TutorialStep(
                    number = "2",
                    icon = Icons.Default.Check,
                    title = "Find GesturGait AI",
                    description = "Look for 'GesturGait AI' under 'Downloaded' or 'Installed' services."
                )

                TutorialStep(
                    number = "3",
                    icon = Icons.Default.TouchApp,
                    title = "Toggle Switch",
                    description = "Turn on the main switch. We never collect typed text or private messages."
                )

                TutorialStep(
                    number = "4",
                    icon = Icons.Default.BatteryFull,
                    title = "Background Activity",
                    description = "Change battery settings to 'No Restrictions' so monitoring can run 24/7."
                )

                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Grant Accessibility", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Allow Background Use", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDone,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("I've already enabled it", color = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TutorialStep(
    number: String,
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
