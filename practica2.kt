package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf("login") }
                var currentUser by remember { mutableStateOf<User?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "login" -> LoginScreen(onLoginSuccess = { user ->
                                currentUser = user
                                currentScreen = "dashboard"
                            })
                            "dashboard" -> DashboardScreen(
                                user = currentUser,
                                onNavigate = { currentScreen = it }
                            )
                            "profile" -> ProfileScreen(
                                user = currentUser,
                                onBack = { currentScreen = "dashboard" }
                            )
                            "admin" -> AdminPanel(
                                onBack = { currentScreen = "dashboard" }
                            )
                            "payments" -> PaymentScreen(
                                onBack = { currentScreen = "dashboard" }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class User(val username: String, val role: String, val token: String)

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    var userText by remember { mutableStateOf("") }
    var passText by remember { mutableStateOf("") }
    
    // Obtenemos el contexto para interactuar con el almacenamiento interno
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D47A1)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SISTEMA CORPORATIVO", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                TextField(value = userText, onValueChange = { userText = it }, label = { Text("Usuario") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = passText,
                    onValueChange = { passText = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (userText == "admin" && passText == "admin123") {
                            
                            // SOLUCIÓN: Cifrado automático de datos sensibles en el almacenamiento local
                            // Usamos las librerías nativas de Android Jetpack para asegurar los datos persistentes.
                            val masterKey = MasterKey.Builder(context)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) // Genera una llave maestra cifrada por hardware
                                .build()

                            val secureSharedPref = EncryptedSharedPreferences.create(
                                context,
                                "secure_user_session",
                                masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Cifra las etiquetas (keys)
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Cifra los valores (values)
                            )
                            with(secureSharedPref.edit()) {
                                putString("saved_username", userText)
                                putString("saved_password", passText) // <- Ahora viaja y se almacena 100% cifrado
                                apply()
                            }

                            onLoginSuccess(User("admin", "ADMIN", "SESSION_TOKEN"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ENTRAR")
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(user: User?, onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Bienvenido, ${user?.username}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { onNavigate("profile") }, modifier = Modifier.fillMaxWidth()) {
            Text("Ver Mi Perfil")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (user?.role == "ADMIN") {
            Button(onClick = { onNavigate("admin") }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth()) {
                Text("PANEL DE ADMINISTRACIÓN")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onNavigate("payments") }, modifier = Modifier.fillMaxWidth()) {
            Text("Configuración de Pagos")
        }
    }
}

@Composable
fun ProfileScreen(user: User?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
        Text("Mi Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre: ${user?.username}")
                Text("Rol: ${user?.role}")
            }
        }
    }
}

@Composable
fun PaymentScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
        Text("Configuración de Pasarela de Pago", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))
        Text("Estado de Stripe: Conectado")
    }
}

@Composable
fun AdminPanel(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFEBEE)).padding(16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
        Text("ÁREA RESTRINGIDA: ADMINISTRACIÓN", color = Color.Red, fontSize = 22.sp, fontWeight = FontWeight.Black)

        Spacer(modifier = Modifier.height(20.dp))
        Text("Usuarios Conectados actualmente:")
        val users = listOf("admin", "paco", "marta", "invitado")
        LazyColumn {
            items(users) { name ->
                Text("• $name")
            }
        }
    }
}