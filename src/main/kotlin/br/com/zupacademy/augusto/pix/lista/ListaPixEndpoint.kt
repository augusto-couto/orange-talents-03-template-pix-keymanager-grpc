package br.com.zupacademy.augusto.pix.lista

import br.com.zupacademy.augusto.KeyManagerListaChavesGrpcServiceGrpc
import br.com.zupacademy.augusto.ListaPixRequest
import br.com.zupacademy.augusto.ListaPixResponse
import br.com.zupacademy.augusto.shared.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ListaPixEndpoint(
    @Inject val listaPixService: ListaPixService
) : KeyManagerListaChavesGrpcServiceGrpc.KeyManagerListaChavesGrpcServiceImplBase() {

    override fun lista(request: ListaPixRequest, responseObserver: StreamObserver<ListaPixResponse>) {

        responseObserver.onNext(listaPixService.listaChaves(request))
        responseObserver.onCompleted()
    }
}