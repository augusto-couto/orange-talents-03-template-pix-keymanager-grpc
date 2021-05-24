package br.com.zupacademy.augusto.pix.consulta

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
data class ConsultaPorChave(
    @field:NotBlank
    @field:Size(max = 77)
    val chave: String
)
