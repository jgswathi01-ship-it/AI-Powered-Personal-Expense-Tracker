package swathiproject1;

import java.sql.*;

public class ExpenseManager {

    // 🔹 Add Expense
    public void addExpense(String description, double amount, String date) {
        String category = detectCategory(description);
        String sql = "INSERT INTO expenses (description, amount, category, expense_date) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, description);
            ps.setDouble(2, amount);
            ps.setString(3, category);
            ps.setString(4, date);
            ps.executeUpdate();
            System.out.println("✅ Expense added under category: " + category);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Detect Category using AI-logic (simple keywords)
    public String detectCategory(String description) {
        description = description.toLowerCase();
        if (description.contains("food") || description.contains("restaurant") || description.contains("pizza"))
            return "Food";
        else if (description.contains("bus") || description.contains("train") || description.contains("fuel") || description.contains("uber") || description.contains("ola"))
            return "Travel";
        else if (description.contains("bill") || description.contains("electricity") || description.contains("water") || description.contains("recharge") || description.contains("rent"))
            return "Bills";
        else if (description.contains("shirt") || description.contains("shoe") || description.contains("mall") || description.contains("dress"))
            return "Shopping";
        else if (description.contains("medicine") || description.contains("hospital") || description.contains("clinic"))
            return "Health";
        else
            return "Others";
    }

    // 🔹 Show All Expenses
    public void viewExpenses() {
        String sql = "SELECT * FROM expenses ORDER BY expense_date DESC, id DESC";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📋 All Expenses:");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.println(rs.getInt("id") + ". " +
                        rs.getString("description") + " | ₹" +
                        rs.getDouble("amount") + " | " +
                        rs.getString("category") + " | " +
                        rs.getDate("expense_date"));
            }
            if (!any) System.out.println("(no expenses yet)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Monthly Report (category totals)
    public void showMonthlyReport(String month) {
        String sql = "SELECT category, SUM(amount) AS total " +
                     "FROM expenses " +
                     "WHERE DATE_FORMAT(expense_date, '%Y-%m') = ? " +
                     "GROUP BY category " +
                     "ORDER BY total DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, month); // e.g. "2025-08"
            ResultSet rs = ps.executeQuery();

            System.out.println("\n📊 Monthly Report for " + month);
            double grand = getMonthTotal(con, month);
            if (grand == 0) {
                System.out.println("(no expenses found for this month)");
                return;
            }
            while (rs.next()) {
                String cat = rs.getString("category");
                double total = rs.getDouble("total");
                double pct = (total / grand) * 100.0;
                System.out.printf("%-10s -> ₹%.2f (%.2f%%)\n", cat, total, pct);
            }
            System.out.printf("Total Spent -> ₹%.2f\n", grand);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== NEW: Savings Feature =====================

    // 🔹 Set or Update Monthly Income
    public void setMonthlyIncome(String month, double income) {
        String upsert = "INSERT INTO monthly_income (month_key, income) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE income = VALUES(income)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(upsert)) {
            ps.setString(1, month);
            ps.setDouble(2, income);
            ps.executeUpdate();
            System.out.println("💰 Monthly income for " + month + " set to ₹" + income);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Show Savings Insights for a month
    public void showSavingsInsights(String month) {
        try (Connection con = DBConnection.getConnection()) {
            Double income = getMonthlyIncome(con, month);
            double total = getMonthTotal(con, month);

            System.out.println("\n🧠 Savings Insights for " + month);
            if (income == null) {
                System.out.println("⚠️ No income set for this month. Please set it first.");
                return;
            }
            System.out.printf("Income: ₹%.2f\n", income);
            System.out.printf("Total Spent: ₹%.2f\n", total);

            double savings = income - total;
            double spendPct = income > 0 ? (total / income) * 100.0 : 0.0;

            if (savings >= 0) {
                System.out.printf("Savings: ₹%.2f (You spent %.2f%% of income)\n", savings, spendPct);
            } else {
                System.out.printf("Overspent by: ₹%.2f (You spent %.2f%% of income)\n", Math.abs(savings), spendPct);
            }

            // Category-wise top spenders
            printTopCategories(con, month, 3);

            // Simple tips based on spend pattern
            if (spendPct > 80) {
                System.out.println("Tip: 🔻 Spending is above 80% of income. Try reducing non-essential categories next month.");
            } else if (spendPct < 50) {
                System.out.println("Nice! ✅ You saved more than 50% of your income this month.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- Helpers ----------

    private Double getMonthlyIncome(Connection con, String month) throws SQLException {
        String q = "SELECT income FROM monthly_income WHERE month_key = ?";
        try (PreparedStatement ps = con.prepareStatement(q)) {
            ps.setString(1, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("income");
            return null;
        }
    }

    private double getMonthTotal(Connection con, String month) throws SQLException {
        String q = "SELECT COALESCE(SUM(amount), 0) AS total " +
                   "FROM expenses WHERE DATE_FORMAT(expense_date, '%Y-%m') = ?";
        try (PreparedStatement ps = con.prepareStatement(q)) {
            ps.setString(1, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("total");
            return 0.0;
        }
    }

    private void printTopCategories(Connection con, String month, int limit) throws SQLException {
        String q = "SELECT category, SUM(amount) AS total " +
                   "FROM expenses " +
                   "WHERE DATE_FORMAT(expense_date, '%Y-%m') = ? " +
                   "GROUP BY category " +
                   "ORDER BY total DESC " +
                   "LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(q)) {
            ps.setString(1, month);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n🏷️ Top Categories:");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("- %s: ₹%.2f\n", rs.getString("category"), rs.getDouble("total"));
            }
            if (!any) System.out.println("(no expenses for this month)");
        }
    }
}

