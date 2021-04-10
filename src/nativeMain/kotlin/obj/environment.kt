package obj

data class Environment(
        val store: MutableMap<String, Obj?>,
        val outer: Environment?,
)

fun Environment.get(name: String): Obj? {
    val obj = store[name]
    if (obj == null && outer != null) {
        return outer.get(name)
    }
    return obj
}

fun Environment.set(name: String, obj: Obj?): Obj? {
    store[name] = obj
    return obj
}

fun newEnvironment(): Environment {
    return Environment(mutableMapOf(), null)
}

fun newEnclosedEnvironment(outer: Environment): Environment {
    return Environment(mutableMapOf(), outer)
}
