package org.novastack.iposca.templates.service;

public class RBACService {

    public static void checkTemplateAccess(String role) throws Exception {
        if (role == null) {
            throw new Exception("Access denied: No role provided");
        }

        String upperRole = role.toUpperCase();
        if (upperRole.equals("PHARMACIST")) {
            throw new Exception("Access denied: Pharmacists cannot edit templates");
        }

        if (!upperRole.equals("ADMIN") && !upperRole.equals("MANAGER")) {
            throw new Exception("Access denied: Invalid role");
        }
    }

    public static boolean hasTemplateAccess(String role) {
        try {
            checkTemplateAccess(role);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}