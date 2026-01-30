package com.example.service.auth.dto;

public class VerifyResponse {
    private String name;

    public VerifyResponse() {}

    public VerifyResponse(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
