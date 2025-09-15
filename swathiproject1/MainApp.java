package swathiproject1;

import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ExpenseManager manager = new ExpenseManager();

        while (true) {
            System.out.println("\n===== AI Expense Tracker =====");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Monthly Report");
            System.out.println("4. Set Monthly Income");
            System.out.println("5. Savings Insights");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");
            int choice = safeInt(sc);

            switch (choice) {
                case 1:
                    System.out.print("Enter description: ");
                    String desc = sc.nextLine();
                    System.out.print("Enter amount: ");
                    double amt = safeDouble(sc);
                    System.out.print("Enter date (YYYY-MM-DD): ");
                    String date = sc.nextLine();
                    manager.addExpense(desc, amt, date);
                    break;

                case 2:
                    manager.viewExpenses();
                    break;

                case 3:
                    System.out.print("Enter month (YYYY-MM): ");
                    String monthR = sc.nextLine();
                    manager.showMonthlyReport(monthR);
                    break;

                case 4:
                    System.out.print("Enter month (YYYY-MM): ");
                    String monthI = sc.nextLine();
                    System.out.print("Enter income for " + monthI + ": ");
                    double income = safeDouble(sc);
                    manager.setMonthlyIncome(monthI, income);
                    break;

                case 5:
                    System.out.print("Enter month (YYYY-MM): ");
                    String monthS = sc.nextLine();
                    manager.showSavingsInsights(monthS);
                    break;

                case 6:
                    System.out.println("👋 Exiting... Bye!");
                    System.exit(0);

                default:
                    System.out.println("❌ Invalid choice, try again.");
            }
        }
    }

    private static int safeInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            sc.next();
        }
        int val = sc.nextInt();
        sc.nextLine(); // consume newline
        return val;
    }

    private static double safeDouble(Scanner sc) {
        while (!sc.hasNextDouble()) {
            System.out.print("Please enter a valid amount: ");
            sc.next();
        }
        double val = sc.nextDouble();
        sc.nextLine(); // consume newline
        return val;
    }
}
