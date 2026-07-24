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
        enableEdgeToEdge()
        setContent {
            BudgetInTheme(darkTheme = true) {
                BudgetInNavGraph()
            }
        }
    }
}