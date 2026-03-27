package org.novastack.iposca.templates.service;

import org.novastack.iposca.templates.database.DatabaseManager;
import org.novastack.iposca.templates.model.ShopIdentity;
import org.novastack.iposca.utils.ui.IValid;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopIdentityService {

    /**
     * Load shop identity from database
     */
    public ShopIdentity loadIdentity() throws Exception {
        ResultSet rs = null;
        try {
            rs = DatabaseManager.loadShopIdentity();

            if (rs.next()) {
                ShopIdentity identity = new ShopIdentity();
                identity.setId(rs.getInt("id"));
                identity.setPharmacyName(rs.getString("pharmacy_name"));
                identity.setAddress(rs.getString("address"));
                identity.setEmail(rs.getString("email"));
                identity.setLogoPath(rs.getString("logo_path"));
                identity.setLastUpdatedBy(rs.getString("last_updated_by"));
                identity.setLastUpdatedAt(rs.getString("last_updated_at"));
                return identity;
            }
            return null;

        } catch (SQLException e) {
            throw new Exception("Database error while loading shop identity: " + e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs.getStatement().getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Save shop identity to database
     */
    public void saveIdentity(ShopIdentity identity, String user) throws Exception {
        // RBAC check
        RBACService.checkTemplateAccess(user);

        // Validate
        if (identity.getPharmacyName() == null || identity.getPharmacyName().trim().isEmpty()) {
            throw new Exception("Pharmacy name is required");
        }
        if (identity.getAddress() == null || identity.getAddress().trim().isEmpty()) {
            throw new Exception("Address is required");
        }
        if (!IValid.checkEmail(identity.getEmail())) {
            throw new Exception("Valid email is required (e.g., name@domain.com)");
        }

        // Save to database
        try {
            DatabaseManager.saveShopIdentity(
                    identity.getPharmacyName(),
                    identity.getAddress(),
                    identity.getEmail(),
                    identity.getLogoPath(),
                    user
            );

            // Audit log
            DatabaseManager.logAudit(
                    "SHOP_IDENTITY_SAVE",
                    "Shop identity updated by " + user,
                    user
            );

        } catch (SQLException e) {
            throw new Exception("Database error while saving shop identity: " + e.getMessage(), e);
        }
    }
}