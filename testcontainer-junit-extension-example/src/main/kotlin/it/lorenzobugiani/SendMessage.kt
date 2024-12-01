package it.lorenzobugiani

import java.nio.charset.Charset

class SendMessage(private val rabbitConnectionFactory: RabbitConnectionFactory) {
    operator fun invoke(message: String) {
        rabbitConnectionFactory.getConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare("queue", false, false, false, null)
                channel.basicPublish("", "queue", null, message.toByteArray(charset("UTF-8")))
            }
            println("message sent")
        }
    }
}