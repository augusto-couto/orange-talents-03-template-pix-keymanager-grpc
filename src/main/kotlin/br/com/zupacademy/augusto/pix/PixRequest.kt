package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.cliente.ClienteAccountResponse
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
class PixRequest(
    @field:Size(max = 77)
    val valorChave: String,
    @field:NotBlank
    val idCliente: UUID,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:NotNull
    val tipoConta: TipoConta?) {

    fun toModel(cliente: ClienteAccountResponse): Pix {

        val valorChave: String = if (tipoChave == TipoChave.RANDOM) {
            UUID.randomUUID().toString()
        } else {
            this.valorChave
        }

        return Pix(
            valorChave,
            cliente.titular.id.toString(),
            TipoChave.valueOf(tipoChave!!.name),
            cliente.tipo)
    }
}
