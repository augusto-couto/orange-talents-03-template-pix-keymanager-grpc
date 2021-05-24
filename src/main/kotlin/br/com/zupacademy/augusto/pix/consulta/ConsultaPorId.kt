package br.com.zupacademy.augusto.pix.consulta

import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank

@Introspected
data class ConsultaPorId(
    @field:NotBlank
    val idPix: Long,
    @field:NotBlank
    val idCliente: UUID
)
