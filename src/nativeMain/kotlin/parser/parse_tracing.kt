package parser

private var traceLevel = 0

private val traceIdentPraceholder = "\t"

private fun identLevel() = traceIdentPraceholder.repeat(traceLevel)

private fun tracePrint(fs: String) {
    println("${identLevel()}$fs")
}

private fun incIdent() = traceLevel++
private fun decIdent() = traceLevel--

fun trace(msg: String): String {
    incIdent()
    tracePrint("BEGIN $msg")
    return msg
}

fun untrace(msg: String) {
    tracePrint("END $msg")
    decIdent()
}
