package br.com.zupacademy.augusto.pix.lista

import br.com.zupacademy.augusto.ListaPixRequest
import br.com.zupacademy.augusto.ListaPixResponse
import br.com.zupacademy.augusto.TipoChave
import br.com.zupacademy.augusto.TipoConta
import br.com.zupacademy.augusto.pix.PixRepository
import com.google.protobuf.Timestamp
import java.io.FileNotFoundException
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListaPixService(
    @Inject val pixRepository: PixRepository
) {

    fun listaChaves(request: ListaPixRequest): ListaPixResponse {
        if (request.clienteId.isNullOrBlank()) throw IllegalArgumentException("Id do cliente obrigatório.")

        val pix = pixRepository.findByClienteId(request.clienteId)

        if (pix.isEmpty) throw FileNotFoundException("Recurso cliente id não encontado.")

        val response = pixRepository.findAllByClienteId(
            request.clienteId
        ).map { it.toListOfChavesPixResponse() }

        return ListaPixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .addAllChaves(response)
            .build()
    }
}