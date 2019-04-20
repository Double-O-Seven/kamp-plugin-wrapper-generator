[![Build Status](https://travis-ci.org/Double-O-Seven/kamp-plugin-wrapper-generator.svg?branch=master)](https://travis-ci.org/Double-O-Seven/kamp-plugin-wrapper-generator)
[![Release Version](https://img.shields.io/maven-central/v/ch.leadrian.samp.kamp/kamp-plugin-wrapper-generator.svg?label=release)](http://search.maven.org/#search%7Cga%7C1%7Ckamp-plugin-wrapper-generator)

# Kamp Native Plugin Wrapper Generator

This Gradle plugin allows you to to generate constants, callbacks and native functions for a Kamp Kotlin API wrapping a SA-MP native plugin.

All you need to do, is provide a valid IDL file that can be parsed by [CIDL](https://github.com/Zeex/cidl). See [sampgdk](https://github.com/Zeex/sampgdk/blob/master/lib/sampgdk/a_samp.idl) for example.

Tasks and extensions
--------------------

The plugin provides a single task `generatePluginWrapper` which is configured by the `pluginWrapperGenerator` extension.

A minimal configuration will look like this:
```kotlin
plugins {
    id("ch.leadrian.samp.kamp.kamp-plugin-wrapper-generator") version "1.0.0-rc2"
}

pluginWrapperGenerator {
    // The package where the generated source code will be located
    packageName = "com.my.amazing.samp.plugin"
    
    // The plugin name, used as prefix for the generated classes
    pluginName = "MyAmazingPlugin"
    
    interfaceDefinitionFile(project.buildDir.resolve("src/main/idl/MyAmazingPlugin.idl"))
}
```

A complete configuration will look like this:
```kotlin
plugins {
    id("ch.leadrian.samp.kamp.kamp-plugin-wrapper-generator") version "1.0.0-rc2"
}

pluginWrapperGenerator {
    // The package where the generated source code will be located
    packageName = "com.my.amazing.samp.plugin"
    
    // The plugin name, used as prefix for the generated classes
    pluginName = "MyAmazingPlugin"
    
    // Add multiple IDL files
    interfaceDefinitionFiles(
            project.buildDir.resolve("src/main/idl/MyAmazingPlugin1.idl"),
            project.buildDir.resolve("src/main/idl/MyAmazingPlugin2.idl")
    )
    
    // Add a single IDL file
    interfaceDefinitionFile(project.buildDir.resolve("src/main/idl/MyAmazingPlugin3.idl"))
    
    // Remove prefix AM_ from generated functions and callbacks if a lot of the functions are prefixed, like in FCNPC or ColAndreas
    removePrefix("FCNPC_", "CA_")
    
    // Specify the case format in which the native functions are named. If specified, the generated function names will be converted to lower camel case.
    // By default, no format is specified.
    nativeFunctionsCaseFormat(CaseFormat.UPPER_CAMEL)
    
    // Specify the case format in which the callbacks are named. If specified, the generated callback names will be converted to lower camel case.
    // By default, upper camel case is assumed.
    callbacksCaseFormat(CaseFormat.UPPER_CAMEL)
}
```

Example
-------

Let's assume we want to wrap a simple plugin name `MyAmazingPlugin` with the following IDL file:

```
const int SOME_AMAZING_INT_VALUE = 1337;

[native] bool MA_SomeNativeFunction(int arg1, string arg2, [out] float outArg1);

[callback] bool MA_OnSomeEvent(int arg1, float arg2);
```

With the following extension configuration:

```
pluginWrapperGenerator {
    packageName = "com.my.amazing.samp.plugin"
    
    pluginName = "MyAmazingPlugin"
    
    interfaceDefinitionFile(project.buildDir.resolve("src/main/idl/MyAmazingPlugin.idl"))
    
    removePrefix("MA_")
    
    nativeFunctionsCaseFormat(CaseFormat.UPPER_CAMEL)
}
```

With this file as input, the Gradle plugin will generate the following classes:

`MyAmazingPluginConstants.kt`:
```kotlin
package com.my.amazing.samp.plugin

object MyAmazingPluginConstants {

    const val SOME_AMAZING_INT_VALUE: Int  = 1337

}
```

`MyAmazingPluginConstants.kt`:
```kotlin
package com.my.amazing.samp.plugin

@Singleton
class MyAmazingPluginNativeFunctions
@Inject
internal constructor(/* constructor parameters */){

    // Code that you don't need to worry about has been omitted

    fun someNativeFunction(arg1: Int, arg2: String, outArg1: MutableFloatCell): Bool {
        // Implementation details that you don't need to worry about
    }

}
```

`MyAmazingPluginCallbacks.kt`
```kotlin
package com.my.amazing.samp.plugin

interface MyAmazingPluginCallbacks {

    fun onSomeEvent(arg1: Int, arg2: Float): Boolean

}
```

`MyAmazingPluginCallbackManager.kt`
```kotlin
package com.my.amazing.samp.plugin

@Singleton
internal class MyAmazingPluginCallbackManager
@Inject
constructor(/* constructor parameters */){

    @PostConstruct
    fun initialize() {
        // Registers callbacks here
    }

}
```

Implementing the plugin wrapper
-------------------------------

Once you have generated the basic plugin API, you will need to do provide an implementation for `MyAmazingPluginCallbacks` or whatever your plugin callbacks class will be named. In order for it to be usable, it is also required to bind it in a Guice module:
```kotlin
bind(MyAmazingPluginCallbacks::class.java).to(MyAmazingPluginCallbacksImplementation::class.java)
```

In addition, you will also have to bind `MyAmazingPluginCallbackManager` as an eager singleton:
```kotlin
bind(MyAmazingPluginCallbackManager::class.java).asEagerSingleton()
```

A resulting Guice module could look like this:
```kotlin
package com.my.amazing.samp.plugin

import ch.leadrian.samp.kamp.core.api.inject.KampModule

class MyAmazingPluginModule : KampModule() {

    override fun configure() {
        bind(MyAmazingPluginCallbacks::class.java).to(MyAmazingPluginCallbacksImplementation::class.java)
        bind(MyAmazingPluginCallbackManager::class.java).asEagerSingleton()
    }

}
```

And of course, at least a minimal `ch.leadrian.samp.kamp.core.api.Plugin` implementation is required:
```kotlin
package com.my.amazing.samp.plugin

import ch.leadrian.samp.kamp.core.api.Plugin
import com.google.inject.Module

class MyAmazingPluginPlugin : Plugin() {

    override fun getModules(): List<Module> = listOf(MyAmazingPluginModule())

}
```

For a real example, have a look at [kamp-fcnpc-wrapper](https://github.com/Double-O-Seven/kamp-fcnpc-wrapper).
