package com.company.shoppingbotv2.exception;

public class ApiNotWorkingException extends RuntimeException {
    private final Object data;

    public ApiNotWorkingException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public String getInfo() {
        return String.format("""
                Xatolik yuz berdi (ApiNotWorkingException):
                
                ```
                %s
                ```
                                
                ```
                %s
                ```
                """, this.getMessage(), this.data.toString());
    }
}
