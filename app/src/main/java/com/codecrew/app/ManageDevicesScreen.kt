package com.codecrew.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codecrew.app.model.CustomerData
import com.codecrew.app.model.RetrofitClient
import com.codecrew.app.utils.UserPreferences
import kotlinx.coroutines.launch

data class Device(val id: String, val name: String, var isPreferred: Boolean = false)

class ManageDevicesViewModel : ViewModel() {
    private val _devices = mutableStateListOf(
        Device("device1", "My Phone", true),
        Device("device2", "My Tablet", false),
        Device("device3", "Work Laptop", false)
    )
    val devices: List<Device> = _devices

    private val _preferredDeviceId = mutableStateOf(_devices.firstOrNull { it.isPreferred }?.id)
    val preferredDeviceId: State<String?> = _preferredDeviceId

    fun setPreferredDevice(deviceId: String, custId: String?) {
        _devices.forEach { device ->
            device.isPreferred = device.id == deviceId
        }
        _preferredDeviceId.value = deviceId
        val customerApi = RetrofitClient.create()

        viewModelScope.launch {
            try {
                val response: CustomerData = customerApi.updatePreferDevice(
                    custId = custId,
                    customerData = CustomerData(
                        preferredDeviceFlag = true,
                        deviceId = deviceId
                    )
                )
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun addDevice(name: String) {
        val newId = "device${_devices.size + 1}" // Simple ID generation
        _devices.add(Device(newId, name))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDevicesScreen(
    navController: NavController,
    manageDevicesViewModel: ManageDevicesViewModel = viewModel()
) {
    val devices = manageDevicesViewModel.devices
    val preferredDeviceId by manageDevicesViewModel.preferredDeviceId
    val custId = UserPreferences.getCustId(LocalContext.current.applicationContext)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Devices") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Current Devices:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (devices.isEmpty()) {
                Text("No devices found.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(devices, key = { it.id }) { device ->
                        DeviceItem(
                            device = device,
                            isSelected = device.id == preferredDeviceId,
                            onSelected = {
                                manageDevicesViewModel.setPreferredDevice(device.id, custId)
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(device: Device, isSelected: Boolean, onSelected: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = device.name + if (device.isPreferred) " (Preferred)" else "",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}