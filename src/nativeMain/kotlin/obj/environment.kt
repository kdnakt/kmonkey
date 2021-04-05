package obj

data class Environment(val store: MutableMap<String, Obj>)

fun Environment.get(name: String): Obj? {
    return store[name]
}

fun Environment.set(name: String, obj: Obj): Obj {
    store[name] = obj
    return obj
}

fun newEnvironment(): Environment {
    return Environment(mutableMapOf())
}

