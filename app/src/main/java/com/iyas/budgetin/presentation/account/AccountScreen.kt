package com.iyas.budgetin.presentation.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.iyas.budgetin.presentation.auth.AuthUiState
import com.iyas.budgetin.presentation.auth.AuthViewModel
import com.iyas.budgetin.presentation.home.BottomNavigationBar
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.ui.components.neoBrutalism
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val userEmail = user?.email ?: ""
    val displayName = user?.displayName
    // currentUser bisa null saat logout, jadi selalu sediakan fallback
    // agar userName tidak pernah kosong
    val userName = displayName?.takeIf { it.isNotBlank() }
        ?: userEmail.substringBefore("@").takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
        ?: "Pengguna"

    // Jangan render konten lagi setelah logout, tunggu navigasi ke login
    if (user == null) return

    AccountScreenContent(
        userName = userName,
        userEmail = userEmail,
        authUiState = authUiState,
        onNavigateToHome = onNavigateToHome,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToCharts = onNavigateToCharts,
        onLogoutConfirmed = {
            authViewModel.logout()
            onLogout()
        },
        onChangePassword = { oldPw, newPw, confirmPw ->
            authViewModel.changePassword(oldPw, newPw, confirmPw)
        },
        onClearError = authViewModel::clearError
    )
}

@Composable
fun AccountScreenContent(
    userName: String,
    userEmail: String,
    authUiState: AuthUiState,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onLogoutConfirmed: () -> Unit,
    onChangePassword: (String, String, String) -> Unit,
    onClearError: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    
    LaunchedEffect(authUiState.isPasswordChanged) {
        if (authUiState.isPasswordChanged) {
            showChangePasswordDialog = false
            currentPassword = ""
            newPassword = ""
            confirmNewPassword = ""
            onClearError()
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp),
            title = { Text("Keluar", color = SolidBlack, fontWeight = FontWeight.Black) },
            text = { Text("Apakah Anda yakin ingin keluar?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.neoBrutalism(cornerRadius = 8.dp, shadowOffset = 2.dp)
                ) {
                    Text("Keluar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    border = BorderStroke(2.dp, SolidBlack),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SolidBlack)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmNewPassword = ""
                onClearError()
            },
            containerColor = Color.White,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp),
            title = { Text("Ganti Password", color = SolidBlack, fontWeight = FontWeight.Black) },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; onClearError() },
                        label = { Text("Password Lama", fontWeight = FontWeight.Bold) },
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    if (currentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeoPurple,
                            unfocusedBorderColor = SolidBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; onClearError() },
                        label = { Text("Password Baru", fontWeight = FontWeight.Bold) },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeoPurple,
                            unfocusedBorderColor = SolidBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it; onClearError() },
                        label = { Text("Konfirmasi Password Baru", fontWeight = FontWeight.Bold) },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeoPurple,
                            unfocusedBorderColor = SolidBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (authUiState.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            authUiState.error,
                            color = ExpenseRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onChangePassword(currentPassword, newPassword, confirmNewPassword)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.neoBrutalism(cornerRadius = 8.dp, shadowOffset = 2.dp),
                    enabled = !authUiState.isLoading
                ) {
                    if (authUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showChangePasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                        confirmNewPassword = ""
                        onClearError()
                    },
                    border = BorderStroke(2.dp, SolidBlack),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SolidBlack),
                    enabled = !authUiState.isLoading
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "account",
                onHomeClick = onNavigateToHome,
                onHistoryClick = onNavigateToHistory,
                onChartsClick = onNavigateToCharts,
                onAccountClick = {}
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 6 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Bagian bawah tidak di-padding agar konten menggulir di belakang navbar
                    .padding(top = padding.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        "Akun Saya",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SolidBlack,
                        fontWeight = FontWeight.Black
                    )
                }

                // Profile Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 8.dp)
                        .background(NeoPink, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .border(3.dp, SolidBlack, CircleShape)
                                .background(NeoYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                userName.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = SolidBlack
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                userName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                userEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Menu Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .neoBrutalism(cornerRadius = 20.dp, shadowOffset = 6.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        AccountMenuItem(
                            icon = Icons.Default.Person,
                            label = "Profil",
                            subtitle = userName,
                            iconBgColor = NeoTeal
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 2.dp,
                            color = SolidBlack.copy(alpha = 0.1f)
                        )
                        AccountMenuItem(
                            icon = Icons.Default.Email,
                            label = "Email",
                            subtitle = userEmail,
                            iconBgColor = NeoYellow
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Info Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .neoBrutalism(cornerRadius = 20.dp, shadowOffset = 6.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        AccountMenuItem(
                            icon = Icons.Default.Info,
                            label = "Tentang Aplikasi",
                            subtitle = "BudgetIn v1.0",
                            iconBgColor = NeoPurple
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Change Password Button
                OutlinedButton(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, SolidBlack),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SolidBlack),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.LockReset,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = SolidBlack
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ganti Password",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = SolidBlack
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Logout Button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(56.dp)
                        .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Keluar",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(padding.calculateBottomPadding() + 40.dp))
            }
        }
    }
}

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    iconBgColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(2.dp, SolidBlack, RoundedCornerShape(12.dp))
                .background(iconBgColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SolidBlack, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = SolidBlack,
                fontWeight = FontWeight.Black
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    BudgetInTheme(darkTheme = false) {
        AccountScreenContent(
            userName = "Iyas",
            userEmail = "iyas@budgetin.com",
            authUiState = AuthUiState(),
            onNavigateToHome = {},
            onNavigateToHistory = {},
            onNavigateToCharts = {},
            onLogoutConfirmed = {},
            onChangePassword = { _, _, _ -> },
            onClearError = {}
        )
    }
}
