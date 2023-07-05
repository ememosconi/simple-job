package com.redbee.billingjob

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BillingJobApplication

fun main(args: Array<String>) {
	runApplication<BillingJobApplication>(*args)
}
