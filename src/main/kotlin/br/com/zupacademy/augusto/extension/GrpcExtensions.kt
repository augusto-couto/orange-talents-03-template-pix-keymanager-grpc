package br.com.zupacademy.augusto.extension

import br.com.zupacademy.augusto.NewPixRequest
import br.com.zupacademy.augusto.pix.PixRequest
import br.com.zupacademy.augusto.pix.TipoChave
import br.com.zupacademy.augusto.pix.TipoConta
import java.util.*

fun NewPixRequest.toValidatorRequest(): PixRequest {
    return PixRequest(
        valorChave,
        UUID.fromString(idCliente),
        when (tipoChave) {
            br.com.zupacademy.augusto.TipoChave.TIPO_CHAVE_UNSPECIFIED -> null
            else -> TipoChave.valueOf(tipoChave.name)
        },
        when (tipoConta) {
            br.com.zupacademy.augusto.TipoConta.TIPO_CONTA_UNSPECIFIED -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }
    )
}