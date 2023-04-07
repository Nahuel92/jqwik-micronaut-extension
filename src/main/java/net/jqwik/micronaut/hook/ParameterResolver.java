package net.jqwik.micronaut.hook;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.annotation.Nonnull;
import net.jqwik.api.JqwikException;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.api.lifecycle.ParameterResolutionContext;
import net.jqwik.api.lifecycle.ResolveParameterHook;
import net.jqwik.api.lifecycle.TryLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ParameterResolver implements ResolveParameterHook {
    private <T> Qualifier<T> resolveQualifier(final Argument<?> argument) {
        final AnnotationMetadata annotationMetadata = Objects.requireNonNull(argument, "Argument cannot be null")
                .getAnnotationMetadata();
        boolean hasMetadata = annotationMetadata != AnnotationMetadata.EMPTY_METADATA;

        final List<String> qualifierTypes = hasMetadata ?
                annotationMetadata.getAnnotationNamesByStereotype(AnnotationUtil.QUALIFIER) :
                Collections.emptyList();
        if (CollectionUtils.isEmpty(qualifierTypes)) {
            return null;
        }

        if (qualifierTypes.size() == 1) {
            return Qualifiers.byAnnotation(annotationMetadata, qualifierTypes.get(0));
        }

        @SuppressWarnings("unchecked") final Qualifier<T>[] qualifiers = qualifierTypes
                .stream()
                .map((type) -> Qualifiers.<T>byAnnotation(annotationMetadata, type))
                .toArray(Qualifier[]::new);
        return Qualifiers.byQualifiers(qualifiers);
    }

    @Override
    @NonNullApi
    @Nonnull
    public Optional<ParameterSupplier> resolve(final ParameterResolutionContext parameterContext,
                                               final LifecycleContext lifecycleContext) {
        return Optional.of(new MicronautSupplier(parameterContext));
    }

    private Argument<?> getArgument(final ParameterResolutionContext parameterContext,
                                    final ApplicationContext applicationContext) {
        try {
            final Executable declaringExecutable = parameterContext.parameter().getDeclaringExecutable();
            final int index = parameterContext.index();
            if (declaringExecutable instanceof Constructor) {
                final Class<?> declaringClass = declaringExecutable.getDeclaringClass();
                final BeanDefinition<?> beanDefinition = applicationContext.findBeanDefinition(declaringClass)
                        .orElse(null);
                if (beanDefinition != null) {
                    final Argument<?>[] arguments = beanDefinition.getConstructor().getArguments();
                    if (index < arguments.length) {
                        return arguments[index];
                    }
                }
            } else {
                final ExecutableMethod<?, Object> executableMethod = applicationContext.getExecutableMethod(
                        declaringExecutable.getDeclaringClass(),
                        declaringExecutable.getName(),
                        declaringExecutable.getParameterTypes()
                );
                final Argument<?>[] arguments = executableMethod.getArguments();
                if (index < arguments.length) {
                    return arguments[index];
                }
            }
        } catch (final NoSuchMethodException e) {
            return null;
        }
        return null;
    }

    private class MicronautSupplier implements ParameterSupplier {
        private final ParameterResolutionContext parameterContext;

        MicronautSupplier(final ParameterResolutionContext parameterContext) {
            this.parameterContext = parameterContext;
        }

        @Override
        @NonNullApi
        @Nonnull
        public Object get(final Optional<TryLifecycleContext> optionalTry) {
            final var applicationContext = JqwikMicronautExtension.STORE.get().getApplicationContext();
            final Argument<?> argument = getArgument(parameterContext, applicationContext);
            if (argument != null) {
                final var parameter = parameterContext.parameter();
                if (argument.isAnnotationPresent(Value.class) || argument.isAnnotationPresent(Property.class)) {
                    if (parameter.isAnnotationPresent(Value.class)) {
                        return applicationContext.getEnvironment()
                                .getProperty(parameter.getAnnotation(Value.class).value()
                                                .replaceAll("[${}]", StringUtils.EMPTY_STRING),
                                        parameter.getType()
                                ).orElseThrow(() ->
                                        new JqwikException("Unresolvable property specified to @Value: " + parameter.getName())
                                );
                    }

                    final var propertyName = parameter.getAnnotation(Property.class).name();
                    if (propertyName.isEmpty()) {
                        return applicationContext.getBean(parameter.getType(), resolveQualifier(argument));
                    }
                    return applicationContext.getEnvironment()
                            .getProperty(propertyName, parameter.getType())
                            .orElseThrow(() ->
                                    new JqwikException("Unresolvable property specified to @Property: " + parameter.getName())
                            );
                }
                return applicationContext.getBean(argument.getType(), resolveQualifier(argument));
            }
            return applicationContext.getBean(parameterContext.parameter().getType());
        }
    }
}
