package it.lorenzobugiani

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import it.lorenzobugiani.config.DbConfig
import java.sql.Connection

class DbConnectionPool(config: DbConfig) {
    private val ds: HikariDataSource

    init {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = config.jdbcUrl
        hikariConfig.username = config.username
        hikariConfig.password = config.password
        ds = HikariDataSource(hikariConfig)
    }

    fun getConnection(): Connection = ds.connection
}