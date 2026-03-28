package com.cristianllanos.container

interface Container : Registrar, Resolver, Caller

fun Container(autoResolver: AutoResolver = ReflectionAutoResolver()): Container =
    Dependencies.make(autoResolver)
