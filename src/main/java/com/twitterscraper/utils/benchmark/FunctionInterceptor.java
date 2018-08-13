package com.twitterscraper.utils.benchmark;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import static com.twitterscraper.utils.benchmark.BenchmarkData.data;
import static com.twitterscraper.utils.benchmark.BenchmarkTimer.timer;

public class FunctionInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        final StringBuilder nameBuilder = new StringBuilder(String.format("%s.%s",
                invocation.getThis().getClass().getSuperclass().getSimpleName(),
                invocation.getMethod().getName()));
        try {
            if (invocation.getMethod().getAnnotation(Benchmark.class).paramName()) {
                nameBuilder.append(".").append((String) invocation.getArguments()[0]);
            }
        } catch (Exception e) {
            // Something went wrong getting the first string parameter, move on
        }
        final String name = nameBuilder.toString();
        timer().start(data(name, 0).logAbsolute(true));
        Object result = invocation.proceed();
        timer().end(name);
        return result;
    }
}
