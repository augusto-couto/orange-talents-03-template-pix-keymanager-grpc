package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.DeletePixRequest
import br.com.zupacademy.augusto.KeymanagerDeletaGrpcServiceGrpc
import br.com.zupacademy.augusto.client.DeletePixKeyRequest
import br.com.zupacademy.augusto.client.DeletePixKeyResponse
import br.com.zupacademy.augusto.client.Participant
import br.com.zupacademy.augusto.client.SistemaPixBCBClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeletaPixEndpointTest(
    val pixRepository: PixRepository,
    val pixManager: KeymanagerDeletaGrpcServiceGrpc.KeymanagerDeletaGrpcServiceBlockingStub
) {

    lateinit var EXISTINTG_PIX: Pix

    @Inject
    lateinit var bcbClient: SistemaPixBCBClient

    @BeforeEach
    fun setup() {
        EXISTINTG_PIX = pixRepository.save(Pix(
            "00000000000",
            UUID.randomUUID().toString(),
            TipoChave.CPF,
            TipoConta.CONTA_CORRENTE
        ))
    }

    @AfterEach
    fun setDown() {
        pixRepository.deleteAll()
    }

    @MockBean(SistemaPixBCBClient::class)
    fun bcbClient(): SistemaPixBCBClient? {
        return Mockito.mock(SistemaPixBCBClient::class.java)
    }

    @Test
    fun `deve deletar uma chave pix existente`() {

        Mockito.`when`(
            bcbClient.deleta(EXISTINTG_PIX.chave, DeletePixKeyRequest(EXISTINTG_PIX.chave))
        ).thenReturn(HttpResponse.ok(
            DeletePixKeyResponse(
                EXISTINTG_PIX.chave,
                Participant.ITAU_UNIBANCO_SA.ispb,
                LocalDateTime.now()
            )
        ))

        val response = pixManager.deleta(DeletePixRequest.newBuilder()
            .setIdPix(EXISTINTG_PIX.id!!)
            .setIdCliente(EXISTINTG_PIX.clienteId)
            .build())

        assertEquals(EXISTINTG_PIX.id, response.idPix)
        assertEquals(EXISTINTG_PIX.clienteId, response.idCliente)
        assertTrue(pixRepository.findAll().isEmpty())
    }

    @Test
    fun `nao deve deletar uma chave pix inexistente`() {
        val exception = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            pixManager.deleta(DeletePixRequest.newBuilder()
                .setIdPix(Long.MAX_VALUE)
                .setIdCliente(EXISTINTG_PIX.clienteId)
                .build())
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Resurso id do pix não encontrado.", exception.status.description)
    }

    @Test
    fun `nao deve deletar uma chave pix quando houver erro no servico bcb`() {

        Mockito.`when`(
            bcbClient.deleta(EXISTINTG_PIX.chave, DeletePixKeyRequest(EXISTINTG_PIX.chave))
        ).thenReturn(HttpResponse.unprocessableEntity())

        val exception = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            pixManager.deleta(DeletePixRequest.newBuilder()
                .setIdPix(EXISTINTG_PIX.id!!)
                .setIdCliente(EXISTINTG_PIX.clienteId)
                .build())
        }

        assertEquals(Status.FAILED_PRECONDITION.code, exception.status.code)
        assertEquals("Erro ao remover chave pix no BCB (Banco Central do Brasil).", exception.status.description)
    }

    @Test
    fun `nao deve deletar uma chave pix quando nao for o dono da mesma`() {
        val exception = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            pixManager.deleta(DeletePixRequest.newBuilder()
                .setIdPix(EXISTINTG_PIX.id!!)
                .setIdCliente(UUID.randomUUID().toString())
                .build())
        }

        assertEquals(Status.PERMISSION_DENIED.code, exception.status.code)
        assertEquals("Somende o dono do pix pode excluí-la.", exception.status.description)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerDeletaGrpcServiceGrpc.KeymanagerDeletaGrpcServiceBlockingStub {
            return KeymanagerDeletaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}
