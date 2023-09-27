package lab2
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = Server()
        server.start()

        val client = Client()
        javaClass.classLoader.getResource("4004.png")?.let { client.setResourcePath(it.path) }
        client.start()

        val client1 = Client()
        javaClass.classLoader.getResource("README.md")?.let{client1.setResourcePath(it.path)}
        client1.start()

        val client2 = Client()
        javaClass.classLoader.getResource("k5ieDIP2li1KN_AQfI9oJ15AYK-KB8fxVJ-riLRlPirUbrc1SLHA_mT_jZtniRhx2DqR8k2xycQAztPzF2HVjcTx.jpg")?.let {client2.setResourcePath(it.path)}
        client2.start()
    }
}