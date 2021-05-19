package br.com.zupacademy.augusto.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixRepository: JpaRepository<Pix, Long> {

    fun existsByChave(chave: String): Boolean
}