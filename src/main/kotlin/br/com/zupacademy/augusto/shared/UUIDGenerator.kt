package br.com.zupacademy.augusto.shared

import java.util.*
import javax.inject.Singleton

@Singleton
open class UUIDGenerator {

    open fun generate(): String {
        return UUID.randomUUID().toString()
    }
}