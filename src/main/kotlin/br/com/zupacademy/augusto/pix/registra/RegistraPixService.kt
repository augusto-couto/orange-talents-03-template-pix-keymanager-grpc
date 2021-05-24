package br.com.zupacademy.augusto.pix.registra

import br.com.zupacademy.augusto.client.ClienteClient
import br.com.zupacademy.augusto.client.CreatePixKeyRequest
import br.com.zupacademy.augusto.client.SistemaPixBCBClient
import br.com.zupacademy.augusto.pix.Pix
import br.com.zupacademy.augusto.pix.PixAlreadyExistsException
import br.com.zupacademy.augusto.pix.PixRepository
import br.com.zupacademy.augusto.pix.TipoChave
import io.micronaut.http.HttpStatus
import io.micronaut.transaction.SynchronousTransactionManager
import io.micronaut.validation.Validated
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class RegistraPixService(
    @Inject val clienteClient: ClienteClient,
    @Inject val pixRepository: PixRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>,
    @Inject val bcbClient: SistemaPixBCBClient
) {

    fun registra(@Valid requestRegistra: RegistraPixRequest): Pix {
        val clienteResponse = clienteClient.consulta(
            requestRegistra.idCliente.toString(),
            requestRegistra.tipoConta!!.name
        )?.body() ?: throw IllegalStateException("Cliente não encontrado.")

        val chavePix = requestRegistra.toModel(clienteResponse)

        if (pixRepository.existsByChave(requestRegistra.valorChave)) {
            throw PixAlreadyExistsException("Chave pix já cadastrada.")
        }

        if (requestRegistra.valorChave.length > 77) {
            throw IllegalArgumentException("Chave não deve ter tamanho superior a 77.")
        }

        when (requestRegistra.tipoChave) {
            TipoChave.CPF -> if (!requestRegistra.valorChave.matches(Regex("^[0-9]{11}\$"))) {
                throw IllegalArgumentException("Formato de cpf inválido.\n Formato esperado 99999999999")
            }
            TipoChave.PHONE -> if (!requestRegistra.valorChave.matches(Regex("^\\+[1-9][0-9]\\d{1,14}\$"))) {
                throw IllegalArgumentException("Formato de numero inválido.\n Formato esperado +5585988714077")
            }
            TipoChave.EMAIL -> if (!requestRegistra.valorChave.matches(Regex("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"))) {
                throw IllegalArgumentException("Formato de email inválido.")
            }
            TipoChave.RANDOM -> if (requestRegistra.valorChave.isNotEmpty()) {
                throw IllegalArgumentException("Chave deve estar vazio para gerar automaticamente")
            }
            else -> if (requestRegistra.valorChave.isBlank()) {
                throw IllegalArgumentException("Formato de pix inválido")
            }
        }

        val bcbResponse = bcbClient.cadastra(clienteResponse.toCreatePyxKeyRequest(requestRegistra))
        if (bcbResponse.status != HttpStatus.CREATED)
            throw java.lang.IllegalStateException("Erro ao registrar chave pix no BCB (Banco Central do Brasil).")

        chavePix.atualiza(bcbResponse.body()!!.key)

        transactionManager.executeWrite {
            pixRepository.save(chavePix)
        }
        return chavePix
    }
}