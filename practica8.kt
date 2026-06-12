package com.example.myapplication

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

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

// Se añade el campo password al modelo para simular la fuga de información sensible
data class User(val username: String, val role: String, val token: String, val passExposed: String)

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    var userText by remember { mutableStateOf("") }
    var passText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
                    visualTransformation = PasswordVisualTransformation() // El login aquí es seguro
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (userText == "admin" && passText == "SecurePass2026!") {
                            // Al iniciar sesión de forma correcta, guardamos los datos de la sesión
                            onLoginSuccess(User("admin", "ADMIN", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTgwMDAwMDAwMH0", passText))
                        } else {
                            errorMessage = "Usuario o contraseña incorrectos" 
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
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
        Text("Mi Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre: ${user?.username}")
                Text("Rol: ${user?.role}")

                /* 
                 * VULNERABILIDAD EXCLUSIVA: OWASP TOP 10 - Exposición de Datos Sensibles (Sensitive Data Exposure)
                 * 
                 * EXPLICACIÓN:
                 * La aplicación muestra datos privados de alta criticidad en la interfaz gráfica sin ningún tipo 
                 * de cifrado visual ni máscara. Expone directamente en texto plano:
                 * 1. El Token JWT de sesión completa (`user?.token`).
                 * 2. La contraseña actual de la cuenta (`user?.passExposed`).
                 * Esto permite que cualquier persona cercana que mire la pantalla (shoulder surfing) o cualquier 
                 * captura de pantalla accidental filtre credenciales de control que deberían permanecer ocultas o protegidas.
                 */
                Spacer(modifier = Modifier.height(16.dp))
                Text("⚠️ DATOS PRIVADOS EXPUESTOS:", color = Color.Red, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Contraseña actual:", fontWeight = FontWeight.SemiBold)
                Text("*********", color = Color.DarkGray) // Exposición de contraseña

                Spacer(modifier = Modifier.height(8.dp))
                Text("Token JWT de Sesión:", fontWeight = FontWeight.SemiBold)
                
                Text("eyJhbGci...", fontSize = 11.sp, color = Color.Gray) // Exposición de token corporativo
            }
        }
    }
}

@Composable
fun PaymentScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
        Text("Configuración de Pasarela de Pago", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Estado de Stripe: Conectado")
    }
}

@Composable
fun AdminPanel(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFEBEE)).padding(16.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
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
