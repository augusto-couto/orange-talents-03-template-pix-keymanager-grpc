package br.com.zupacademy.augusto.pix.deleta

import br.com.zupacademy.augusto.client.DeletePixKeyRequest
import br.com.zupacademy.augusto.client.Participant
import br.com.zupacademy.augusto.client.SistemaPixBCBClient
import br.com.zupacademy.augusto.pix.NotOwnerPixException
import br.com.zupacademy.augusto.pix.PixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Part
import io.micronaut.transaction.SynchronousTransactionManager
import io.micronaut.validation.Validated
import java.io.FileNotFoundException
import java.lang.IllegalStateException
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class DeletaPixService(
    @Inject val pixRepository: PixRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>,
    @Inject val bcbClient: SistemaPixBCBClient

) {

    fun deleta(@Valid deleteRequest: DeletaPixRequest) {
        val pix = pixRepository.findById(deleteRequest.idPix)

        if (!pix.isPresent) {
            throw FileNotFoundException("Resurso id do pix não encontrado.")
        }

        if (pix.get().clienteId != deleteRequest.idCliente.toString()) {
            throw NotOwnerPixException("Somende o dono do pix pode excluí-la.")
        }

        val request = DeletePixKeyRequest(pix.get().chave)

        val bcbResponse = bcbClient.deleta(pix.get().chave, request)
        if (bcbResponse.status != HttpStatus.OK)
            throw IllegalStateException("Erro ao remover chave pix no BCB (Banco Central do Brasil).")

        transactionManager.executeWrite {
            pixRepository.delete(pix.get())
        }
    }
}