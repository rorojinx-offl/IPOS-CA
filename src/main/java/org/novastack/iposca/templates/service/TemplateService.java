package org.novastack.iposca.templates.service;

import org.novastack.iposca.templates.database.DatabaseManager;
import org.novastack.iposca.templates.model.TemplateModel;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TemplateService {

    /**
     * Save or update a template
     */
    public void saveTemplate(TemplateModel t, String user) throws Exception {
        // 1. RBAC check
        RBACService.checkTemplateAccess(user);

        // 2. Validate template
        validateTemplate(t);

        // 3. Save to database
        try {
            DatabaseManager.saveTemplate(
                    t.type,
                    t.header,
                    t.body,
                    t.footer,
                    t.showLogo,
                    user
            );

            // 4. Audit log
            DatabaseManager.logAudit(
                    "TEMPLATE_SAVE",
                    "Saved template: " + t.type + " by " + user,
                    user
            );

        } catch (SQLException e) {
            throw new Exception("Database error while saving template: " + e.getMessage(), e);
        }
    }

    /**
     * Load a template by type
     */
    public TemplateModel loadTemplate(String type) throws Exception {
        ResultSet rs = null;
        try {
            rs = DatabaseManager.loadTemplate(type);

            if (rs.next()) {
                TemplateModel t = new TemplateModel();
                t.type = rs.getString("template_type");
                t.header = rs.getString("header_text");
                t.body = rs.getString("body_text");
                t.footer = rs.getString("footer_text");
                t.showLogo = rs.getInt("show_logo") == 1;
                return t;
            }
            return null;

        } catch (SQLException e) {
            throw new Exception("Database error while loading template: " + e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    // Also close the connection if needed
                    rs.getStatement().getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reset a template to its default value
     */
    public void resetToDefault(String type, String user) throws Exception {
        // 1. RBAC check
        RBACService.checkTemplateAccess(user);

        // 2. Get default template
        TemplateModel defaultTemplate = getDefaultTemplate(type);

        // 3. Save it
        saveTemplate(defaultTemplate, user);

        // 4. Audit log
        DatabaseManager.logAudit(
                "TEMPLATE_RESET",
                "Reset template to default: " + type + " by " + user,
                user
        );
    }

    /**
     * Get the default template for a given type
     */
    private TemplateModel getDefaultTemplate(String type) {
        TemplateModel t = new TemplateModel();
        t.type = type;

        switch (type) {
            case "RECEIPT":
                t.header = "INVOICE";
                t.body = "{PHARMACY_NAME}\n{PHARMACY_ADDRESS}\n{PHARMACY_EMAIL}\n\n" +
                        "--- ITEMS ---\n\n" +
                        "Thank you for your purchase!";
                t.footer = "";
                t.showLogo = true;
                break;

            case "REMINDER_1":
                t.body = "Dear {CUSTOMER_NAME},\n\n" +
                        "This is a reminder that your outstanding balance of {BALANCE} is due.\n" +
                        "Please make payment by the end of the month to avoid account suspension.\n\n" +
                        "Thank you,\n" +
                        "{PHARMACY_NAME}\n{PHARMACY_ADDRESS}\n{PHARMACY_EMAIL}";
                t.footer = "Payment Reminder";
                t.showLogo = false;
                break;

            case "REMINDER_2":
                t.body = "Dear {CUSTOMER_NAME},\n\n" +
                        "FINAL NOTICE: Your outstanding balance of {BALANCE} is now overdue.\n" +
                        "Your account has been SUSPENDED. No further credit purchases are allowed.\n" +
                        "Please contact us immediately to resolve this matter.\n\n" +
                        "{PHARMACY_NAME}\n{PHARMACY_ADDRESS}\n{PHARMACY_EMAIL}";
                t.footer = "URGENT: Account Suspended";
                t.showLogo = false;
                break;

            default:
                t.body = "Template content";
                t.footer = "";
                break;
        }

        return t;
    }

    /**
     * Validate template content
     */
    private void validateTemplate(TemplateModel t) throws Exception {
        // Check body is not empty
        if (t.body == null || t.body.trim().isEmpty()) {
            throw new Exception("Template body cannot be empty");
        }

        // For reminders, check required tokens
        if (t.type != null && t.type.contains("REMINDER")) {
            if (!t.body.contains("{CUSTOMER_NAME}")) {
                throw new Exception("Missing required token: {CUSTOMER_NAME}");
            }
            if (!t.body.contains("{BALANCE}")) {
                throw new Exception("Missing required token: {BALANCE}");
            }
        }

        // For receipt, check header is not empty
        if ("RECEIPT".equals(t.type)) {
            if (t.header == null || t.header.trim().isEmpty()) {
                throw new Exception("Receipt header cannot be empty");
            }
        }

        // Check for merchant placeholders (these are optional but good to warn about)
        if (!t.body.contains("{PHARMACY_NAME}")) {
            // Warning only, not error — will use default from shop identity
            System.out.println("Warning: Template missing {PHARMACY_NAME} placeholder");
        }
    }
}