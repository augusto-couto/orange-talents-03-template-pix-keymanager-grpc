package br.com.zupacademy.augusto.cliente

import java.util.*

data class ClienteTitularResponse(
    val id: UUID,
    val nome: String,
    val cpf: String,
)
