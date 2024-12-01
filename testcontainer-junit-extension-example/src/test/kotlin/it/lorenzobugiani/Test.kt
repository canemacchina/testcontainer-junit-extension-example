package it.lorenzobugiani

import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import it.lorenzobugiani.config.DbConfig
import it.lorenzobugiani.config.RabbitConfig
import it.lorenzobugiani.junit.extension.PostgresTestExtension
import it.lorenzobugiani.junit.extension.RabbitTestExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

const val MESSAGE = "SOME MESSAGE"

@ExtendWith(PostgresTestExtension::class)
@ExtendWith(RabbitTestExtension::class)
class Test(
    dbConfig: DbConfig,
    rabbitConfig: RabbitConfig
) {
    private val dbConnectionPool = DbConnectionPool(dbConfig)
    private val rabbitConnectionFactory = RabbitConnectionFactory(rabbitConfig)
    private val app = App(ReadMessage(dbConnectionPool), SendMessage(rabbitConnectionFactory))

    @Test
    fun `should send the message`() {
        insertMessageIntoPostgres()

        app.run()

        val message = readMessage()

        assertEquals(MESSAGE, message)
    }

    private fun insertMessageIntoPostgres() {
        dbConnectionPool.getConnection().use { connection ->
            connection
                .prepareStatement("insert into msg_table (message) values ('$MESSAGE');")
                .execute()
        }
    }

    private fun readMessage(): String {
        val messageFuture = CompletableFuture<String>()

        val connection = rabbitConnectionFactory.getConnection()
        val channel = connection.createChannel()

        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            val message = String(delivery.body, charset("UTF-8"))
            messageFuture.complete(message)
            connection.close()
        }

        channel.basicConsume("queue", true, deliverCallback) { _: String? -> }

        return messageFuture.join()
    }
}