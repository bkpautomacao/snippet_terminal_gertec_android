package br.com.gabrielmorais.terminalgertec.di

import br.com.gabrielmorais.terminalgertec.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(get())
    }
}