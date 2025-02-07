package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.ListaPixResponse
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
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
    var chave: String,
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

    val createdAt = LocalDateTime.now()

    constructor() : this("", "", TipoChave.TIPO_CHAVE_UNSPECIFIED, TipoConta.TIPO_CONTA_UNSPECIFIED)

    fun atualiza(chave: String) {
        this.chave = chave
    }

    fun toListOfChavesPixResponse(): ListaPixResponse.Chave {
        return ListaPixResponse.Chave.newBuilder()
            .setPixId(this.id.toString())
            .setTipoChave(br.com.zupacademy.augusto.TipoChave.valueOf(this.tipoChave.name))
            .setChave(this.chave)
            .setTipoConta(br.com.zupacademy.augusto.TipoConta.valueOf(this.tipoConta.name))
            .setCriadoEm(this.createdAt.let { timestamp ->
                val createdAt = timestamp.atZone(ZoneId.of("UTC")).toInstant()
                Timestamp.newBuilder()
                    .setSeconds(createdAt.epochSecond)
                    .setNanos(createdAt.nano)
                    .build()
            })
            .build()
    }
}