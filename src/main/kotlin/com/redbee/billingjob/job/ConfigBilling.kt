package com.redbee.billingjob.job

import com.redbee.billingjob.dto.BillingDTO
import com.redbee.jobcdl.commons.database.BillingRowMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.ItemPreparedStatementSetter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.sql.PreparedStatement
import javax.sql.DataSource

@Configuration
@EnableBatchProcessing
class ConfigBilling(
    val dataSource: DataSource,

    ) {

    @Bean
    fun billingJob(jobRepository: JobRepository?, billingStep: Step?): Job? {
        return JobBuilder("billingJob", jobRepository!!)
            .incrementer(RunIdIncrementer())
            .start(billingStep!!)
            .build()
    }

    @Bean
    fun billingStep(jobRepository: JobRepository?, transactionManager: PlatformTransactionManager?): TaskletStep? {
        return StepBuilder("billingStep", jobRepository!!).chunk<Any, Any>(100, transactionManager!!)
            .reader(billingReader()!!).processor { item: Any? -> item }
            .writer(billingWriter() as ItemWriter<in Any>)
            .build()
    }

    private fun billingReader(): JdbcCursorItemReader<BillingDTO>? {
        val reader: JdbcCursorItemReader<BillingDTO> = JdbcCursorItemReader<BillingDTO>()
        reader.setSql("select count(*) as count, idSite from TRANSACTIONS where STATE = 'PROCESADO' group by idSite")
        reader.setDataSource(dataSource)
        reader.setRowMapper(BillingRowMapper())
        return reader
    }

    private fun billingWriter(): ItemWriter<*>{
        return JdbcBatchItemWriterBuilder<BillingDTO>()
            .dataSource(dataSource)
            .sql("insert into facturacion_unificada (idSite, count) values (?, ?) ;")
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider<BillingDTO>())
            .itemPreparedStatementSetter(setter())
            .build()
    }

    fun setter(): ItemPreparedStatementSetter<BillingDTO> {
        return ItemPreparedStatementSetter<BillingDTO> { item: BillingDTO, ps: PreparedStatement ->
            ps.setString(1, item.idSite)
            ps.setLong(2, item.count)
        }
    }

    @Bean(name = ["myJobLauncher"])
    @Throws(Exception::class)
    fun simpleJobLauncher(jobRepository: JobRepository?): JobLauncher? {
        val jobLauncher = TaskExecutorJobLauncher()
        jobLauncher.setJobRepository(jobRepository!!)
        jobLauncher.setTaskExecutor(SimpleAsyncTaskExecutor())
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }
}