import controller.ReviewController;

/**
 * Entry point of the Book Review application.
 */
public class Main {
    public static void main(String[] args) {
        ReviewController controller = new ReviewController();
        controller.runReviewFlow();
    }
}
