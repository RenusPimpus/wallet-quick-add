package com.polimorph.walletquickadd

data class WalletAccount(val id: String, val name: String, val currencyCode: String = "")
data class WalletCategory(val id: String, val name: String)

data class QuickAddState(
    val tokenDraft: String = "",
    val tokenSaved: Boolean = false,
    val tokenSettingsVisible: Boolean = true,
    val amount: String = "",
    val note: String = "",
    val accounts: List<WalletAccount> = emptyList(),
    val categories: List<WalletCategory> = emptyList(),
    val selectedAccountId: String? = null,
    val selectedCategoryId: String? = null,
    val loading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)
