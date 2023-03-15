package net.jqwik.micronaut;

import jakarta.inject.Singleton;

@Singleton
class MyObj {
    public String myMethod() {
        return "Hello world!";
    }
}
