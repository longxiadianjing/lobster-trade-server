package com.lobster.trade.config;

public class AdminContext {
    private static final ThreadLocal<Long> CONTEXT = new ThreadLocal<>();

    public static void set(Long id) { CONTEXT.set(id); }
    public static Long get() { return CONTEXT.get(); }
    public static void remove() { CONTEXT.remove(); }
}