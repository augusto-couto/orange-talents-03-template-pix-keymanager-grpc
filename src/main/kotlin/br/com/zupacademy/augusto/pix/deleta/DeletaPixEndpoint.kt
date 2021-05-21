package br.com.zupacademy.augusto.pix.deleta

import br.com.zupacademy.augusto.DeletePixRequest
import br.com.zupacademy.augusto.DeletePixResponse
import br.com.zupacademy.augusto.KeymanagerDeletaGrpcServiceGrpc
import br.com.zupacademy.augusto.extension.toDeleteRequest
import br.com.zupacademy.augusto.shared.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class DeletaPixEndpoint(
    @Inject val deletaPixService: DeletaPixService
) : KeymanagerDeletaGrpcServiceGrpc.KeymanagerDeletaGrpcServiceImplBase() {

    override fun deleta(request: DeletePixRequest, responseObserver: StreamObserver<DeletePixResponse>) {
        deletaPixService.deleta(request.toDeleteRequest())

        val response = DeletePixResponse
            .newBuilder()
            .setIdPix(request.idPix)
            .setIdCliente(request.idCliente)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}