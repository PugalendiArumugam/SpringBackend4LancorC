package com.nexus.lancorC.back.appartmentez

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AppartmentEzyApplication

fun main(args: Array<String>) {
	val context = runApplication<AppartmentEzyApplication>(*args)
	println("DEBUG: Mail Host is -> " + context.environment.getProperty("spring.mail.host"))
}
