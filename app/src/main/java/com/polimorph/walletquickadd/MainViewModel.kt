package com.polimorph.walletquickadd

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = TokenStore(application)
    private val api = WalletApi()
    private val _state = MutableStateFlow(QuickAddState(tokenDraft = tokenStore.load()))
    val state: StateFlow<QuickAddState> = _state.asStateFlow()

    fun acceptIntent(intent: Intent?) {
        val sharedText = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            else -> null
        } ?: return
        AmountParser.parse(sharedText)?.let { parsed ->
            _state.update { it.copy(amount = parsed.abs().stripTrailingZeros().toPlainString()) }
        }
    }

    fun setToken(value: String) = _state.update { it.copy(tokenDraft = value) }
    fun setAmount(value: String) = _state.update { it.copy(amount = value) }
    fun setNote(value: String) = _state.update { it.copy(note = value) }
    fun selectAccount(id: String) = _state.update { it.copy(selectedAccountId = id) }
    fun selectCategory(id: String) = _state.update { it.copy(selectedCategoryId = id) }

    fun saveTokenAndLoad() {
        val token = state.value.tokenDraft.trim()
        if (token.isBlank()) {
            _state.update { it.copy(message = "Wklej token API.", isError = true) }
            return
        }
        tokenStore.save(token)
        loadMetadata()
    }

    fun loadMetadata() {
        val token = state.value.tokenDraft.trim()
        if (token.isBlank()) {
            _state.update { it.copy(message = "Najpierw zapisz token API.", isError = true) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = "Pobieram konta i kategorie…", isError = false) }
            runCatching {
                api.getAccounts(token) to api.getCategories(token)
            }.onSuccess { (accounts, categories) ->
                _state.update {
                    it.copy(
                        accounts = accounts,
                        categories = categories,
                        selectedAccountId = it.selectedAccountId ?: accounts.firstOrNull()?.id,
                        selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull()?.id,
                        loading = false,
                        message = "Połączono. Konta: ${accounts.size}, kategorie: ${categories.size}.",
                        isError = false
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(loading = false, message = error.message ?: "Nie udało się połączyć.", isError = true) }
            }
        }
    }

    fun addExpense() {
        val snapshot = state.value
        val amount = AmountParser.parse(snapshot.amount)
        val accountId = snapshot.selectedAccountId
        val categoryId = snapshot.selectedCategoryId
        when {
            amount == null || amount.signum() == 0 -> {
                _state.update { it.copy(message = "Podaj prawidłową kwotę.", isError = true) }
                return
            }
            accountId == null -> {
                _state.update { it.copy(message = "Wybierz konto.", isError = true) }
                return
            }
            categoryId == null -> {
                _state.update { it.copy(message = "Wybierz kategorię.", isError = true) }
                return
            }
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = "Dodaję wydatek…", isError = false) }
            runCatching {
                api.createExpense(snapshot.tokenDraft, accountId, categoryId, amount, snapshot.note)
            }.onSuccess {
                _state.update { it.copy(amount = "", note = "", loading = false, message = "Wydatek został dodany.", isError = false) }
            }.onFailure { error ->
                _state.update { it.copy(loading = false, message = error.message ?: "Nie udało się dodać wydatku.", isError = true) }
            }
        }
    }
}
