package org.novastack.iposca.rpt.factory;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.novastack.iposca.rpt.model.DebtChangeData;
import org.novastack.iposca.rpt.model.StockItem;
import org.novastack.iposca.rpt.model.TurnoverData;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportFactory {

    private static final String REPORT_DIR = "generated-reports";

}
