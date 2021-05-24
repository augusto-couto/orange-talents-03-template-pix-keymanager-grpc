package br.com.zupacademy.augusto.client

data class CreatePixKeyResponse (
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: String
)
