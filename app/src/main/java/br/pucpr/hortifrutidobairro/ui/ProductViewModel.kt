package br.pucpr.hortifrutidobairro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.hortifrutidobairro.data.Product
import br.pucpr.hortifrutidobairro.data.ProductDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel: keeps the screen data and survives screen rotation.
// The screen only reads the state and calls these functions (MVVM).
class ProductViewModel : ViewModel() {

    private var dao: ProductDao? = null

    // StateFlow: holds the current value and notifies the screen on every change.
    // The "_" version is private and mutable; the public one is read-only.
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _donations = MutableStateFlow<List<Product>>(emptyList())
    val donations: StateFlow<List<Product>> = _donations

    // Called from setContent, which runs again on every recomposition.
    // The early return avoids starting duplicate collections.
    fun setDao(dao: ProductDao) {
        if (this.dao != null) return
        this.dao = dao

        // Collect the DAO Flows and copy each new list into the StateFlows.
        viewModelScope.launch {
            dao.listAll().collect { _products.value = it }
        }
        viewModelScope.launch {
            dao.listDonations().collect { _donations.value = it }
        }
    }

    // viewModelScope.launch starts a coroutine to call the suspend DAO functions.
    fun addProduct(product: Product) {
        viewModelScope.launch { dao?.insert(product) }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch { dao?.update(product) }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { dao?.delete(product) }
    }
}
