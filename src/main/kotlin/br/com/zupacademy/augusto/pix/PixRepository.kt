package br.com.zupacademy.augusto.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository: JpaRepository<Pix, Long> {

    fun existsByChave(chave: String): Boolean

    fun findAllByClienteId(clienteId: String): List<Pix>

    fun findByClienteId(clienteId: String): Optional<Pix>
}