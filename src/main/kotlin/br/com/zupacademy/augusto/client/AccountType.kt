package br.com.zupacademy.augusto.client

import br.com.zupacademy.augusto.pix.TipoConta

enum class AccountType {
    ACCOUNT_TYPE_UNSPECIFIED, CACC, SVGS;

    fun toTipoConta(): TipoConta {
        return when (this) {
            CACC -> return TipoConta.CONTA_CORRENTE
            SVGS -> return TipoConta.CONTA_POUPANCA
            else -> TipoConta.TIPO_CONTA_UNSPECIFIED
        }
    }
}
