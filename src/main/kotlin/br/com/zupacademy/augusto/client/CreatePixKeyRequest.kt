package br.com.zupacademy.augusto.client

data class CreatePixKeyRequest (
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
)

