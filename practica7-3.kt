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

data class User(val username: String, val role: String, val token: String)

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
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        /* 
                         * VULNERABILIDAD AÑADIDA: OWASP TOP 10 - Fallas en la Validación de Entrada (Input Validation Failures)
                         * 
                         * EXPLICACIÓN:
                         * La aplicación procesa los datos introducidos en 'userText' y 'passText' directamente sin sanitizar, 
                         * sin verificar la longitud y sin filtrar caracteres especiales o secuencias de escape de bases de datos o comandos.
                         * 
                         * En este ejemplo, el sistema acepta cualquier tipo de entrada. Si un usuario introduce caracteres maliciosos 
                         * como comillas simples ('), operadores lógicos (OR 1=1) o scripts, la aplicación los concatena de forma insegura.
                         * Esto abre la puerta a ataques de Inyección SQL/NoSQL si estos datos se envían a un backend, o a fallos de 
                         * desbordamiento de memoria interna y denegación de servicio (DoS) local si se introducen millones de caracteres.
                         */
                        val queryUser = userText
                        val queryPass = passText

                        // Simulación de una validación vulnerable que acepta cualquier cadena sin verificar formato ni estructura
                        if (queryUser == "admin" && queryPass == "SecurePass2026!") {
                            onLoginSuccess(User("admin", "ADMIN", "SESSION_TOKEN"))
                        } else if (queryUser.contains("' OR '1'='1") || queryPass.contains("' OR '1'='1")) {
                            // Simulación del impacto: La falta de validación de entrada permite que un bypass por inyección 
                            // sea interpretado positivamente por la lógica del sistema, otorgando acceso de administrador.
                            onLoginSuccess(User("admin_injected", "ADMIN", "INJECTED_TOKEN"))
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