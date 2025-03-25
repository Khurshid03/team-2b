package view;

import model.*;

import java.io.PrintStream;
import java.util.*;

/**
 * Terminal-based implementation of the UI.
 */
public class CmdLineUI implements UI {

    private final Scanner scanner = new Scanner(System.in);
    private final PrintStream out = System.out;
    private UI.Listener listener;

    @Override
    public void setListener(UI.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        out.println("=== Welcome to the Book Review App ===");
        if (listener != null) listener.onStartReview();
    }

    public String promptUsername() {
        out.print("Enter your username: ");
        return scanner.nextLine();
    }

    public String promptEmail() {
        out.print("Enter your email: ");
        return scanner.nextLine();
    }

    public String promptGenre() {
        out.print("Choose a genre: ");
        return scanner.nextLine();
    }

    public int promptBookId() {
        out.print("Enter the ID of the book you'd like to review: ");
        return Integer.parseInt(scanner.nextLine());
    }

    public double promptRating() {
        out.print("Enter your rating (1.0–5.0): ");
        return Double.parseDouble(scanner.nextLine());
    }

    public String promptComment() {
        out.print("Enter your comment: ");
        return scanner.nextLine();
    }

    @Override
    public void showGenres(List<String> genres) {
        out.println("\nAvailable genres:");
        for (String genre : genres) {
            out.println("- " + genre);
        }
    }

    @Override
    public void showBooksInGenre(List<Book> books) {
        out.println("\nBooks in selected genre:");
        for (Book book : books) {
            out.println(book);
        }
    }

    @Override
    public void showSelectedBook(Book book) {
        out.println("\nYou selected: " + book);
    }

    @Override
    public void showMessage(String message) {
        out.println(message);
    }

    @Override
    public void showReviews(List<Review> reviews, String context) {
        out.println("\nReviews for " + context + ":");
        if (reviews.isEmpty()) {
            out.println("No reviews yet.");
        } else {
            for (Review r : reviews) {
                out.println("- " + r);
            }
        }
    }
}