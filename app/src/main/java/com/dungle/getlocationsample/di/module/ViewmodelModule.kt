package com.dungle.getlocationsample.di.module

import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SessionViewModel(get()) }
}