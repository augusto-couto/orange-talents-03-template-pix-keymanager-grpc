package br.com.zupacademy.augusto.cliente

import br.com.zupacademy.augusto.instituicao.InstituicaoContaResponse
import br.com.zupacademy.augusto.pix.TipoConta

data class ClienteAccountResponse(
    val tipo: TipoConta,
    val instituicao: InstituicaoContaResponse,
    val agencia: String,
    val numero: String,
    val titular: ClienteTitularResponse
)