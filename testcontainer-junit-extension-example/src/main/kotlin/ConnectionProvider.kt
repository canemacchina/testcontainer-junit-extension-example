import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class ConnectionProvider(jdbcUrl: String, username: String, password: String) {
    private val ds: HikariDataSource

    init {
        val config = HikariConfig()
        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        ds = HikariDataSource(config)
    }

    fun getConnection(): Connection = ds.connection
}