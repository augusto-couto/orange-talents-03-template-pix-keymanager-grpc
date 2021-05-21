package br.com.zupacademy.augusto.pix.deleta

import br.com.zupacademy.augusto.pix.NotOwnerPixException
import br.com.zupacademy.augusto.pix.PixRepository
import io.micronaut.transaction.SynchronousTransactionManager
import io.micronaut.validation.Validated
import java.io.FileNotFoundException
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class DeletaPixService(
    @Inject val pixRepository: PixRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>
) {

    fun deleta(@Valid deleteRequest: DeletaPixRequest) {
        val pix = pixRepository.findById(deleteRequest.idPix)

        if (!pix.isPresent) {
            throw FileNotFoundException("Resurso id do pix não encontrado.")
        }

        if (pix.get().clienteId != deleteRequest.idCliente.toString()) {
            throw NotOwnerPixException("Somende o dono do pix pode excluí-la.")
        }

        transactionManager.executeWrite {
            pixRepository.delete(pix.get())
        }
    }
}