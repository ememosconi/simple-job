package com.redbee.billingjob.controller

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BillingController(private val myJobLauncher: JobLauncher, val billingJob:Job) {

    @PostMapping("/billing")
    fun run() {
        val jobParameters = JobParametersBuilder()
            .addString("jobId", System.currentTimeMillis().toString())
            .toJobParameters()
        myJobLauncher.run(billingJob, jobParameters)
    }
}