package com.iyas.budgetin.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.ui.components.neoBrutalism
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    RegisterScreenContent(
        uiState = uiState,
        onNavigateToLogin = onNavigateToLogin,
        onRegisterSuccess = onRegisterSuccess,
        onRegister = viewModel::register,
        onClearError = viewModel::clearError
    )
}

@Composable
fun RegisterScreenContent(
    uiState: AuthUiState,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onClearError: () -> Unit,
    animateEntry: Boolean = true
) {
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(!animateEntry) }
    val focusManager = LocalFocusManager.current

    if (animateEntry) {
        LaunchedEffect(Unit) { delay(100); visible = true }
    }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))

                // Back button
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier
                            .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 2.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = SolidBlack)
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .neoBrutalism(cornerRadius = 22.dp, shadowOffset = 6.dp)
                        .background(NeoTeal, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("₿", fontSize = 34.sp, color = Color.White, fontWeight = FontWeight.Black)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Buat Akun Baru",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SolidBlack,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Mulai perjalanan keuangan Anda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 8.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        BudgetTextField(
                            value = nickname,
                            onValueChange = { nickname = it; onClearError() },
                            label = "Nama Panggilan",
                            leadingIcon = Icons.Default.Person,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Spacer(Modifier.height(16.dp))

                        BudgetTextField(
                            value = email,
                            onValueChange = { email = it; onClearError() },
                            label = "Email",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Spacer(Modifier.height(16.dp))

                        BudgetTextField(
                            value = password,
                            onValueChange = { password = it; onClearError() },
                            label = "Password",
                            leadingIcon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                        )

                        Spacer(Modifier.height(16.dp))

                        BudgetTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; onClearError() },
                            label = "Konfirmasi Password",
                            leadingIcon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            onImeAction = { focusManager.clearFocus(); onRegister(email, password, confirmPassword, nickname) },
                            isPassword = true,
                            passwordVisible = confirmPasswordVisible,
                            onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
                        )

                        AnimatedVisibility(visible = uiState.error != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 2.dp)
                                    .background(ExpenseRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = uiState.error ?: "",
                                    color = ExpenseRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { focusManager.clearFocus(); onRegister(email, password, confirmPassword, nickname) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeoPurple),
                            contentPadding = PaddingValues(0.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    "Daftar",
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Sudah punya akun? ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Masuk", color = NeoPink, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    BudgetInTheme {
        RegisterScreenContent(
            uiState = AuthUiState(),
            onNavigateToLogin = {},
            onRegisterSuccess = {},
            onRegister = { _, _, _, _ -> },
            onClearError = {},
            animateEntry = false
        )
    }
}
