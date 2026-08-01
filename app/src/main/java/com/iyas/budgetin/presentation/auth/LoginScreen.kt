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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LoginScreenContent(
        uiState = uiState,
        onNavigateToRegister = onNavigateToRegister,
        onLoginSuccess = onLoginSuccess,
        onLogin = viewModel::login,
        onClearError = viewModel::clearError
    )
}

@Composable
fun LoginScreenContent(
    uiState: AuthUiState,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onLogin: (String, String) -> Unit,
    onClearError: () -> Unit,
    animateEntry: Boolean = true
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(!animateEntry) }
    val focusManager = LocalFocusManager.current

    if (animateEntry) {
        LaunchedEffect(Unit) { delay(100); visible = true }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(60.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 6.dp, shadowColor = SolidBlack)
                        .background(NeoPink, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("₿", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Black)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "BudgetIn",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SolidBlack,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    "Kelola keuangan Anda dengan cerdas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 8.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Masuk",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Selamat datang kembali!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

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
                            imeAction = ImeAction.Done,
                            onImeAction = { focusManager.clearFocus(); onLogin(email, password) },
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                        )

                        // Error
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
                            onClick = { focusManager.clearFocus(); onLogin(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeoYellow),
                            contentPadding = PaddingValues(0.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = SolidBlack,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    "Masuk",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = SolidBlack
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
                    Text("Belum punya akun? ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Daftar Sekarang", color = NeoPink, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    BudgetInTheme {
        LoginScreenContent(
            uiState = AuthUiState(),
            onNavigateToRegister = {},
            onLoginSuccess = {},
            onLogin = { _, _ -> },
            onClearError = {},
            animateEntry = false
        )
    }
}

