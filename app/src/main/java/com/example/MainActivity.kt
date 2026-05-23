package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.data.db.BePresentDatabase
import com.example.data.repository.BePresentRepository
import com.example.ui.screens.BePresentMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BePresentViewModel
import com.example.ui.viewmodel.BePresentViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val database: BePresentDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            BePresentDatabase::class.java,
            "bepresent_wellness.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    private val repository: BePresentRepository by lazy {
        BePresentRepository(database)
    }

    private val viewModel: BePresentViewModel by viewModels {
        BePresentViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Suppress or handle window edge system bar bleed
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                BePresentMainScreen(viewModel = viewModel)
            }
        }
    }
}
