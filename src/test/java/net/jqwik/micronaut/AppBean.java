package net.jqwik.micronaut;

import jakarta.inject.Singleton;

@Singleton
class AppBean {
    public String method() {
        return "Hello world!";
    }
}
