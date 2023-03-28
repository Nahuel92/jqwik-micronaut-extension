package net.jqwik.micronaut.annotation;

import io.micronaut.context.annotation.Property;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@Property(name = "datasources.default.name", value = "testdb")
@Property(name = "jpa.default.properties.hibernate.hbm2ddl.auto", value = "update")
public @interface DbProperties {
}