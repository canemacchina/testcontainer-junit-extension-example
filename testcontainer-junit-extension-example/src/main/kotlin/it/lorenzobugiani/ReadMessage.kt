package it.lorenzobugiani

class ReadMessage(private val dbConnectionPool: DbConnectionPool) {
    operator fun invoke(): String =
        dbConnectionPool.getConnection().use { connection ->
            val statement = connection.prepareStatement("select * from msg_table")
            val result = statement.executeQuery()
            result.next()
            result.getString("message")
        }
}