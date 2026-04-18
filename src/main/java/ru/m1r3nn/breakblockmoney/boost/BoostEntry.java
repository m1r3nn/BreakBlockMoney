package ru.m1r3nn.breakblockmoney.boost;

public class BoostEntry {

    private final String boostName;
    private final long expiresAt;

    public BoostEntry(String boostName, long expiresAt) {
        this.boostName = boostName;
        this.expiresAt = expiresAt;
    }

    public String getBoostName() {
        return boostName;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}