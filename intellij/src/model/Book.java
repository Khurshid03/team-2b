package model;

import java.util.*;

/**
 * Represents a book with a title, author and genre.
 */
public class Book {
    private final int bookID;
    private final String title;
    private final String author;
    private final String genre;

    // Genre -> List of books

    private static final Map<String, List<Book>> booksByGenre = new HashMap<>();
    private static final List<Book> allBooks = new ArrayList<>();

    static {
        // Fiction
        addBook(new Book(1, "To Kill a Mockingbird", "Harper Lee", "Fiction"));
        addBook(new Book(2, "1984", "George Orwell", "Fiction"));
        addBook(new Book(3, "The Great Gatsby", "F. Scott Fitzgerald", "Fiction"));
        addBook(new Book(4, "Pride and Prejudice", "Jane Austen", "Fiction"));
        addBook(new Book(5, "The Catcher in the Rye", "J.D. Salinger", "Fiction"));
        addBook(new Book(6, "The Hobbit", "J.R.R. Tolkien", "Fiction"));
        addBook(new Book(7, "Jane Eyre", "Charlotte Brontë", "Fiction"));
        addBook(new Book(8, "Animal Farm", "George Orwell", "Fiction"));
        addBook(new Book(9, "Lord of the Flies", "William Golding", "Fiction"));
        addBook(new Book(10, "Wuthering Heights", "Emily Brontë", "Fiction"));

        // Science
        addBook(new Book(11, "A Brief History of Time", "Stephen Hawking", "Science"));
        addBook(new Book(12, "The Selfish Gene", "Richard Dawkins", "Science"));
        addBook(new Book(13, "Cosmos", "Carl Sagan", "Science"));
        addBook(new Book(14, "The Gene", "Siddhartha Mukherjee", "Science"));
        addBook(new Book(15, "The Origin of Species", "Charles Darwin", "Science"));
        addBook(new Book(16, "The Elegant Universe", "Brian Greene", "Science"));
        addBook(new Book(17, "Astrophysics for People in a Hurry", "Neil deGrasse Tyson", "Science"));
        addBook(new Book(18, "The Immortal Life of Henrietta Lacks", "Rebecca Skloot", "Science"));
        addBook(new Book(19, "Silent Spring", "Rachel Carson", "Science"));
        addBook(new Book(20, "The Structure of Scientific Revolutions", "Thomas S. Kuhn", "Science"));

        // Programming
        addBook(new Book(21, "Clean Code", "Robert C. Martin", "Programming"));
        addBook(new Book(22, "Effective Java", "Joshua Bloch", "Programming"));
        addBook(new Book(23, "The Pragmatic Programmer", "Andrew Hunt", "Programming"));
        addBook(new Book(24, "Design Patterns", "Erich Gamma et al.", "Programming"));
        addBook(new Book(25, "Head First Design Patterns", "Eric Freeman", "Programming"));
        addBook(new Book(26, "Code Complete", "Steve McConnell", "Programming"));
        addBook(new Book(27, "You Don't Know JS", "Kyle Simpson", "Programming"));
        addBook(new Book(28, "Refactoring", "Martin Fowler", "Programming"));
        addBook(new Book(29, "Introduction to Algorithms", "Cormen et al.", "Programming"));
        addBook(new Book(30, "Cracking the Coding Interview", "Gayle Laakmann McDowell", "Programming"));

        // History
        addBook(new Book(31, "Sapiens", "Yuval Noah Harari", "History"));
        addBook(new Book(32, "Guns, Germs, and Steel", "Jared Diamond", "History"));
        addBook(new Book(33, "The Silk Roads", "Peter Frankopan", "History"));
        addBook(new Book(34, "A People's History of the United States", "Howard Zinn", "History"));
        addBook(new Book(35, "The Wright Brothers", "David McCullough", "History"));
        addBook(new Book(36, "Team of Rivals", "Doris Kearns Goodwin", "History"));
        addBook(new Book(37, "The Rise and Fall of the Third Reich", "William L. Shirer", "History"));
        addBook(new Book(38, "Alexander Hamilton", "Ron Chernow", "History"));
        addBook(new Book(39, "1776", "David McCullough", "History"));
        addBook(new Book(40, "The History of the Ancient World", "Susan Wise Bauer", "History"));
    }

    private static void addBook(Book book) {
        allBooks.add(book);
        booksByGenre.computeIfAbsent(book.getGenre(), k -> new ArrayList<>()).add(book);
    }

    public Book(int bookID, String title, String author, String genre) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.genre = genre;
    }


    public int getBookID() { return bookID;}

    public String getTitle() { return title; }

    public String getAuthor() {return author;}

    public String getGenre() {return genre; }

    public static Set<String> getAvailableGenres() {
        return booksByGenre.keySet();
    }

    public static List<Book> getBooksByGenre(String genre) {
        return booksByGenre.getOrDefault(genre, Collections.emptyList());
    }



    public static Book getBookById(int bookID) {
        return allBooks.stream()
                .filter(book -> book.getBookID() == bookID)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "[" + bookID + "] "  + title + " by " + author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return bookID == book.bookID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookID);
    }

}
