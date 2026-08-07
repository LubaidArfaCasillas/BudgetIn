package com.iyas.budgetin.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.iyas.budgetin.data.repository.CategoryRepositoryImpl
import com.iyas.budgetin.data.repository.TransactionRepositoryImpl
import com.iyas.budgetin.domain.repository.CategoryRepository
import com.iyas.budgetin.domain.repository.TransactionRepository
import com.iyas.budgetin.presentation.auth.AuthViewModel
import com.iyas.budgetin.presentation.charts.ChartsViewModel
import com.iyas.budgetin.presentation.home.HomeViewModel
import com.iyas.budgetin.presentation.transaction.TransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase instances
    single<FirebaseAuth> { Firebase.auth }
    single<FirebaseFirestore> { Firebase.firestore }

    // Repositories
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { TransactionViewModel(get(), get()) }
    viewModel { ChartsViewModel(get()) }
}
