package com.kyrnforge.dsp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.kyrnforge.dsp.audio.DspAudioEngine
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyrnforge.dsp.audio.DeviceCategory
import com.kyrnforge.dsp.ui.components.ArcRotaryKnob
import com.kyrnforge.dsp.ui.components.DeviceStatusCard
import com.kyrnforge.dsp.ui.components.DspTuningSliders
import com.kyrnforge.dsp.ui.components.PresetSelector
import com.kyrnforge.dsp.ui.components.SpectrumVisualizer
import com.kyrnforge.dsp.ui.theme.BorderSubtle
import com.kyrnforge.dsp.ui.theme.CyberAmber
import com.kyrnforge.dsp.ui.theme.ElectricCyan
import com.kyrnforge.dsp.ui.theme.ObsidianBg
import com.kyrnforge.dsp.ui.theme.SurfaceCard
import com.kyrnforge.dsp.ui.theme.SurfaceDark
import com.kyrnforge.dsp.ui.theme.SurfaceElevated
import com.kyrnforge.dsp.ui.theme.TextMuted
import com.kyrnforge.dsp.ui.theme.TextPrimary
import com.kyrnforge.dsp.ui.theme.TextSecondary

@Composable
fun MainScreen(
    viewModel: AuraSoundViewModel
) {
    val isEnabled by viewModel.isEnabled.collectAsState()
    val boostPercent by viewModel.boostPercent.collectAsState()
    val antiDistortionActive by viewModel.antiDistortionActive.collectAsState()
    val vocalClarityPercent by viewModel.vocalClarityPercent.collectAsState()
    val bassResonancePercent by viewModel.bassResonancePercent.collectAsState()
    val spatialPercent by viewModel.spatialPercent.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()
    val currentDevice by viewModel.currentDevice.collectAsState()
    val spectrum by viewModel.spectrum.collectAsState()

    var showSafetyDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                isEnabled = isEnabled,
                onPowerToggle = { viewModel.togglePower() },
                onInfoClick = { showSafetyDialog = true }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DeviceStatusCard(
                    device = currentDevice,
                    onRefresh = { viewModel.refreshDevices() }
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ArcRotaryKnob(
                        valuePercent = boostPercent,
                        gainDb = DspAudioEngine.boostGainMb(boostPercent, currentPreset) / 100f,
                        onValueChange = { viewModel.setBoost(it) },
                        enabled = isEnabled
                    )
                }
            }

            item {
                SpectrumVisualizer(
                    frequencyBands = spectrum,
                    enabled = isEnabled
                )
            }

            item {
                PresetSelector(
                    selectedPreset = currentPreset,
                    onPresetSelect = { viewModel.selectPreset(it) },
                    enabled = isEnabled
                )
            }

            item {
                DspTuningSliders(
                    antiDistortionActive = antiDistortionActive,
                    onAntiDistortionChange = { viewModel.setAntiDistortion(it) },
                    vocalClarityPercent = vocalClarityPercent,
                    onVocalClarityChange = { viewModel.setVocalClarity(it) },
                    bassResonancePercent = bassResonancePercent,
                    onBassResonanceChange = { viewModel.setBass(it) },
                    spatialPercent = spatialPercent,
                    onSpatialChange = { viewModel.setSpatial(it) },
                    isSpeaker = currentDevice.category == DeviceCategory.INTERNAL_SPEAKER,
                    enabled = isEnabled
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { showSafetyDialog = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyberAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Algoritmo DRC activo: Protege los transitorios de tus altavoces y previene saturación.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    if (showSafetyDialog) {
        AlertDialog(
            onDismissRequest = { showSafetyDialog = false },
            title = {
                Text(
                    text = "Protección de Salud Auditiva",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "AuraSound DSP procesa el audio en tiempo real mediante algoritmos de compresión de rango dinámico (DRC) y limitador de picos brickwall. " +
                            "Esto maximiza la potencia limpia percibida sin recortar la onda en distorsión.\n\n" +
                            "Recomendamos mantener niveles auditivos responsables al usar auriculares para proteger tu salud auditiva a largo plazo.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showSafetyDialog = false }) {
                    Text("Entendido", color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun TopAppBar(
    isEnabled: Boolean,
    onPowerToggle: () -> Unit,
    onInfoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(ObsidianBg)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AURASOUND",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DSP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricCyan
                        )
                    }
                }
                Text(
                    text = if (isEnabled) "PROCESAMIENTO DINÁMICO ACTIVO" else "BYPASS DESACTIVADO",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) ElectricCyan else TextMuted,
                    letterSpacing = 1.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isEnabled) ElectricCyan.copy(alpha = 0.15f) else SurfaceCard)
                        .border(
                            1.5.dp,
                            if (isEnabled) ElectricCyan else BorderSubtle,
                            CircleShape
                        )
                        .clickable { onPowerToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Encender/Apagar DSP",
                        tint = if (isEnabled) ElectricCyan else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
