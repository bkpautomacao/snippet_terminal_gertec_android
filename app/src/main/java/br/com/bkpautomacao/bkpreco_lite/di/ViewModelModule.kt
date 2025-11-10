package br.com.bkpautomacao.bkpreco_lite.di

import br.com.bkpautomacao.bkpreco_lite.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(get(), get())
    }
}