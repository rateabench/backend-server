package com.rateabench.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection


/**
 * Created by Jonathan Schurmann on 5/10/19.
 */
class DataSource {

    companion object {
        private val ds: HikariDataSource

        init {
            val config = HikariConfig("resources/hikari.properties")
            ds = HikariDataSource(config)
        }

        fun getConnection(): Connection {
            return ds.connection
        }
    }

}