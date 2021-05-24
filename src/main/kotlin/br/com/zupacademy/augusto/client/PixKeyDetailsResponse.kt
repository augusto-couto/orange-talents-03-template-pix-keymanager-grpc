package br.com.zupacademy.augusto.client

import br.com.zupacademy.augusto.pix.TipoChave
import br.com.zupacademy.augusto.pix.consulta.ConsultaPixDetails
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: TipoChave,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toPixDetails(): ConsultaPixDetails {
        return ConsultaPixDetails(
            this.keyType.name,
            this.key,
            bankAccount = BankAccount(
                participant = bankAccount.participant,
                branch = bankAccount.branch,
                accountNumber = bankAccount.accountNumber,
                accountType = bankAccount.accountType
            ),
            Owner(
                type = owner.type,
                name = owner.name,
                taxIdNumber = owner.taxIdNumber
            ),
            this.createdAt
        )
    }
}