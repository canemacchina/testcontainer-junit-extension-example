import com.rabbitmq.client.ConnectionFactory


class SendMessage(
    private val connectionProvider: ConnectionProvider,
    private val rabbitMQHost: String,
    private val rabbitMQPort: Int
) {

    fun sendMessage() {
        val message = connectionProvider.getConnection().use { connection ->
            val statement = connection.prepareStatement("select * from msg_table")
            val result = statement.executeQuery()
            result.next()
            result.getString("message")
        }

        println("message read")

        val factory = ConnectionFactory()
        factory.host = rabbitMQHost
        factory.port = rabbitMQPort
        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare("queue", false, false, false, null)
                channel.basicPublish("", "queue", null, message.toByteArray())
            }
            println("message sent")
        }
    }
}