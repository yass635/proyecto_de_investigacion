import android.os.Bundle
import android.text.Html
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
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
                                user = currentUser,
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
                            // Simulamos que el administrador configuró un nombre con etiquetas HTML maliciosas
                            onLoginSuccess(User("<b>Admin</b> <img src='x' onerror='alert(1)'>", "ADMIN", "SESSION_TOKEN"))
                        } else if (userText == "paco" && passText == "paco123") {
                            onLoginSuccess(User("paco", "USER", "SESSION_TOKEN_2"))
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
                
                // VULNERABILIDAD SIMPLIFICADA: 
                // Se utiliza el método antiguo descatalogado "Html.fromHtml(string)" sin flags de seguridad.
                // Si el nombre del usuario contiene código malicioso de un servidor, este componente desactualizado lo procesará.
                val oldDectecatedComponent = Html.fromHtml(user?.username ?: "")
                
                Text("Nombre: $oldDectecatedComponent")
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
fun AdminPanel(user: User?, onBack: () -> Unit) {
    if (user == null || user.role != "ADMIN") {
        LaunchedEffect(Unit) {
            onBack()
        }
        return
    }

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
