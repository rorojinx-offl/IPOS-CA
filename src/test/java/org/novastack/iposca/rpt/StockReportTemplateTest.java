package org.novastack.iposca.rpt;

import net.sf.jasperreports.engine.JasperCompileManager;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StockReportTemplateTest {

    @Test
    void stockReportTemplateCompiles() throws Exception {
        compileReport("/jasper/rpt/stockReport.jrxml");
    }

    @Test
    void turnoverReportTemplateCompiles() throws Exception {
        compileReport("/jasper/rpt/turnoverReport.jrxml");
    }

    @Test
    void debtReportTemplateCompiles() throws Exception {
        compileReport("/jasper/rpt/debtReport.jrxml");
    }

    private void compileReport(String resourcePath) throws Exception {
        try (InputStream jrxml = getClass().getResourceAsStream(resourcePath)) {
            assertNotNull(jrxml, resourcePath + " should be available on the test classpath");
            JasperCompileManager.compileReport(jrxml);
        }
    }
}
