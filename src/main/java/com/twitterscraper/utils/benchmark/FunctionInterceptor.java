package com.twitterscraper.utils.benchmark;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import static com.twitterscraper.utils.benchmark.BenchmarkData.data;
import static com.twitterscraper.utils.benchmark.BenchmarkTimer.timer;

public class FunctionInterceptor implements MethodInterceptor {
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final String name = String.format("%s.%s",
                getClass().getTypeName(),
                invocation.getMethod().getName());
        timer().start(data(name, 0).logAbsolute(true));
        Object result = invocation.proceed();
        // hide the waiting cursor
        timer().end(name);
        return result;
    }
}
