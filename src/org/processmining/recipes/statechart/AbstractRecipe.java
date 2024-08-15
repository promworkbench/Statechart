package org.processmining.recipes.statechart;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.common.base.Function;

public abstract class AbstractRecipe<F, T, P> implements Function<F, T> {

    private static final Logger logger = LogManager
            .getLogger(AbstractRecipe.class.getName());

    private final P parameters;

    public AbstractRecipe(P parameters) {
        this.parameters = parameters;
    }

    public P getParameters() {
        return parameters;
    }

    @Override
    public T apply(F input) {
        final String name = this.getClass().getSimpleName();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Start recipe '%s' ...", name));
        }

        long start = System.nanoTime();
        T output = execute(input);
        long end = System.nanoTime();

        if (logger.isDebugEnabled()) {
            double tExec = (double) (end - start) / 1000.0 / 1000.0;
            logger.debug(String.format(
                    "Completed recipe '%s', exec = %.2f ms",
                    name, tExec));
        }

        return output;
    }

    protected abstract T execute(F input);
}
