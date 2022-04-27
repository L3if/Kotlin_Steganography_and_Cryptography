package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
import java.util.BitSet
import java.util.Vector
import javax.imageio.ImageIO
import kotlin.experimental.xor

enum class Tasks{
    hide,
    show,
    exit,
    //hash
}

operator fun Byte.get(i: Int ) : Int{
    return ((this.toInt() shr i) and 0b0001)
}
operator fun Byte.set(i: Int , b: Boolean) : Byte{
    return if (b)
        ((this.toInt() + (0b0001 shl i)).toByte())
    else
        this
}

fun hash() {
    var a = BitArray("test")
    a.printAsBits()
    a.printAsString()
    println("Input image file:")
    val inPath = File(readLine()!!.toString())
    val inputImage = ImageIO.read(inPath)
    val imageByteArray = ByteArray(3 * inputImage.width * inputImage.height)
    var index = 0
    for (y in 0 until inputImage.height) {
        for (x in 0 until inputImage.width) {
            val color = Color(inputImage.getRGB(x, y))
            imageByteArray[index] = color.red.toByte()
            index++
            imageByteArray[index] = color.green.toByte()
            index++
            imageByteArray[index] = color.blue.toByte()
            index++
        }
    }
    println(imageByteArray.joinToString("") { "%02x".format(it)})

    val md = MessageDigest.getInstance("SHA-1")
    md.update(imageByteArray)
    println(md.digest().joinToString("") { "%02x".format(it)})
}


fun hide(){
    println("Input image file:")
    val inPath = File(readLine()!!.toString())
    println("Output image file:")
    val outPath = File(readLine()!!.toString())
    println("Message to hide:")
    var message = readLine()!!
    println("Password:")
    val password = readLine()!!
    message = encrypt(message,password) + "\u0000\u0000\u0003"
    val msg = BitArray(message)
    try {
        val inImg = ImageIO.read(inPath)
        val result = messageToImage(inImg, msg)
        ImageIO.write(result,"png",outPath)
        println("Message saved in  ${outPath} image.")
    } catch (e: Exception){
        println(e.message)
    }
};
fun encrypt(message: String, password: String): String{
    var messageBytes = message.toByteArray()
    var passwordBytes = password.toByteArray()
    for(i in messageBytes.indices){
        messageBytes[i] = messageBytes[i] xor passwordBytes[i%passwordBytes.size]
    }
    return messageBytes.toString(Charsets.UTF_8)
}

fun setLastBit(pixel: Int, bit: Boolean): Int {
    if (bit) {
        return pixel.and(254).or(1)
    }
    else{
        return pixel.and(254).or(0)
    }
}

fun messageToImage(image: BufferedImage, message: BitArray): BufferedImage {
    if(message.bits.length() > image.width*image.height)
        throw error("The input image is not large enough to hold this message.")

    var colArray = IntArray(image.width*image.height)
    // println(bits.size)
    var ins = 0
    for (y in 0 until image.height) { // --> y --> height
        for (x in 0 until image.width) { // --> x width
            if (ins < message.bits.length()) {
                val color = Color(image.getRGB(x, y))
                // println("Color Original: ${color.blue}")
                val rgb = Color(
                    color.red,
                    color.green,
                    setLastBit(color.blue, message.bits[ins]),
                ).getRGB()
                // println("Color Cambiado: ${rgb.blue}")
                image.setRGB(x,y,rgb)
                //println("ins: ${ins+1}")
                ins++
            } else {
                //println(ins)
                return image
            }
        }
    }
    return image
}

class BitArray {
    var bits = BitSet()

    constructor(str: String) {
        var tmp = str.encodeToByteArray()
        for (i in tmp.indices)
            for (j in 0..7)
                bits.set(i * 8 + j, tmp[i][7-j] == 1)
    }

    fun printAsBits() {
        println()
        print("{ ")
        for (i in 0 until bits.length())
            print(" ${bits[i]}, ")
        println(" }")
    }

    fun printAsString() {
        println(bits.toByteArray().toString(Charsets.UTF_8))
    }
}



    fun show() {
        println("Input image file:")
        val inputFile = readLine()!! // "out.png"
        println("Password:")
        val password = readLine()!!
        try {
            val read = ImageIO.read(File(inputFile))
            //  BufferedImage.TYPE_INT_RGB
            //var image = BufferedImage(read.width, read.height, BufferedImage.TYPE_INT_RGB)
            val bits = bitFromImage(read)

            if (DELIMITATOR in bits) {
                val messageBits = bits.substring(0, bits.indexOf(DELIMITATOR))
                var message = messageFromBits(messageBits)
                message = encrypt(message,password)
                println("Message:")
                println(message)
            } else {
                println("No message in the image")
            }

        } catch (e: Exception) {
            println("Can't read input file!")
        }
    }

fun messageFromBits(messageBits: String): String {
    val message = StringBuilder()
    // println("Message: ${messageBits}")
    // println("Message: ${messageBits.length}")
    for (i in messageBits.indices step 8) {
        val byte = messageBits.substring(i, i + 8)
        val byteInt = byte.toInt(2)
        val char = byteInt.toChar()
        message.append(char)
    }
    return message.toString()
}

fun bitFromImage(image: BufferedImage): String {
    val bits : StringBuilder = StringBuilder()
    for (y in 0 until image.height) { // --> y --> height
        for (x in 0 until image.width) { // --> x width
            val color = Color(image.getRGB(x, y))
            val blue = color.blue
            //println(blue)
            val bit = blue.and(1)
            //println(bit)
            bits.append(bit)
        }
    }
    return bits.toString()
}

/**
 * Hiding the message in the image
 */