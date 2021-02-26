import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import java.util.concurrent.ExecutionException

suspend fun computeValue(): String = coroutineScope {
    val one = async { computeOne() }
    val two = async { computeTwo() }
    delay(5000)
    combineResults(one, two)
}

suspend fun combineResults(one: Deferred<String>, two: Deferred<String>): String =
    one.await() + two.await()

suspend fun computeOne(): String {
    delay(5000)
    return "4"
}

suspend fun computeTwo(): String {
    delay(5000)
    throw ExecutionException(null)
}

suspend fun nestedMethod(deferred: Deferred<*>) {
    oneMoreNestedMethod(deferred)
    computeTwo()
}

suspend fun oneMoreNestedMethod(deferred: Deferred<*>) {
    try {
        deferred.await()
        computeOne()
    } catch (e: ExecutionException) {
        println(e.stackTraceToString())
    }
}

@ExperimentalCoroutinesApi
fun main() {
    System.setProperty(DEBUG_PROPERTY_NAME,DEBUG_PROPERTY_VALUE_ON)
    DebugProbes.sanitizeStackTraces = false
    DebugProbes.enableCreationStackTraces = true
    DebugProbes.install()
    try {
        runBlocking {
            val deferred = async(NonCancellable) { computeValue() }
            // Delay for some time
            delay(1000)
            // Dump running coroutines
            DebugProbes.dumpCoroutines()
            println("\nDumping only deferred")
            DebugProbes.printJob(deferred)
            deferred.await()
        }
    } finally {
        DebugProbes.uninstall()
    }
}