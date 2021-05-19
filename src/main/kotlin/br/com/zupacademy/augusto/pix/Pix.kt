package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.TipoChave
import br.com.zupacademy.augusto.TipoConta
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class Pix(
    @field:NotBlank
    @field:Size(max = 77)
    @Column(unique = true)
    val chave: String,
    @field:NotNull
    val clienteId: String,
    @field:NotBlank
    val tipoChave: TipoChave,
    @field:NotBlank
    val tipoConta: TipoConta
) {
    @Id
    @GeneratedValue
    val id: Long? = null
}