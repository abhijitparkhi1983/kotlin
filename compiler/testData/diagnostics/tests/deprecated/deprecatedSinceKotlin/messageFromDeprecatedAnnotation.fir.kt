package kotlin

@Deprecated("foo test")
@DeprecatedSinceKotlin(warningSince = "1.0")
fun foo() {}

fun test() {
    foo()
}
