package br.com.zupacademy.augusto.pix

import java.util.*
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
    @Column(unique = true, nullable = false)
    val chave: String,
    @field:NotBlank
    @Column(nullable = false)
    val clienteId: String,
    @field:NotNull
    @Column(nullable = false)
    val tipoChave: TipoChave,
    @field:NotNull
    @Column(nullable = false)
    val tipoConta: TipoConta
) {
    @Id
    @GeneratedValue
    val id: Long? = null
}