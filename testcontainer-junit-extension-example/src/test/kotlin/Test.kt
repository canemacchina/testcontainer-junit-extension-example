package com.testcontainers.demo

import ConnectionProvider
import SendMessage
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName


internal class Test {
    private lateinit var sendMessage: SendMessage

    @BeforeEach
    fun setUp() {
        sendMessage = SendMessage(connectionProvider, rabbitMQ.host, rabbitMQ.amqpPort)
    }

    @Test
    fun shouldGetCustomers() {
        connectionProvider.getConnection().use { connection ->
            val statement = connection.prepareStatement(
                """
                    insert into msg_table (message) values ('some message');
                """.trimIndent()
            )
            statement.execute()
        }

        sendMessage.sendMessage()

        readMessage()
    }

    private fun readMessage() {
        val factory = ConnectionFactory()
        factory.host = rabbitMQ.host
        factory.port = rabbitMQ.amqpPort
        val connection = factory.newConnection()
        val channel = connection.createChannel()

        val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
            val message = String(delivery.body, charset("UTF-8"))
            println("Received '$message'")
            connection.close()
        }
        channel.basicConsume("queue", true, deliverCallback) { consumerTag: String? -> }
    }

    companion object {
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        val rabbitMQ = RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"))
        @JvmStatic
        lateinit var connectionProvider: ConnectionProvider

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            println("starting POSTGRES")
            postgres.start()
            println("POSTGRES started")
            println("starting RABBITMQ")
            rabbitMQ.start()
            println("RABBITMQ started")

            Thread.sleep(3000)

            connectionProvider = ConnectionProvider(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password
            )
            connectionProvider.getConnection().use { connection ->
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

        @AfterAll
        @JvmStatic
        fun afterAll() {
            postgres.stop()
            rabbitMQ.stop()
        }
    }
}