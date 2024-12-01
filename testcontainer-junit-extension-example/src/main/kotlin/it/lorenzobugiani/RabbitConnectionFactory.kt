package it.lorenzobugiani

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import it.lorenzobugiani.config.RabbitConfig

class RabbitConnectionFactory(rabbitConfig: RabbitConfig) {

    private val connectionFactory = ConnectionFactory().apply {
        host = rabbitConfig.host
        port = rabbitConfig.port
    }

    fun getConnection(): Connection = connectionFactory.newConnection()
}