package com.cristianllanos.container

// Shared test fixtures

class SimpleService

class ServiceA
class ServiceB(val a: ServiceA)

interface RegisteredAbstractDependency
class AnotherSimpleDependency : RegisteredAbstractDependency
