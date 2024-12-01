package it.lorenzobugiani.junit.extension

import it.lorenzobugiani.config.RabbitConfig
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

class RabbitTestExtension : BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private val rabbitMQ: RabbitMQContainer =
        RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"))
            .withExposedPorts(5672)
            .waitingFor(Wait.forListeningPorts(5672))

    override fun beforeAll(extensionContext: ExtensionContext) {
        rabbitMQ.start()
    }

    override fun afterAll(extensionContext: ExtensionContext) {
        rabbitMQ.stop()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        parameterContext.parameter.type === RabbitConfig::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        RabbitConfig(rabbitMQ.host, rabbitMQ.amqpPort)
}