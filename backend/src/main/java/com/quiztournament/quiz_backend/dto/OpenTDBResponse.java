package com.quiztournament.quiz_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for OpenTDB API response
 * Maps the external API response structure
 */
public class OpenTDBResponse {

    @JsonProperty("response_code")
    private Integer responseCode;

    @JsonProperty("results")
    private List<OpenTDBQuestion> results;

    // Constructors
    public OpenTDBResponse() {}

    public OpenTDBResponse(Integer responseCode, List<OpenTDBQuestion> results) {
        this.responseCode = responseCode;
        this.results = results;
    }

    // Getters and Setters
    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public List<OpenTDBQuestion> getResults() {
        return results;
    }

    public void setResults(List<OpenTDBQuestion> results) {
        this.results = results;
    }

    // Helper method to check if response is successful
    public boolean isSuccessful() {
        return responseCode != null && responseCode == 0;
    }

    // Response codes from OpenTDB API documentation
    public String getResponseMessage() {
        if (responseCode == null) return "Unknown error";

        switch (responseCode) {
            case 0: return "Success";
            case 1: return "No Results - Could not return results. The API doesn't have enough questions for your query.";
            case 2: return "Invalid Parameter - Contains an invalid parameter. Arguements passed in aren't valid.";
            case 3: return "Token Not Found - Session Token does not exist.";
            case 4: return "Token Empty - Session Token has returned all possible questions for the specified query. Resetting the Token is necessary.";
            default: return "Unknown response code: " + responseCode;
        }
    }
}