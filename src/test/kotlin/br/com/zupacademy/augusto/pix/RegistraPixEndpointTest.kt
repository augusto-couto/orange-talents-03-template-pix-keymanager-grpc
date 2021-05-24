package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.KeymanagerCadastraGrpcServiceGrpc
import br.com.zupacademy.augusto.NewPixRequest
import br.com.zupacademy.augusto.TipoChave
import br.com.zupacademy.augusto.TipoConta
import br.com.zupacademy.augusto.client.*
import br.com.zupacademy.augusto.cliente.ClienteAccountResponse
import br.com.zupacademy.augusto.cliente.ClienteTitularResponse
import br.com.zupacademy.augusto.instituicao.InstituicaoContaResponse
import br.com.zupacademy.augusto.pix.TipoConta.CONTA_CORRENTE
import br.com.zupacademy.augusto.pix.registra.RegistraPixRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistraPixEndpointTest(
    val pixRepository: PixRepository,
    val pixManager: KeymanagerCadastraGrpcServiceGrpc.KeymanagerCadastraGrpcServiceBlockingStub
) {

    @Inject
    lateinit var clienteClient: ClienteClient

    @Inject
    lateinit var bcbClient: SistemaPixBCBClient

    @BeforeEach
    internal fun setup() {
        pixRepository.deleteAll()
    }

    @MockBean(ClienteClient::class)
    fun clienteClient(): ClienteClient? {
        return Mockito.mock(ClienteClient::class.java)
    }

    @MockBean(SistemaPixBCBClient::class)
    fun bcbClient(): SistemaPixBCBClient? {
        return Mockito.mock(SistemaPixBCBClient::class.java)
    }

    @Test
    fun `deve adicionar uma nova chave do tipo cpf`() {
        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        `when`(bcbClient.cadastra(clienteAccountResponse().toCreatePyxKeyRequest(registraPixRequest()))
        ).thenReturn(HttpResponse.created(createPixKeyResponse()))

        val response = pixManager.cadastra(
            NewPixRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.CPF)
                .setValorChave("40764442058")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        with(response) {
            assertNotNull(idPix)
            assertTrue(pixRepository.existsByChave("40764442058"))
        }
    }

    @Test
    fun `nao deve adicionar uma nova chave quando ja nao encontrar o cliente`() {
        `when`(
            clienteClient.consulta(
                "0d1bb194-3c52-4e67-8c35-a93c0af9284f",
                "CONTA_POUPANCA"
            )
        ).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("0d1bb194-3c52-4e67-8c35-a93c0af9284f")
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("40764442058")
                    .setTipoConta(TipoConta.CONTA_POUPANCA)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado.", status.description)
        }
    }

    @Test
    fun `nao deve adicionar uma nova chave quando ocorrer um erro no servico bcb`() {
        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        `when`(bcbClient.cadastra(clienteAccountResponse().toCreatePyxKeyRequest(registraPixRequest()))
        ).thenReturn(HttpResponse.badRequest())

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("40764442058")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }


        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar chave pix no BCB (Banco Central do Brasil).", status.description)
        }
    }

    @Test
    fun `nao deve adicionar uma nova chave quando ja existente`() {

        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        pixRepository.save(
            Pix(
                "40764442058",
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                br.com.zupacademy.augusto.pix.TipoChave.CPF,
                CONTA_CORRENTE
            )
        )

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("40764442058")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave pix já cadastrada.", status.description)
        }
    }

    @Test
    fun `nao deve adicionar uma nova chave quando formato cpf ivalido`() {
        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("adsfhf")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "Formato de cpf inválido.\n" +
                        " Formato esperado 99999999999", status.description
            )
        }
    }

    @Test
    fun `nao deve adicionar uma nova quando tipo chave nulo`() {
        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setValorChave("99999999999")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("request with invalid parameters", status.description)
        }
    }

    @Test
    fun `nao deve adicionar uma nova quando tipo conta nulo`() {
        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("99999999999")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("request with invalid parameters", status.description)
        }
    }

    @Test
    fun `nao deve adicionar uma nova chave quando formato phone ivalido`() {
        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.PHONE)
                    .setValorChave("adsfhf")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "Formato de numero inválido.\n" +
                        " Formato esperado +5585988714077", status.description
            )
        }
    }

    @Test
    fun `nao deve adicionar uma nova chave quando formato email ivalido`() {
        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.EMAIL)
                    .setValorChave("adsfhf")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Formato de email inválido.", status.description)
        }
    }

    @Test
    fun `nao deve adicionar uma nova chave quando formato random nao vazio`() {
        `when`(
            clienteClient.consulta(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "CONTA_CORRENTE"
            )
        ).thenReturn(HttpResponse.ok(clienteAccountResponse()))

        val exception = assertThrows<StatusRuntimeException> {
            pixManager.cadastra(
                NewPixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.RANDOM)
                    .setValorChave("deve estar vazio")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave deve estar vazio para gerar automaticamente", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerCadastraGrpcServiceGrpc.KeymanagerCadastraGrpcServiceBlockingStub {
            return KeymanagerCadastraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun clienteAccountResponse(): ClienteAccountResponse {
        return ClienteAccountResponse(
            CONTA_CORRENTE,
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

    private fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            TipoChave.CPF.name,
            "40764442058",
            BankAccount(Participant.ITAU_UNIBANCO_SA.ispb, "0001", "291900", "CACC"),
            Owner("NATURAL_PERSON", "Rafael M C Ponte", "02467781054"),
            LocalDateTime.now().toString()
        )
    }

    private fun registraPixRequest(): RegistraPixRequest {
        return RegistraPixRequest(
            "40764442058",
            UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            br.com.zupacademy.augusto.pix.TipoChave.CPF,
            CONTA_CORRENTE
        )
    }
}