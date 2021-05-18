package br.com.zupacademy.augusto

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.augusto")
		.start()
}

