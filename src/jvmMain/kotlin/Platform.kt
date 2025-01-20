actual object Platform {
    const val a = 2
    const val b = 3

    external fun plus(x: Int, y: Int): Int

    init {
        // TODO: Load native library
    }

    actual val name = "JVM. Also, $a + $b equals to ${plus(a, b)} in Rust"
}
