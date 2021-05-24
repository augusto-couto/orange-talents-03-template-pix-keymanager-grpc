package br.com.zupacademy.augusto.pix.consulta

import br.com.zupacademy.augusto.*
import br.com.zupacademy.augusto.extension.porChave
import br.com.zupacademy.augusto.extension.porId
import br.com.zupacademy.augusto.client.AccountType
import br.com.zupacademy.augusto.shared.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ConsultaPixEndpoint(
    @Inject val consultaPixService: ConsultaPixService
) : KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceImplBase() {

    override fun consulta(request: ConsultaPixRequest, responseObserver: StreamObserver<ConsultaPixResponse>) {

        val response =

            when (request.tipoCase.number) {
                ConsultaPixRequest.CHAVE_FIELD_NUMBER ->
                    consultaPixService.consultaBCB(porChave(request.chave))

                ConsultaPixRequest.TIPOBUSCA_FIELD_NUMBER ->
                    consultaPixService.consultaPorId(porId(request.tipoBusca.pixId, request.tipoBusca.clienteId))

                else -> throw IllegalArgumentException("Chave pix inválida ou não informada.")
            }

        responseObserver.onNext(
            ConsultaPixResponse.newBuilder()
                .setClienteId(request.tipoBusca.clienteId)
                .setPixId(request.tipoBusca.pixId.toString())
                .setChavePix(ChavePix.newBuilder()
                    .setTipoChave(TipoChave.valueOf(response.keyType))
                    .setChave(response.key)
                    .setConta(Conta.newBuilder()
                        .setTipoConta(TipoConta.valueOf(AccountType.valueOf(response.bankAccount.accountType).toTipoConta().name))
                        .setInstituicao(response.bankAccount.participant)
                        .setNomeTitular(response.owner.name)
                        .setCpfTitular(response.owner.taxIdNumber)
                        .setAgencia(response.bankAccount.branch)
                        .setNumeroConta(response.bankAccount.accountNumber)
                    )
                    .setCriadaEm(response.createdAt.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano).build()
                    })
                    .build())
                .build())
        responseObserver.onCompleted()
    }
}