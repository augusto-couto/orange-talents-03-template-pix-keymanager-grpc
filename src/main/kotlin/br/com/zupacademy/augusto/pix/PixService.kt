package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.client.ClienteClient
import io.micronaut.transaction.SynchronousTransactionManager
import io.micronaut.validation.Validated
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class PixService(
    @Inject val clienteClient: ClienteClient,
    @Inject val pixRepository: PixRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>
) {

    fun registra(@Valid request: PixRequest): Pix {
        val clienteResponse = clienteClient.consulta(
            request.idCliente.toString(),
            request.tipoConta!!.name
        )?.body() ?: throw IllegalStateException("Cliente não encontrado.")

        val chavePix = request.toModel(clienteResponse)

        if (pixRepository.existsByChave(request.valorChave)) {
            throw PixAlreadyExistsException("Chave pix já cadastrada.")
        }

        if (request.valorChave.length > 77) {
            throw IllegalArgumentException("Chave não deve ter tamanho superior a 77.")
        }

        when (request.tipoChave) {
            TipoChave.CPF -> if (!request.valorChave.matches(Regex("^[0-9]{11}\$"))) {
                throw IllegalArgumentException("Formato de cpf inválido.\n Formato esperado 99999999999")
            }
            TipoChave.PHONE -> if (!request.valorChave.matches(Regex("^\\+[1-9][0-9]\\d{1,14}\$"))) {
                throw IllegalArgumentException("Formato de numero inválido.\n Formato esperado +5585988714077")
            }
            TipoChave.EMAIL -> if (!request.valorChave.matches(Regex("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"))) {
                throw IllegalArgumentException("Formato de email inválido.")
            }
            TipoChave.RANDOM -> if (request.valorChave.isNotEmpty()) {
                throw IllegalArgumentException("Chave deve estar vazio para gerar automaticamente")
            }
            else -> if (request.valorChave.isBlank()) {
                throw IllegalArgumentException("Formato de pix inválido")
            }
        }

        transactionManager.executeWrite {
            pixRepository.save(chavePix)
        }
        return chavePix
    }
}