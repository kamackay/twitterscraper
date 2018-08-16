package com.twitterscraper.utils.benchmark;

import com.twitterscraper.utils.StringMaker;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import static com.twitterscraper.utils.benchmark.BenchmarkData.data;
import static com.twitterscraper.utils.benchmark.BenchmarkTimer.timer;

public class FunctionInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Benchmark annotation = invocation.getMethod().getAnnotation(Benchmark.class);
        final StringBuilder nameBuilder = new StringBuilder(String.format("%s.%s",
                invocation.getThis().getClass().getSuperclass().getSimpleName(),
                invocation.getMethod().getName()));
        try {
            if (annotation.logAllParams()) {
                nameBuilder.append(".").append(paramsToString(invocation.getArguments()));
            } else if (annotation.paramName()) {
                nameBuilder.append(".").append((String) invocation.getArguments()[0]);
            }
        } catch (Exception e) {
            // Something went wrong getting the first string parameter, move on
        }
        final String name = nameBuilder.toString();
        timer().start(data(name, annotation.limit()));
        Object result = invocation.proceed();
        timer().end(name);
        return result;
    }

    private String paramsToString(Object[] params) {
        final StringMaker maker = new StringMaker("{ ");
        for (int i = 0; i < params.length; i++) {
            maker.append(String.valueOf(params[i]));
            if (i != params.length - 1) maker.append(", ");
        }
        return maker.append(" }").toString();
    }
}
