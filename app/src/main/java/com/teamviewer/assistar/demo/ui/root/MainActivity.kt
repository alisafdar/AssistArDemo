package com.teamviewer.assistar.demo.ui.root

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.teamviewer.assistar.demo.ui.theme.AssistArTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
            .launch(Manifest.permission.CAMERA)

        setContent {
            AssistArTheme {
                ComposeApp()
            }
        }
    }
}