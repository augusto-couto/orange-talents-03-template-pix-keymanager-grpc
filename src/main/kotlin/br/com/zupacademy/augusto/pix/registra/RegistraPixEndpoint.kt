package br.com.zupacademy.augusto.pix.registra

import br.com.zupacademy.augusto.KeymanagerCadastraGrpcServiceGrpc
import br.com.zupacademy.augusto.NewPixRequest
import br.com.zupacademy.augusto.NewPixResponse
import br.com.zupacademy.augusto.extension.toValidatorRequest
import br.com.zupacademy.augusto.shared.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraPixEndpoint(
    @Inject val registraPixService: RegistraPixService
) : KeymanagerCadastraGrpcServiceGrpc.KeymanagerCadastraGrpcServiceImplBase() {

    override fun cadastra(request: NewPixRequest, responseObserver: StreamObserver<NewPixResponse>) {

        val chavePix = registraPixService.registra(request.toValidatorRequest())

        val response = NewPixResponse.newBuilder()
            .setIdPix(chavePix.id!!)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}