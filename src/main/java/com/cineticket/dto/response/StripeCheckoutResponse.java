package com.cineticket.dto.response;

public class StripeCheckoutResponse {

    private String sessionId;
    private String url;
    private String message;

    public StripeCheckoutResponse() {
    }

    public StripeCheckoutResponse(String sessionId, String url) {
        this.sessionId = sessionId;
        this.url = url;
        this.message = "Checkout session created successfully";
    }

    public StripeCheckoutResponse(String sessionId, String url, String message) {
        this.sessionId = sessionId;
        this.url = url;
        this.message = message;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
