package org.novastack.iposca.templates.model;

public class ShopIdentity {
    private int id;
    private String pharmacyName;
    private String address;
    private String email;
    private String logoPath;
    private String lastUpdatedBy;
    private String lastUpdatedAt;

    // Constructors
    public ShopIdentity() {}

    public ShopIdentity(String pharmacyName, String address, String email, String logoPath) {
        this.pharmacyName = pharmacyName;
        this.address = address;
        this.email = email;
        this.logoPath = logoPath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(String lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }

    public String getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(String lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
