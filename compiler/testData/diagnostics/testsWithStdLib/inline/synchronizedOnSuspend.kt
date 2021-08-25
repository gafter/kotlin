// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

<!SYNCHRONIZED_ON_SUSPEND!>@Synchronized<!>
suspend fun foo(f: () -> Unit): Unit = f()

fun builder(c: suspend () -> Unit) {}

val c = builder (<!SYNCHRONIZED_ON_SUSPEND!>@Synchronized<!> {})
val d = suspend <!SYNCHRONIZED_ON_SUSPEND!>@Synchronized<!> {}
