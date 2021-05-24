package br.com.zupacademy.augusto.pix.consulta

import br.com.zupacademy.augusto.client.ClienteClient
import br.com.zupacademy.augusto.client.SistemaPixBCBClient
import br.com.zupacademy.augusto.pix.NotOwnerPixException
import br.com.zupacademy.augusto.pix.PixRepository
import io.micronaut.validation.Validated
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class ConsultaPixService(
    @Inject val pixRepository: PixRepository,
    @Inject val bcbClient: SistemaPixBCBClient,
    @Inject val clienteClient: ClienteClient
) {

    fun consultaBCB(@Valid request: ConsultaPorChave): ConsultaPixDetails {

        val consultaResponse = bcbClient.consulta(request.chave).body()
            ?: throw IllegalStateException("Chave pix não encontrado.")

        return consultaResponse.toPixDetails()
    }

    fun consultaPorId(@Valid request: ConsultaPorId): ConsultaPixDetails {
        val pixRequest = pixRepository.findById(request.idPix)

        if (pixRequest.isEmpty) {
            throw FileNotFoundException("Resurso id do pix não encontrado.")
        }

        val clienteResponse = clienteClient.consulta(
            pixRequest.get().clienteId,
            pixRequest.get().tipoConta.name
        )?.body() ?: throw IllegalStateException("Cliente não encontrado.")

        if (pixRequest.get().clienteId != request.idCliente.toString()) {
            throw NotOwnerPixException("Modo de busca permitida somente pelo dono da chave.")
        }

        return clienteResponse.toPixDetails(pixRequest.get())
    }
}