package com.kyrnforge.dsp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.kyrnforge.dsp.ui.AuraSoundViewModel
import com.kyrnforge.dsp.ui.MainScreen
import com.kyrnforge.dsp.ui.theme.AuraSoundTheme
import com.kyrnforge.dsp.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private val viewModel: AuraSoundViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onPermissionsGranted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestNecessaryPermissions()

        setContent {
            AuraSoundTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    private fun requestNecessaryPermissions() {
        val permissions = PermissionHelper.getRequiredPermissions().toTypedArray()
        permissionLauncher.launch(permissions)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}
