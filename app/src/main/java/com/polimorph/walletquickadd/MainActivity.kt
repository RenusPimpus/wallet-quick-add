package com.polimorph.walletquickadd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.acceptIntent(intent)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { QuickAddScreen(viewModel) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.acceptIntent(intent)
    }
}

@Composable
private fun QuickAddScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Wallet Quick Add", style = MaterialTheme.typography.headlineMedium)
        Text("Udostępnij zaznaczoną lub skopiowaną kwotę do tej aplikacji, wybierz kategorię i zatwierdź.")
        OutlinedTextField(
            value = state.tokenDraft,
            onValueChange = viewModel::setToken,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Token API Wallet") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = viewModel::saveTokenAndLoad, enabled = !state.loading) { Text("Zapisz i połącz") }
            OutlinedButton(onClick = viewModel::loadMetadata, enabled = !state.loading) { Text("Odśwież") }
        }
        SelectionMenu(
            "Konto",
            state.accounts.firstOrNull { it.id == state.selectedAccountId }?.name,
            state.accounts.map { it.id to it.name },
            viewModel::selectAccount
        )
        SelectionMenu(
            "Kategoria",
            state.categories.firstOrNull { it.id == state.selectedCategoryId }?.name,
            state.categories.map { it.id to it.name },
            viewModel::selectCategory
        )
        OutlinedTextField(
            value = state.amount,
            onValueChange = viewModel::setAmount,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Kwota wydatku") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        OutlinedTextField(
            value = state.note,
            onValueChange = viewModel::setNote,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notatka (opcjonalnie)") },
            minLines = 2
        )
        Button(onClick = viewModel::addExpense, modifier = Modifier.fillMaxWidth(), enabled = !state.loading) {
            Text("Dodaj wydatek")
        }
        if (state.loading) CircularProgressIndicator()
        state.message?.let {
            Text(it, color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SelectionMenu(
    label: String,
    selected: String?,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), enabled = options.isNotEmpty()) {
            Text("$label: ${selected ?: "brak danych"}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = {
                    onSelected(id)
                    expanded = false
                })
            }
        }
    }
}
