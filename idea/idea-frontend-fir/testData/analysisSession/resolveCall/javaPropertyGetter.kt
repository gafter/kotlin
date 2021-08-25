// FILE: call.kt
fun call() {
    val javaClass = JavaClass()
    javaClass.<expr>foo</expr>
}

// FILE: JavaClass.java
class JavaClass {
    int getFoo() { return 42; }
}

// CALL: KtFunctionCall: targetFunction = /JavaClass.getFoo(): kotlin.Int
