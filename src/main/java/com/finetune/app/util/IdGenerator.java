package com.finetune.app.util;


import java.util.UUID;

public class IdGenerator {

    public static UUID newId() {
        return UUID.randomUUID();
    }
}