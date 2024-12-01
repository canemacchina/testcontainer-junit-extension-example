package it.lorenzobugiani

class App(
    private val readMessage: ReadMessage,
    private val sendMessage: SendMessage
) {

    fun run() {
        sendMessage(readMessage())
    }
}