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
    val userName = displayName.takeIf { !it.isNullOrBlank() }
        ?: userEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

    // Navigate to login when account is deleted
    LaunchedEffect(authUiState.isAccountDeleted) {
        if (authUiState.isAccountDeleted) {
            onLogout()
        }
    }

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
        onDeleteAccount = { password ->
            authViewModel.deleteAccount(password)
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
    onDeleteAccount: (String) -> Unit,
    onClearError: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var deletePasswordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

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

    // Delete Account Dialog with password input
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletePassword = ""
                onClearError()
            },
            containerColor = Color.White,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp),
            title = { Text("Hapus Akun", color = ExpenseRed, fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text(
                        "Tindakan ini tidak dapat dibatalkan. Masukkan password Anda untuk konfirmasi.",
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it; onClearError() },
                        label = { Text("Password", fontWeight = FontWeight.Bold) },
                        visualTransformation = if (deletePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { deletePasswordVisible = !deletePasswordVisible }) {
                                Icon(
                                    if (deletePasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (deletePasswordVisible) "Sembunyikan" else "Tampilkan",
                                    tint = TextSecondary
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ExpenseRed,
                            unfocusedBorderColor = SolidBlack,
                            focusedLabelColor = ExpenseRed,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = SolidBlack,
                            unfocusedTextColor = SolidBlack,
                            cursorColor = ExpenseRed
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
                        onDeleteAccount(deletePassword)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
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
                        Text("Hapus Akun", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        deletePassword = ""
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
                    .padding(padding)
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
                                userName.first().uppercase(),
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

                Spacer(Modifier.height(12.dp))

                // Delete Account Button
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, ExpenseRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ExpenseRed
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Hapus Akun",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = ExpenseRed
                    )
                }

                Spacer(Modifier.height(100.dp))
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
            onDeleteAccount = {},
            onClearError = {}
        )
    }
}
