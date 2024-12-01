package it.lorenzobugiani.junit.extension

import it.lorenzobugiani.DbConnectionPool
import it.lorenzobugiani.config.DbConfig
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

class PostgresTestExtension : BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private val postgres: PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:16-alpine")
            .withExposedPorts(5432)
            .waitingFor(Wait.forListeningPorts(5432))

    override fun beforeAll(extensionContext: ExtensionContext) {
        postgres.start()
        val dbConnectionPool = DbConnectionPool(
            DbConfig(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password
            )
        )
        dbConnectionPool.getConnection().use { connection ->
            val statement = connection.prepareStatement(
                """
                    create table if not exists msg_table (
                        id BIGSERIAL PRIMARY KEY,
                        message varchar not null
                    )
                """.trimIndent()
            )
            statement.execute()
        }
    }

    override fun afterAll(extensionContext: ExtensionContext) {
        postgres.stop()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        parameterContext.parameter.type === DbConfig::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        DbConfig(postgres.jdbcUrl, postgres.username, postgres.password)
}