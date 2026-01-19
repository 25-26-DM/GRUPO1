package ec.edu.uce.appproductos.ui.location

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ec.edu.uce.appproductos.location.ForegroundOnlyLocationService
import ec.edu.uce.appproductos.location.SharedPreferenceUtil
import ec.edu.uce.appproductos.location.toText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var locationList by remember { mutableStateOf(emptyList<String>()) }
    var isTracking by remember { mutableStateOf(false) }
    
    var serviceBound by remember { mutableStateOf(false) }
    var locationService by remember { mutableStateOf<ForegroundOnlyLocationService?>(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder = service as ForegroundOnlyLocationService.LocalBinder
                locationService = binder.service
                serviceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                locationService = null
                serviceBound = false
            }
        }
    }

    val broadcastReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Usamos getParcelableExtra compatible con versiones antiguas y nuevas
                val location = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(ForegroundOnlyLocationService.EXTRA_LOCATION, Location::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(ForegroundOnlyLocationService.EXTRA_LOCATION)
                }
                
                location?.let {
                    locationList = listOf("Ubicación: ${it.toText()}") + locationList
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Si el usuario acepta el permiso desde el diálogo, NO iniciamos el seguimiento automáticamente
                // El usuario debe presionar el botón de nuevo para iniciar.
            }
        }
    )

    // Efecto para solicitar el permiso siempre al entrar
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(context) {
        val intent = Intent(context, ForegroundOnlyLocationService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        LocalBroadcastManager.getInstance(context).registerReceiver(
            broadcastReceiver,
            IntentFilter(ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )

        onDispose {
            if (serviceBound) {
                context.unbindService(serviceConnection)
            }
            LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Ubicación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (isTracking) {
                        locationService?.unsubscribeToLocationUpdates()
                        isTracking = false
                        locationList = emptyList()
                    } else {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            locationService?.subscribeToLocationUpdates()
                            isTracking = true
                        } else {
                            // Si no tiene el permiso, lo solicitamos de nuevo
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isTracking) "Detener Seguimiento" else "Iniciar Seguimiento")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isTracking) {
                if (locationList.isNotEmpty()) {
                    Text("Historial de Ubicaciones:", style = MaterialTheme.typography.titleMedium)
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(locationList) { locationText ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = locationText,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text("Obteniendo ubicación...")
                }
            }
        }
    }
}
