package br.com.zupacademy.augusto.pix.registra

import br.com.zupacademy.augusto.cliente.ClienteAccountResponse
import br.com.zupacademy.augusto.pix.Pix
import br.com.zupacademy.augusto.pix.TipoChave
import br.com.zupacademy.augusto.pix.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
class RegistraPixRequest(
    @field:Size(max = 77)
    val valorChave: String,
    @field:NotBlank
    val idCliente: UUID,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:NotNull
    val tipoConta: TipoConta?) {

    fun toModel(cliente: ClienteAccountResponse): Pix {

        return Pix(
            valorChave,
            cliente.titular.id.toString(),
            TipoChave.valueOf(tipoChave!!.name),
            cliente.tipo)
    }
}
