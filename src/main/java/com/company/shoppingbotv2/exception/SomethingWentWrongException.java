package com.company.shoppingbotv2.exception;

public class SomethingWentWrongException extends RuntimeException {
    private final Object data;

    public SomethingWentWrongException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public String getInfo() {
        return String.format("""
                Xatolik yuz berdi xatolik (SomethingWentWrongException):
                
                ```
                %s
                ```
                
                ```
                %s
                ```
                """, this.getMessage(), this.data.toString());
    }
}
