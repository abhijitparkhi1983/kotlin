import kotlin.experimental.ExperimentalTypeInference

interface FlowCollector<in T> {}

@Suppress("EXPERIMENTAL_API_USAGE_ERROR")
fun <L> flow(@BuilderInference block: suspend FlowCollector<L>.() -> Unit) = Flow(block)

class Flow<out R>(private val block: suspend FlowCollector<R>.() -> Unit)

fun poll72(): Flow<String> {
    return flow {
        val inv = {{}}!!
        inv()
    }
}
