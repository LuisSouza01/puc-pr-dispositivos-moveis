package br.pucpr.hortifrutidobairro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import br.pucpr.hortifrutidobairro.data.AppDatabase
import br.pucpr.hortifrutidobairro.ui.ProductScreen
import br.pucpr.hortifrutidobairro.ui.ProductViewModel
import br.pucpr.hortifrutidobairro.ui.theme.HortifrutiDoBairroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Creates (or opens) the SQLite database file "hortifruti_db" with Room.
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "hortifruti_db"
        ).build()

        setContent {
            HortifrutiDoBairroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // viewModel() returns the same instance across recompositions and rotation.
                    val viewModel: ProductViewModel = viewModel()
                    // Runs on every recomposition, but setDao ignores repeated calls.
                    viewModel.setDao(db.productDao())
                    ProductScreen(viewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}
