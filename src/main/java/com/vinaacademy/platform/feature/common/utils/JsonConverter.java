//package com.util;
//
//import com.google.gson.Gson;
//import jakarta.persistence.AttributeConverter;
//import jakarta.persistence.Converter;
//
//@Converter(autoApply = true)
//public class JsonConverter implements AttributeConverter<String, String> {
//    private static final Gson gson = new Gson();
//
//    @Override
//    public String convertToDatabaseColumn(String attribute) {
//        return attribute == null ? null : gson.toJson(attribute);
//    }
//
//    @Override
//    public String convertToEntityAttribute(String dbData) {
//        return dbData == null ? null : gson.fromJson(dbData, String.class);
//    }
//}
