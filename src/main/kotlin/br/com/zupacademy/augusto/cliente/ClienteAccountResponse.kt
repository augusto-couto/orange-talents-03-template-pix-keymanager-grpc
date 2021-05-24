package br.com.zupacademy.augusto.cliente

import br.com.zupacademy.augusto.client.BankAccount
import br.com.zupacademy.augusto.client.CreatePixKeyRequest
import br.com.zupacademy.augusto.client.Owner
import br.com.zupacademy.augusto.instituicao.InstituicaoContaResponse
import br.com.zupacademy.augusto.pix.Pix
import br.com.zupacademy.augusto.pix.TipoConta
import br.com.zupacademy.augusto.pix.registra.RegistraPixRequest

data class ClienteAccountResponse(
    val tipo: TipoConta,
    val instituicao: InstituicaoContaResponse,
    val agencia: String,
    val numero: String,
    val titular: ClienteTitularResponse
) {
    fun toCreatePyxKeyRequest(chave: RegistraPixRequest): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = chave.tipoChave!!.name,
            key = chave.valorChave,
            bankAccount = BankAccount(
                participant = instituicao.ispb,
                branch = this.agencia,
                accountNumber = this.numero,
                accountType = this.tipo.toAccountType().toString()
            ),
            Owner(
                type = "NATURAL_PERSON",
                name = titular.nome,
                taxIdNumber = titular.cpf
            )
        )
    }
}