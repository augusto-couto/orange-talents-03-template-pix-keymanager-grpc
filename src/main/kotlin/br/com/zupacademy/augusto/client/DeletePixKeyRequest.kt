package br.com.zupacademy.augusto.client

import br.com.zupacademy.augusto.pix.Pix

data class DeletePixKeyRequest(
    val key: String,
    val participant: String = Participant.ITAU_UNIBANCO_SA.ispb
)