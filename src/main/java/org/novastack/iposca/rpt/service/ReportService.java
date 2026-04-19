package org.novastack.iposca.rpt.service;

import org.jooq.DSLContext;
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;
import org.novastack.iposca.rpt.model.DebtChangeData;
import org.novastack.iposca.rpt.model.DebtReportRow;
import org.novastack.iposca.rpt.model.StockItem;
import org.novastack.iposca.rpt.model.TurnoverData;
import org.novastack.iposca.rpt.model.TurnoverSale;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.utils.db.JooqConnection;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static schema.tables.Customer.CUSTOMER;
import static schema.tables.CustomerCharge.CUSTOMER_CHARGE;
import static schema.tables.CustomerRepayment.CUSTOMER_REPAYMENT;
import static schema.tables.Sale.SALE;

/**
 * Utility class that organises data required for generating reports.
 * */
public class ReportService {

    private final String currentUser;

    public ReportService(String currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Method that prepares data for generating a turnover report. First, we insert the date range and current user into
     * the {@link TurnoverData} bean. Then we fetch all sales from the database within the specified date range. Finally, we
     * calculate the total sales amount and total orders placed value.
     * @param startDate The start date of the report period.
     * @param endDate The end date of the report period.
     * @return A {@link TurnoverData} bean containing the required data for generating a turnover report.
     * */
    public TurnoverData getTurnoverData(LocalDate startDate, LocalDate endDate) throws SQLException{
        TurnoverData data = new TurnoverData();
        data.setReportPeriodStart(startDate);
        data.setReportPeriodEnd(endDate);
        data.setGeneratedBy(currentUser);
        data.setGeneratedTimestamp(LocalDate.now());

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        DSLContext ctx = JooqConnection.getDSLContext();

        List<TurnoverSale> sales = new ArrayList<>();
        ctx.selectFrom(SALE)
                .where(SALE.SALE_DATE_TIME.ge(startDateTime.toString()))
                .and(SALE.SALE_DATE_TIME.lt(endDateTime.toString()))
                .orderBy(SALE.SALE_DATE_TIME.asc())
                .fetch()
                .forEach(record -> sales.add(new TurnoverSale(
                        record.getId(),
                        record.getPaymentMethod(),
                        record.getSaleDateTime(),
                        record.getAmount()
                )));

        float totalSalesAmount = (float) sales.stream()
                .mapToDouble(TurnoverSale::getAmount)
                .sum();

        data.setTotalSalesCount(sales.size());
        data.setTotalSalesAmount(totalSalesAmount);
        data.setTotalOrdersPlacedValue(0f);
        data.setSales(sales);

        logReport("TURNOVER_REPORT", "Period: " + startDate + " to " + endDate);
        return data;
    }

    /**
     * Method that prepares data for generating a stock report. First, we fetch all stock items from the database. Then,
     * we filter the items based on the provided stock filter and search text. Finally, we calculate the retail price,
     * total stock value, and VAT amount for each item and encapsulate them in a {@link StockItem} bean.
     * @param stockStatus The stock filter to apply to the report. Can be "ALL", "LOW_STOCK", or "OUT_OF_STOCK".
     * @param searchText The search text to filter the items by.
     * @return A list of {@link StockItem} beans containing the required data for generating a stock report.
     * */
    public List<StockItem> getStockData(String stockStatus, String searchText) throws SQLException {
        String status = stockStatus == null ? "ALL" : stockStatus.trim().toUpperCase(Locale.ROOT);
        String search = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        float vatRate = getVatRate();

        List<StockItem> reportRows = new ArrayList<>();
        for (Stock stock : Stock.getAllStock()) {
            if (!matchesStockStatus(stock, status) || !matchesSearch(stock, search)) {
                continue;
            }

            float retailPrice = calculateRetailPrice(stock.getBulkCost(), stock.getMarkupRate());
            float totalStockValue = retailPrice * stock.getQuantity();
            float vatAmount = totalStockValue * (vatRate / 100f);

            StockItem item = new StockItem(
                    stock.getId(),
                    stock.getName(),
                    stock.getBulkCost(),
                    stock.getQuantity(),
                    stock.getStockLimit()
            );
            item.setLowStock(stock.getQuantity() <= stock.getStockLimit());
            item.setRetailPrice(retailPrice);
            item.setVatRate(vatRate);
            item.setVatAmount(vatAmount);
            item.setTotalStockValue(totalStockValue);

            reportRows.add(item);
        }

        logReport("STOCK_REPORT", "Status: " + status + ", Search: " + search);
        return reportRows;
    }

    /**
     * Method that prepares data for generating a debt report. First, we insert the date range and current user into
     * the {@link DebtChangeData} bean. Then we fetch all purchases and repayments from the database within the specified
     * date range. Then we establish a map of customer IDs to their debt balances before the end of the date range. Finally,
     * we perform calculations to calculate the aggregate debt, new debt accrued, and closing aggregate debt.
     * @param startDate The start date of the report period.
     * @param endDate The end date of the report period.
     * @return A {@link DebtChangeData} bean containing the required data for generating a debt report.
     * */
    public DebtChangeData getDebtChangeData(LocalDate startDate, LocalDate endDate) throws SQLException {
        DebtChangeData data = new DebtChangeData();
        data.setStartDate(startDate);
        data.setEndDate(endDate);
        data.setGeneratedBy(currentUser);
        data.setGeneratedTimestamp(LocalDate.now());

        DSLContext ctx = JooqConnection.getDSLContext();
        String start = startDate.toString();
        String endExclusive = endDate.plusDays(1).toString();

        List<Float> charges = ctx.select(CUSTOMER_CHARGE.AMOUNT)
                .from(CUSTOMER_CHARGE)
                .where(CUSTOMER_CHARGE.DATE.ge(start))
                .and(CUSTOMER_CHARGE.DATE.lt(endExclusive))
                .fetch(CUSTOMER_CHARGE.AMOUNT);

        List<Float> repayments = ctx.select(CUSTOMER_REPAYMENT.AMOUNT)
                .from(CUSTOMER_REPAYMENT)
                .where(CUSTOMER_REPAYMENT.DATE.ge(start))
                .and(CUSTOMER_REPAYMENT.DATE.lt(endExclusive))
                .fetch(CUSTOMER_REPAYMENT.AMOUNT);

        Map<Integer, Float> closingBalances = getDebtBalancesBefore(ctx, endExclusive);

        float newDebtAccrued = sum(charges);
        float paymentsReceived = sum(repayments);
        float closingAggregateDebt = sumPositiveBalances(closingBalances);
        float openingAggregateDebt = closingAggregateDebt - newDebtAccrued + paymentsReceived;

        data.setOpeningAggregateDebt(openingAggregateDebt);
        data.setPaymentsReceived(paymentsReceived);
        data.setNewDebtAccrued(newDebtAccrued);
        data.setClosingAggregateDebt(closingAggregateDebt);
        data.setTotalDebtorsCount((int) closingBalances.values().stream()
                .filter(balance -> balance > 0f)
                .count());
        data.setTotalPaymentsCount(repayments.size());
        data.setTotalCreditSalesCount(charges.size());
        data.setPdfRows(buildDebtReportRows(ctx, start, endExclusive, closingBalances));

        logReport("DEBT_REPORT", "Period: " + startDate + " to " + endDate);
        return data;
    }

    /**
     * Test method to check if the data has been correctly prepared for the report.
     * @param reportType The type of report to generate.
     * @param details Additional details about the report generation.
     * */
    private void logReport(String reportType, String details) {
        System.out.println("AUDIT: " + reportType + " generated by " + currentUser + " - " + details);
    }

    /**
     * Reads stock data and filters it based on the provided stock status.
     * @param stock The stock item to check.
     * @param status The status to filter by.
     * @return True if the stock item matches the status, false otherwise.
     * */
    private boolean matchesStockStatus(Stock stock, String status) {
        return switch (status) {
            case "LOW_STOCK" -> stock.getQuantity() > 0 && stock.getQuantity() <= stock.getStockLimit();
            case "OUT_OF_STOCK" -> stock.getQuantity() <= 0;
            default -> true;
        };
    }

    /**
     * Reads stock data and filters it based on the provided search text.
     * @param stock The stock item to check.
     * @param search The search text to filter by.
     * @return True if the stock item matches the search text, false otherwise.
     * */
    private boolean matchesSearch(Stock stock, String search) {
        if (search.isEmpty()) {
            return true;
        }

        return String.valueOf(stock.getId()).contains(search)
                || stock.getName().toLowerCase(Locale.ROOT).contains(search);
    }

    /**
     * Calculates the retail price of a stock item based on its bulk cost and markup rate.
     * @param bulkCost The bulk cost of the stock item.
     * @param markupRate The markup rate (%) of the stock item.
     * @return The retail price of the stock item.
     * */
    private float calculateRetailPrice(float bulkCost, int markupRate) {
        return bulkCost * (1 + (markupRate / 100f));
    }

    /**
     * Sums a list of floats, ignoring null values.
     * @param values The {@link List} of {@link Float} to sum.
     * @return The sum of the floats in the list, ignoring null values.
     * */
    private float sum(List<Float> values) {
        return (float) values.stream()
                .mapToDouble(value -> value == null ? 0f : value)
                .sum();
    }

    /**
     * Builds the rows for the debt report. First, it adds the required rows and then fetches customer purchase data from
     * the database using SQL joins. It then formats the received data. Then it populates the {@link DebtReportRow} bean
     * to be used in the report.
     * @param ctx The {@link DSLContext} to use for database operations.
     * @param start The start date of the report period.
     * @param endExclusive The end date of the report period, exclusive.
     * @param closingBalances A map of customer IDs to their debt balances before the end of the date range.
     * @return A list of {@link DebtReportRow} beans containing the data for the debt report.
     * */
    private List<DebtReportRow> buildDebtReportRows(DSLContext ctx, String start, String endExclusive, Map<Integer, Float> closingBalances) {
        List<DebtReportRow> rows = new ArrayList<>();

        rows.add(new DebtReportRow("Credit Sales", "", "", "", ""));
        rows.add(new DebtReportRow("Charge ID", "Customer ID", "Customer", "Date", "Amount"));
        int creditSalesRows = rows.size();
        ctx.select(CUSTOMER_CHARGE.CRG_ID, CUSTOMER_CHARGE.CUST_ID, CUSTOMER.NAME, CUSTOMER_CHARGE.DATE, CUSTOMER_CHARGE.AMOUNT)
                .from(CUSTOMER_CHARGE)
                .join(CUSTOMER)
                .on(CUSTOMER_CHARGE.CUST_ID.eq(CUSTOMER.ID))
                .where(CUSTOMER_CHARGE.DATE.ge(start))
                .and(CUSTOMER_CHARGE.DATE.lt(endExclusive))
                .orderBy(CUSTOMER_CHARGE.DATE.asc(), CUSTOMER_CHARGE.CRG_ID.asc())
                .fetch()
                .forEach(record -> rows.add(new DebtReportRow(
                        String.valueOf(record.get(CUSTOMER_CHARGE.CRG_ID)),
                        String.valueOf(record.get(CUSTOMER_CHARGE.CUST_ID)),
                        record.get(CUSTOMER.NAME),
                        record.get(CUSTOMER_CHARGE.DATE),
                        String.format("£%.2f", record.get(CUSTOMER_CHARGE.AMOUNT))
                )));
        if (rows.size() == creditSalesRows) {
            rows.add(new DebtReportRow("No credit sales in selected period", "", "", "", ""));
        }

        rows.add(new DebtReportRow("", "", "", "", ""));
        rows.add(new DebtReportRow("Debtors at Period End", "", "", "", ""));
        rows.add(new DebtReportRow("Customer ID", "Customer", "Status", "Credit Limit", "Debt Balance"));
        int debtorRows = rows.size();
        closingBalances.entrySet().stream()
                .filter(entry -> entry.getValue() > 0f)
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    var customerRecord = ctx.select(CUSTOMER.NAME, CUSTOMER.STATUS, CUSTOMER.CREDITLIMIT)
                            .from(CUSTOMER)
                            .where(CUSTOMER.ID.eq(entry.getKey()))
                            .fetchOne();
                    if (customerRecord == null) {
                        return;
                    }

                    rows.add(new DebtReportRow(
                            String.valueOf(entry.getKey()),
                            customerRecord.get(CUSTOMER.NAME),
                            customerRecord.get(CUSTOMER.STATUS),
                            String.format("£%.2f", customerRecord.get(CUSTOMER.CREDITLIMIT)),
                            String.format("£%.2f", entry.getValue())
                    ));
                });
        if (rows.size() == debtorRows) {
            rows.add(new DebtReportRow("No debtors at period end", "", "", "", ""));
        }

        return rows;
    }

    /**
     * Gets the debt balances before a given date. It uses the customer charges and repayments to calculate the debt
     * difference between the end of the date range and the date.
     * @param ctx The {@link DSLContext} to use for database operations.
     * @param endExclusive The end date of the date range, exclusive.
     * @return A map of customer IDs to their debt balances before the end of the date range.
     * */
    private Map<Integer, Float> getDebtBalancesBefore(DSLContext ctx, String endExclusive) {
        Map<Integer, Float> balances = new HashMap<>();

        ctx.select(CUSTOMER_CHARGE.CUST_ID, CUSTOMER_CHARGE.AMOUNT)
                .from(CUSTOMER_CHARGE)
                .where(CUSTOMER_CHARGE.DATE.lt(endExclusive))
                .fetch()
                .forEach(record -> balances.merge(
                        record.get(CUSTOMER_CHARGE.CUST_ID),
                        record.get(CUSTOMER_CHARGE.AMOUNT),
                        Float::sum
                ));

        ctx.select(CUSTOMER_REPAYMENT.CUST_ID, CUSTOMER_REPAYMENT.AMOUNT)
                .from(CUSTOMER_REPAYMENT)
                .where(CUSTOMER_REPAYMENT.DATE.lt(endExclusive))
                .fetch()
                .forEach(record -> balances.merge(
                        record.get(CUSTOMER_REPAYMENT.CUST_ID),
                        -record.get(CUSTOMER_REPAYMENT.AMOUNT),
                        Float::sum
                ));

        return balances;
    }

    private float sumPositiveBalances(Map<Integer, Float> balances) {
        return (float) balances.values().stream()
                .mapToDouble(balance -> Math.max(0f, balance))
                .sum();
    }

    private float getVatRate() {
        try {
            byte[] vat = AppConfig.get(AppConfig.ConfigKey.VAT);
            return vat == null ? 0f : AppConfigAPI.decodeByteToInt(vat);
        } catch (IllegalArgumentException e) {
            return 0f;
        }
    }
}
