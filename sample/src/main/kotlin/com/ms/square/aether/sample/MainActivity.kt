package com.ms.square.aether.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ms.square.aether.sample.ui.theme.AetherTheme

class MainActivity : ComponentActivity() {

  @OptIn(ExperimentalMaterial3Api::class)
  @Suppress("LongMethod")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      AetherTheme {
        val demos = remember { weatherEffects }
        // -1 = gallery, 0..n = playground for that index
        var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            TopAppBar(
              title = {
                Text(
                  when {
                    selectedTab == 1 -> "Map Showcase"
                    selectedIndex >= 0 -> demos[selectedIndex].name
                    else -> "Aether Showcase"
                  }
                )
              },
              navigationIcon = {
                if (selectedTab == 0 && selectedIndex >= 0) {
                  IconButton(onClick = { selectedIndex = -1 }) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Back"
                    )
                  }
                }
              }
            )
          },
          bottomBar = {
            NavigationBar {
              NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                label = { Text("Effects") }
              )
              NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                label = { Text("Map") }
              )
            }
          }
        ) { innerPadding ->
          when (selectedTab) {
            0 -> {
              if (selectedIndex < 0) {
                EffectGallery(
                  demos = demos,
                  onEffectSelected = { selectedIndex = it },
                  contentPadding = innerPadding
                )
              } else {
                EffectPlayground(demo = demos[selectedIndex], contentPadding = innerPadding)
              }
            }
            1 -> MapShowcase(contentPadding = innerPadding)
          }
        }
      }
    }
  }
}
