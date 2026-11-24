package br.pucpr.hortifrutidobairro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.pucpr.hortifrutidobairro.data.Product

// Single screen of the app: form + summary panel + product list.
@Composable
fun ProductScreen(viewModel: ProductViewModel, modifier: Modifier) {

    // collectAsState turns the StateFlow into Compose state:
    // when the list changes, the screen is redrawn (recomposition).
    val products by viewModel.products.collectAsState()
    val donations by viewModel.donations.collectAsState()

    // Local form state. remember keeps the value between recompositions.
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var localProducer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fruta") }
    var availableForDonation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // null = create mode; any id = editing that product.
    var selectedId by remember { mutableStateOf<Int?>(null) }

    // true = show only the donation board.
    var showDonationsOnly by remember { mutableStateOf(false) }

    // Clears the form and goes back to create mode.
    fun clearForm() {
        name = ""
        price = ""
        unit = ""
        stock = ""
        localProducer = ""
        category = "Fruta"
        availableForDonation = false
        errorMessage = ""
        selectedId = null
    }

    // Returns an error message, or an empty text when the form is valid.
    fun validate(): String = when {
        !name.isNotBlank() -> "Informe o nome do produto."
        price.replace(",", ".").toDoubleOrNull() == null -> "Preço inválido (ex.: 4,50)"
        stock.toIntOrNull() == null -> "Estoque inválido. Use um número inteiro."
        else -> ""
    }

    // Builds a Product from the form fields with the given id.
    fun buildProduct(id: Int) = Product(
        id = id,
        name = name,
        category = category,
        price = price.replace(",", ".").toDoubleOrNull() ?: 0.0,
        unit = unit,
        stock = stock.toIntOrNull() ?: 0,
        localProducer = localProducer,
        availableForDonation = availableForDonation
    )

    // Summary panel numbers (always over all products).
    val localCount = products.filter { it.localProducer.isNotBlank() }.size
    val donationCount = donations.size

    // List shown below: all products or only the donation board.
    val shownList = if (showDonationsOnly) donations else products

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        Text(text = "Hortifruti do Bairro", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.padding(4.dp))

        // ---------- Form ----------
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Preço") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unidade") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Estoque") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = localProducer,
            onValueChange = { localProducer = it },
            label = { Text("Produtor do bairro (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Each tap moves to the next category: Fruta > Verdura > Legume > Fruta.
            Button(
                onClick = {
                    category = when (category) {
                        "Fruta" -> "Verdura"
                        "Verdura" -> "Legume"
                        else -> "Fruta"
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Categoria: $category")
            }
            Spacer(modifier = Modifier.padding(4.dp))
            // Each tap flips the Boolean.
            Button(
                onClick = { availableForDonation = !availableForDonation },
                modifier = Modifier.weight(1f)
            ) {
                Text("Disponível para doação: " + if (availableForDonation) "Sim" else "Não")
            }
        }

        // Error message only shows up when validation fails.
        if (errorMessage.isNotBlank()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        // Create mode shows "Salvar"; edit mode shows "Atualizar", "Excluir" and "Cancelar".
        if (selectedId == null) {
            Button(
                onClick = {
                    errorMessage = validate()
                    if (errorMessage == "") {
                        // id = 0 lets Room generate a new id.
                        viewModel.addProduct(buildProduct(id = 0))
                        clearForm()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        errorMessage = validate()
                        if (errorMessage == "") {
                            // Same id, so Room updates the existing row.
                            selectedId?.let { viewModel.updateProduct(buildProduct(id = it)) }
                            clearForm()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Atualizar")
                }
                Spacer(modifier = Modifier.padding(2.dp))
                Button(
                    onClick = {
                        // Room deletes by primary key.
                        selectedId?.let { viewModel.deleteProduct(buildProduct(id = it)) }
                        clearForm()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Excluir")
                }
                Spacer(modifier = Modifier.padding(2.dp))
                Button(
                    onClick = { clearForm() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }
        Spacer(modifier = Modifier.padding(4.dp))

        // ---------- Summary panel ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(8.dp)
        ) {
            Text(text = "Produtos: ${products.size}", modifier = Modifier.weight(1f))
            Text(text = "Do bairro: $localCount", modifier = Modifier.weight(1f))
            Text(text = "Doação: $donationCount", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.padding(4.dp))

        // Toggles between the full list and the donation board.
        Button(
            onClick = { showDonationsOnly = !showDonationsOnly },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (showDonationsOnly) "Ver todos os produtos" else "Mural de doações")
        }

        Text(
            text = if (showDonationsOnly) "Mural de doações" else "Todos os produtos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (shownList.size == 0) {
            Text("Nenhum produto para mostrar.")
        }

        // ---------- Product list ----------
        // LazyColumn only draws the items visible on screen.
        // weight(1f) makes the list take the remaining space.
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(shownList) { product ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .clickable {
                            // Tapping an item loads it into the form (edit mode).
                            selectedId = product.id
                            name = product.name
                            price = product.price.toString()
                            unit = product.unit
                            stock = product.stock.toString()
                            localProducer = product.localProducer
                            category = product.category
                            availableForDonation = product.availableForDonation
                            errorMessage = ""
                        }
                        .padding(8.dp)
                ) {
                    Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                    Text(text = product.category)
                    Text(text = "R$ ${String.format("%.2f", product.price)} / ${product.unit}")
                    Text(text = "Estoque: ${product.stock}")
                    if (product.localProducer.isNotBlank()) {
                        Text(text = "Produtor do bairro: ${product.localProducer}")
                    }
                    if (product.availableForDonation) {
                        Text(text = "Disponível para doação")
                    }
                }
            }
        }
    }
}
