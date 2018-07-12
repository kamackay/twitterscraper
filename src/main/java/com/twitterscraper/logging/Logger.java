package com.twitterscraper.logging;

public class Logger {

    private final Class classname;

    public Logger(Class classname) {
        this.classname = classname;
    }

    public void log(String s) {
        // TODO make this an actual logger
        System.out.println(String.format("%s - %s",
                classname.getName(),
                s));
    }

    public void e(Throwable e){
        for (StackTraceElement element : e.getStackTrace()){
            this.log(element.toString());
        }
    }
}
