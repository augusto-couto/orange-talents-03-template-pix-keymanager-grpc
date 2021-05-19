package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.KeymanagerGrpcServiceGrpc
import br.com.zupacademy.augusto.NewPixRequest
import br.com.zupacademy.augusto.NewPixResponse
import br.com.zupacademy.augusto.TipoChave
import br.com.zupacademy.augusto.TipoChave.*
import br.com.zupacademy.augusto.client.ClienteClient
import br.com.zupacademy.augusto.shared.ErrorHandler
import br.com.zupacademy.augusto.shared.UUIDGenerator
import io.grpc.stub.StreamObserver
import io.micronaut.transaction.SynchronousTransactionManager
import java.lang.IllegalStateException
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class PixManagerService(
    @Inject val clienteClient: ClienteClient,
    @Inject val pixRepository: PixRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>,
    @Inject val keyGenerator: UUIDGenerator
) : KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceImplBase() {

    override fun cadastra(request: NewPixRequest, responseObserver: StreamObserver<NewPixResponse>) {
        val cienteResponse = clienteClient.consulta(
            request.idCliente,
            request.tipoConta.name
        ).body() ?: throw IllegalStateException("Cliente não encontrado.")

        val valorChave: String = if (request.tipoChave.equals(RANDOM)) {
            keyGenerator.generate()
        } else {
            request.valorChave
        }

        val chavePix = Pix(
            valorChave,
            cienteResponse.titular.id,
            TipoChave.valueOf(request.tipoChave.name),
            cienteResponse.tipo
        )

        transactionManager.executeWrite {

            if (pixRepository.existsByChave(request.valorChave)) {
                throw PixAlreadyExistsException("Chave pix já cadastrada.")
            }

            if (request.valorChave.length > 77) {
                throw IllegalArgumentException("Chave não deve ter tamanho superior a 77.")
            }

            when (request.tipoChave) {
                CPF -> if (!request.valorChave.matches(Regex("^[0-9]{11}\$"))) {
                    throw IllegalArgumentException("Formato de cpf inválido.\n Formato esperado 99999999999")
                }
                PHONE -> if (!request.valorChave.matches(Regex("^\\+[1-9][0-9]\\d{1,14}\$"))) {
                    throw IllegalArgumentException("Formato de numero inválido.\n Formato esperado +5585988714077")
                }
                EMAIL -> if (!request.valorChave.matches(Regex("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"))) {
                    throw IllegalArgumentException("Formato de email inválido.")
                }
                RANDOM -> if (request.valorChave.isNotEmpty()) {
                    throw IllegalArgumentException("Chave deve estar vazio para gerar automaticamente")
                }
                else -> if (request.valorChave.isBlank()) {
                    throw IllegalArgumentException("Formato de pix inválido")
                }
            }

            pixRepository.save(chavePix)
        }

        val response = NewPixResponse.newBuilder()
            .setIdPix(chavePix.id!!)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

}