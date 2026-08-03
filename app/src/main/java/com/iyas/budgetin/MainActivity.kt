package com.iyas.budgetin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.iyas.budgetin.presentation.navigation.BudgetInNavGraph
import com.iyas.budgetin.ui.theme.BudgetInTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mencegah force close akibat bug dari Google Play Services 
        // saat menghapus akun Firebase.
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val isGmsBug = throwable.stackTraceToString().contains("com.google.android.gms")
            if (isGmsBug) {
                // Abaikan crash dari GMS agar tidak force close
                android.util.Log.e("BudgetIn", "Swallowed GMS crash bug", throwable)
            } else {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        enableEdgeToEdge()
        setContent {
            BudgetInTheme(darkTheme = true) {
                BudgetInNavGraph()
            }
        }
    }
}