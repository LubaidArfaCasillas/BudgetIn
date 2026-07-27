package com.iyas.budgetin.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.iyas.budgetin.ui.theme.*
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
            .background(
                Brush.linearGradient(
                    colors = listOf(BackgroundDark, Color(0xFF0D2137)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative glow circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GreenPrimary.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GreenSecondary.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

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
                        .background(
                            Brush.linearGradient(listOf(GreenPrimary, GreenSecondary)),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("₿", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "BudgetIn",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    "Kelola keuangan Anda dengan cerdas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
                )

                // Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Masuk",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
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

                        Spacer(Modifier.height(12.dp))

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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = ExpenseRed.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = uiState.error ?: "",
                                    color = ExpenseRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { focusManager.clearFocus(); onLogin(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            enabled = !uiState.isLoading
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(GreenPrimary, GreenSecondary)),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Masuk",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Belum punya akun? ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Daftar Sekarang", color = GreenPrimary, fontWeight = FontWeight.SemiBold)
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
