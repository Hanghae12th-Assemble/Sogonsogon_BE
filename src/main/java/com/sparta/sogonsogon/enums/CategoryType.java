package com.sparta.sogonsogon.enums;

import lombok.Getter;

@Getter
public enum CategoryType {

    음악(Value.음악),
    일상(Value.일상),
    도서(Value.도서),
    ASMR(Value.ASMR);

//    음악,
//    일상,
//    도서,
//    ASMR;

    private final String value;

    CategoryType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static class Value {
        public static final String 음악 = "음악";
        public static final String 일상 = "일상";
        public static final String ASMR = "ASMR";
        public static final String 도서 = "도서";
    }
}
