package com.firefly.ragdemo.util;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {}

    public static String newUuid() {
        return UUID.randomUUID().toString();
    }
} 