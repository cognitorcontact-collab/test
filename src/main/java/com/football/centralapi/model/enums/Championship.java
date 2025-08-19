package com.football.centralapi.model.enums;

public enum Championship {
    PREMIER_LEAGUE("http://localhost:8080"),
    LA_LIGA("http://localhost:8081"),
    BUNDESLIGA("http://localhost:8082"),
    SERIA("http://localhost:8083"),
    LIGUE_1("http://localhost:8084");
    
    private final String apiUrl;
    
    Championship(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
}