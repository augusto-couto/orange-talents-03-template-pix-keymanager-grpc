package br.com.zupacademy.augusto.pix.consulta

import br.com.zupacademy.augusto.client.BankAccount
import br.com.zupacademy.augusto.client.Owner
import java.time.LocalDateTime

data class ConsultaPixDetails(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {


}