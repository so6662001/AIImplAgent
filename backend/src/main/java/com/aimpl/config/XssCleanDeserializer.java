package com.aimpl.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

public class XssCleanDeserializer extends StringDeserializer {

    private static final long serialVersionUID = 1L;

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = super.deserialize(p, ctxt);
        return value != null ? stripHtml(value) : null;
    }

    private String stripHtml(String input) {
        return input.replaceAll("<[^>]*>", "");
    }
}
