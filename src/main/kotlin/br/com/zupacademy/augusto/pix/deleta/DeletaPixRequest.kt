package br.com.zupacademy.augusto.pix.deleta

import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank

@Introspected
class DeletaPixRequest(
    @field:NotBlank
    val idPix: Long,
    @field:NotBlank
    val idCliente: UUID
)