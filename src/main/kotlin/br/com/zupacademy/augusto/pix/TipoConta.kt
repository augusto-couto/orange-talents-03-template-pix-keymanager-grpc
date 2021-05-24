package br.com.zupacademy.augusto.pix

import br.com.zupacademy.augusto.client.AccountType

enum class TipoConta{
    TIPO_CONTA_UNSPECIFIED, CONTA_CORRENTE, CONTA_POUPANCA;

    fun toAccountType(): AccountType {
        return when (this) {
            CONTA_CORRENTE -> return AccountType.CACC
            CONTA_POUPANCA -> return AccountType.SVGS
            else -> AccountType.ACCOUNT_TYPE_UNSPECIFIED
        }
    }
}