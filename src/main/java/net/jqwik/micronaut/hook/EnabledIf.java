package net.jqwik.micronaut.hook;

import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.api.lifecycle.SkipExecutionHook;
import net.jqwik.micronaut.adapter.JupiterExtensionContextAdapter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;

class EnabledIfHook implements SkipExecutionHook {
    @Override
    public SkipResult shouldBeSkipped(LifecycleContext context) {
        if (!context.findAnnotation(EnabledIf.class).isPresent()) {
            return SkipResult.doNotSkip();
        }
        ExtensionContext extensionContext = new JupiterExtensionContextAdapter(context);
        /*ConditionEvaluationResult evaluationResult = new EnabledIfCondition().evaluateExecutionCondition(extensionContext);
        if (evaluationResult.isDisabled()) {
            return SkipResult.skip(evaluationResult.getReason().orElse(null));
        }*/
        return SkipResult.doNotSkip();
    }

}
