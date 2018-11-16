package com.solace.apac.demo.stockmarket;

public class Customer {
    private String clientId;
    private String displayName;
    private byte serviceLevel;

    public Customer() {
        this.clientId = "NULL-CLIENT-ID";
        this.displayName = "NULL CUSTOMER";
        this.serviceLevel = 0;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(byte serviceLevel) {
        this.serviceLevel = serviceLevel;
    }
}
