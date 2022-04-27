package cryptography

import java.io.File
import java.io.IOException
import javax.imageio.ImageIO


fun main() {
    println(Tasks.values().joinToString(separator = ", ", prefix = "Task (", postfix = "):"))
    do {
        var task: String = readLine()!!.toString()
        when(task.lowercase())
        {
            Tasks.exit.toString() -> {
                println("Bye!")
                break
            }
            Tasks.hide.toString() -> hide()
            Tasks.show.toString() -> show()
            "hash" -> hash()
            else -> println("Wrong task: ${task}")
        }
        println(Tasks.values().joinToString(separator = ", ", prefix = "Task (", postfix = "):"))
    }while (true)

}


const val DELIMITATOR = "000000000000000000000011"

/**
 * Main
 */

