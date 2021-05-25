package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.ConsultaPixRequest
import br.com.zupacademy.augusto.KeyManagerConsultaGrpcServiceGrpc
import br.com.zupacademy.augusto.client.*
import br.com.zupacademy.augusto.cliente.ClienteAccountResponse
import br.com.zupacademy.augusto.cliente.ClienteTitularResponse
import br.com.zupacademy.augusto.instituicao.InstituicaoContaResponse
import br.com.zupacademy.augusto.pix.registra.RegistraPixRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaPixEndpointTest(
    val pixRepository: PixRepository,
    val pixManager: KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub
) {

    lateinit var EXISTINTG_PIX: Pix
    val randomId = UUID.randomUUID().toString()

    @Inject
    lateinit var bcbClient: SistemaPixBCBClient

    @Inject
    lateinit var clienteClient: ClienteClient

    @BeforeEach
    internal fun setup() {
        EXISTINTG_PIX = pixRepository.save(
            Pix(
                "00000000000",
                UUID.randomUUID().toString(),
                TipoChave.CPF,
                TipoConta.CONTA_CORRENTE
            )
        )

        bcbClient.cadastra(clienteAccountResponse().toCreatePyxKeyRequest(registraPixRequest()))
    }

    @AfterEach
    internal fun setoff() {
        pixRepository.deleteAll()
    }

    @MockBean(SistemaPixBCBClient::class)
    fun bcbClient(): SistemaPixBCBClient? {
        return Mockito.mock(SistemaPixBCBClient::class.java)
    }

    @MockBean(ClienteClient::class)
    fun clienteClient(): ClienteClient? {
        return Mockito.mock(ClienteClient::class.java)
    }

    @Test
    fun `deve consulta uma chave pelo id da chave e cliente`() {
        `when`(
            clienteClient.consulta(
                EXISTINTG_PIX.clienteId,
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val chave = pixRepository.findById(EXISTINTG_PIX.id).get()

        val response = pixManager.consulta(
            ConsultaPixRequest.newBuilder().setTipoBusca(
                ConsultaPixRequest.TipoBuscaPixId.newBuilder()
                    .setPixId(chave.id!!)
                    .setClienteId(chave.clienteId)
                    .build()
            )
                .build()
        )

        with(response) {
            assertEquals(chave.chave, this.chavePix.chave)
            assertEquals(chave.id.toString(), this.pixId)
            assertEquals(chave.clienteId, this.clienteId)
            assertEquals(chave.tipoChave.name, this.chavePix.tipoChave.name)
        }
    }

    @Test
    fun `nap deve consulta uma chave quando cliente nao encontrado pela consulta`() {
        `when`(
            clienteClient.consulta(
                randomId,
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.consulta(
                ConsultaPixRequest.newBuilder().setTipoBusca(
                    ConsultaPixRequest.TipoBuscaPixId.newBuilder()
                        .setPixId(EXISTINTG_PIX.id!!)
                        .setClienteId(randomId)
                        .build()
                )
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado.", status.description)
        }
    }

    @Test
    fun `nap deve consulta uma chave quando cliente nao for dono da mesma`() {
        `when`(
            clienteClient.consulta(
                EXISTINTG_PIX.clienteId,
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.consulta(
                ConsultaPixRequest.newBuilder().setTipoBusca(
                    ConsultaPixRequest.TipoBuscaPixId.newBuilder()
                        .setPixId(EXISTINTG_PIX.id!!)
                        .setClienteId(randomId)
                        .build()
                )
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Modo de busca permitida somente pelo dono da chave.", status.description)
        }
    }

    @Test
    fun `nao deve consultar uma chave pelo id da chave e cliente quando vazia`() {
        `when`(
            clienteClient.consulta(
                EXISTINTG_PIX.clienteId,
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val chave = pixRepository.findById(EXISTINTG_PIX.id).get()

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.consulta(
                ConsultaPixRequest.newBuilder().setTipoBusca(
                    ConsultaPixRequest.TipoBuscaPixId.newBuilder()
                        .setPixId(0)
                        .setClienteId("")
                        .build()
                )
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid UUID string: ", status.description)
        }
    }

    @Test
    fun `nao deve consultar uma chave inexistente pelo id`() {
        val exception = assertThrows<StatusRuntimeException> {
            pixManager.consulta(
                ConsultaPixRequest.newBuilder().setTipoBusca(
                    ConsultaPixRequest.TipoBuscaPixId.newBuilder()
                        .setPixId(Long.MAX_VALUE)
                        .setClienteId(randomId)
                        .build()
                )
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Resurso id do pix não encontrado.", status.description)
        }
    }

    @Test
    fun `deve consulta uma chave pelo valor da chave`() {
        `when`(bcbClient.consulta(EXISTINTG_PIX.chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val response = pixManager.consulta(
            ConsultaPixRequest.newBuilder()
                .setChave(EXISTINTG_PIX.chave)
                .build()
        )

        with(response) {
            assertEquals(pixKeyDetailsResponse().key, this.chavePix.chave)
            assertEquals("0", this.pixId)
            assertEquals("", this.clienteId)
            assertEquals(pixKeyDetailsResponse().keyType.name, this.chavePix.tipoChave.name)
        }
    }

    @Test
    fun `nap deve consulta uma chave pelo valor da chave quando vazia`() {
        `when`(bcbClient.consulta(EXISTINTG_PIX.chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.consulta(
                ConsultaPixRequest.newBuilder()
                    .setChave("")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("request with invalid parameters", status.description)
        }
    }

    @Test
    fun `nap deve consulta uma chave quando nao encontrar chave pix no bcb`() {
        `when`(bcbClient.consulta(randomId))
            .thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.consulta(
                ConsultaPixRequest.newBuilder()
                    .setChave(randomId)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave pix não encontrado.", status.description)
        }
    }

    @Test
    fun `nap deve consulta uma chave pelo tipo inexistente`() {
        `when`(bcbClient.consulta(EXISTINTG_PIX.chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.consulta(
                ConsultaPixRequest.newBuilder().build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave pix inválida ou não informada.", status.description)
        }
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub {
            return KeyManagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun clienteAccountResponse(): ClienteAccountResponse {
        return ClienteAccountResponse(
            TipoConta.CONTA_CORRENTE,
            InstituicaoContaResponse(
                "ITAÚ UNIBANCO S.A.",
                "60701190"
            ),
            "0001",
            "291900",
            ClienteTitularResponse(
                UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
                "Rafael M C Ponte",
                "02467781054"
            )
        )
    }

    private fun registraPixRequest(): RegistraPixRequest {
        return RegistraPixRequest(
            "40764442058",
            UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            TipoChave.CPF,
            TipoConta.CONTA_CORRENTE
        )
    }

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = TipoChave.CPF,
            key = "00000000000",
            bankAccount = BankAccount(
                participant = Participant.ITAU_UNIBANCO_SA.ispb,
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.CACC.name
            ),
            Owner(
                type = "NATURAL_PERSON",
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            ),
            createdAt = LocalDateTime.now()
        )
    }

}