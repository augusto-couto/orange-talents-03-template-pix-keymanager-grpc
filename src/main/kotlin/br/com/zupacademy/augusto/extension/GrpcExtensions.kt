package br.com.zupacademy.augusto.extension

import br.com.zupacademy.augusto.DeletePixRequest
import br.com.zupacademy.augusto.NewPixRequest
import br.com.zupacademy.augusto.pix.registra.RegistraPixRequest
import br.com.zupacademy.augusto.pix.TipoChave
import br.com.zupacademy.augusto.pix.TipoConta
import br.com.zupacademy.augusto.pix.deleta.DeletaPixRequest
import java.util.*

fun NewPixRequest.toValidatorRequest(): RegistraPixRequest {
    return RegistraPixRequest(
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

fun DeletePixRequest.toDeleteRequest(): DeletaPixRequest {
    return DeletaPixRequest(
        idPix,
        UUID.fromString(idCliente)
    )
}