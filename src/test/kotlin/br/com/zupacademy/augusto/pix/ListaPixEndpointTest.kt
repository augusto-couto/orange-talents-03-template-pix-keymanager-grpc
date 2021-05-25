package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.KeyManagerListaChavesGrpcServiceGrpc
import br.com.zupacademy.augusto.ListaPixRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

@MicronautTest(transactional = false)
internal class ListaPixEndpointTest(
    private val pixRepository: PixRepository,
    private val pixManager: KeyManagerListaChavesGrpcServiceGrpc.KeyManagerListaChavesGrpcServiceBlockingStub
)  {

    private val CLIENTE_ID = UUID.randomUUID().toString()

    @BeforeEach
    internal fun setup() {
        pixRepository.save(Pix("00000000000", CLIENTE_ID, TipoChave.CPF, TipoConta.CONTA_CORRENTE))
        pixRepository.save(Pix("+5585988714077", CLIENTE_ID, TipoChave.PHONE, TipoConta.CONTA_CORRENTE))
        pixRepository.save(Pix("rafael@email.com", CLIENTE_ID, TipoChave.EMAIL, TipoConta.CONTA_CORRENTE))
        pixRepository.save(Pix("d2f9b0fa-9c53-431c-931e-4f7673e3b087", CLIENTE_ID, TipoChave.RANDOM, TipoConta.CONTA_CORRENTE))
    }

    @AfterEach
    internal fun setOff() {
        pixRepository.deleteAll()
    }

    @Test
    fun `deve retornar uma lista de chaves que pertencem ao usuario`() {
        val response = pixManager.lista(
            ListaPixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .build()
        )

        with(response) {
            assertTrue(chavesList.isNotEmpty())
            assertEquals(pixRepository.findById(chavesList.first().pixId.toLong()).get().clienteId, clienteId)
        }
    }

    @Test
    fun `nao deve retornar uma lista quando id nulo ou vazio`() {
        val exception = assertThrows<StatusRuntimeException> {
            pixManager.lista(
                ListaPixRequest.newBuilder()
                    .setClienteId("")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Id do cliente obrigatório.", status.description)
        }
    }

    @Test
    fun `nao deve retornar uma lista quando id inexistente`() {
        val exception = assertThrows<StatusRuntimeException> {
            pixManager.lista(
                ListaPixRequest.newBuilder()
                    .setClienteId(UUID.randomUUID().toString())
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Recurso cliente id não encontado.", status.description)
        }
    }

    @Factory
    class Clients2 {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListaChavesGrpcServiceGrpc.KeyManagerListaChavesGrpcServiceBlockingStub {
            return KeyManagerListaChavesGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}