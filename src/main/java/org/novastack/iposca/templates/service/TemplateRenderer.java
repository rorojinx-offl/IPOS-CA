package org.novastack.iposca.templates.service;

import org.novastack.iposca.templates.model.TemplateModel;

public class TemplateRenderer {

    public static String preview(TemplateModel template, String context) {
        // Simple preview with sample data
        String result = template.body;

        // Replace merchant placeholders with sample values
        result = result.replace("{PHARMACY_NAME}", "HealthPlus Pharmacy");
        result = result.replace("{PHARMACY_ADDRESS}", "123 Main Street, London");
        result = result.replace("{PHARMACY_EMAIL}", "info@healthplus.com");

        if ("RECEIPT".equals(template.type)) {
            result = template.header + "\n\n" + result + "\n\n" + template.footer;
            if (template.showLogo) {
                result = "[LOGO]\n" + result;
            }
        } else {
            // Reminder templates
            result = result.replace("{CUSTOMER_NAME}", "John Smith");
            result = result.replace("{BALANCE}", "£120.50");
            result = result + "\n\n" + template.footer;
        }

        return result;
    }
}