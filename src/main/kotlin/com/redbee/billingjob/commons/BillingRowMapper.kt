package com.redbee.jobcdl.commons.database

import com.redbee.billingjob.dto.BillingDTO
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.SQLException

class BillingRowMapper : RowMapper<BillingDTO> {
    @Throws(SQLException::class)
    override fun mapRow(rs: ResultSet, rowNum: Int): BillingDTO {
        return BillingDTO(rs.getString(ID_SITE),rs.getLong(COUNT) );
    }

    companion object {
        const val COUNT = "count"
        const val ID_SITE = "idSite"
    }
}