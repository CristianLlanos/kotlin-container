package com.cristianllanos.container

// Shared test fixtures

class SimpleService

class ServiceA
class ServiceB(val a: ServiceA)

interface RegisteredAbstractDependency
class AnotherSimpleDependency : RegisteredAbstractDependency

// Circular dependency fixtures
class CircularA(val b: CircularB)
class CircularB(val a: CircularA)

// Deep chain fixtures: Level0 -> Level1 -> Level2 -> Level3 -> Level4 -> SimpleService
class Level4(val service: SimpleService)
class Level3(val dep: Level4)
class Level2(val dep: Level3)
class Level1(val dep: Level2)
class Level0(val dep: Level1)
