package lab1.src.kotlin

class Checker(address: InitAddress): Thread() {
    private var addr = address
    private val sleepTime = 1000L

    override fun run(){
        while (!currentThread().isInterrupted){
            try{
                sleep(sleepTime)
            } catch (e: InterruptedException){
                e.printStackTrace()
            }

            addr.check()
        }
    }
}