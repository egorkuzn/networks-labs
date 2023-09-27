package lab1.src.kotlin

import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap

class InitAddress {
    private val addr = ConcurrentHashMap<SocketAddress, Long>()

    public fun add(addr: SocketAddress, time: Long){
        if (this.addr.containsKey(addr))
            this.addr.replace(addr, time)
        else {
            this.addr[addr] = time
            print("GOT!: ")
            updateConsole()
        }
    }

    public fun updateConsole(){
        for(entry in addr)
            println("${entry.key} | ${entry.value}")

        println("*********************")
    }

    public fun check(){
        var flag = true
        val iterator: Iterator<Map.Entry<SocketAddress, Long>> = addr.iterator()

        while (iterator.hasNext()){
            val entry = iterator.next()
            val timeout = 4000L

            if(timeout < (System.currentTimeMillis() - entry.value)){
                flag = false
                break
            }
        }

        if(!flag) {
            updateConsole()
        }
    }
}