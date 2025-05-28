package com.example.startupidea.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.startupidea.R
import com.example.startupidea.ui.viewmodel.AuthViewModel
import com.example.startupidea.data.model.AuthResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    // Tambahkan state untuk validasi input
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val authResult by authViewModel.authResult.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val pendingConfirmationEmail by authViewModel.pendingConfirmationEmail.collectAsState()

    // Efek untuk menangani login sukses
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_startupidea),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Kata pembuka
            Text(
                text = "Wujudkan Ide Startup Anda",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFFF9800),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Platform untuk berbagi dan mengembangkan ide startup inovatif",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card untuk form login
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUp) "Daftar Akun" else "Login",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null // Reset error saat input berubah
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        supportingText = {
                            emailError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFF9800),
                            unfocusedBorderColor = Color.Black,
                            cursorColor = Color(0xFFFF9800),
                            focusedLabelColor = Color(0xFFFF9800)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null // Reset error saat input berubah
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError != null,
                        supportingText = {
                            passwordError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFF9800),
                            unfocusedBorderColor = Color.Black,
                            cursorColor = Color(0xFFFF9800),
                            focusedLabelColor = Color(0xFFFF9800)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tampilkan pesan error dari AuthResult
                    if (authResult is AuthResult.Error) {
                        Text(
                            text = (authResult as AuthResult.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Tampilkan pesan konfirmasi email jika diperlukan
                    if (authResult is AuthResult.EmailConfirmationNeeded && pendingConfirmationEmail != null) {
                        AlertDialog(
                            onDismissRequest = { authViewModel.clearPendingConfirmation() },
                            title = { Text("Konfirmasi Email") },
                            text = {
                                Column {
                                    Text("Silakan cek email Anda di:")
                                    Text(
                                        text = pendingConfirmationEmail!!,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Klik link konfirmasi yang telah kami kirim untuk mengaktifkan akun Anda.")
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = { authViewModel.clearPendingConfirmation() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF9800)
                                    )
                                ) {
                                    Text("OK")
                                }
                            },
                            containerColor = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            // Validasi input
                            var isValid = true

                            if (email.isBlank()) {
                                emailError = "Email tidak boleh kosong"
                                isValid = false
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                                    .matches()
                            ) {
                                emailError = "Format email tidak valid"
                                isValid = false
                            }

                            if (password.isBlank()) {
                                passwordError = "Password tidak boleh kosong"
                                isValid = false
                            } else if (password.length < 6) {
                                passwordError = "Password minimal 6 karakter"
                                isValid = false
                            }

                            if (isValid) {
                                coroutineScope.launch {
                                    if (isSignUp) {
                                        authViewModel.signUp(email, password)
                                    } else {
                                        authViewModel.signIn(email, password)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = authResult !is AuthResult.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (authResult is AuthResult.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                if (isSignUp) "Daftar" else "Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { isSignUp = !isSignUp },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFF9800)
                        )
                    ) {
                        Text(
                            if (isSignUp) "Sudah punya akun? Login" else "Belum punya akun? Daftar",
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }
        }
    }
}