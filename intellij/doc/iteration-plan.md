# Iteration #2 Plan

In this iteration, we will focus on four key fronts:
1. Authentication: Users should be able to create accounts and log in into
   the app.
2. Implementing the Landing Page with categorized books for users to pick from.
3. Creating the Book Details Page for detailed book information.
4. Integrating the “Post Review” feature directly from the Book Details Page.

## 1. Authentication
### Objective
Users should be able to create and account and log in into the app.
Only users who have successfully logged in should have access to our app.

## 2. Landing Page

### Objective
• Provide users with an engaging entry point into the application.  
• Showcase a variety of book categories (e.g., “Best Sellers,”
“New Arrivals,” “Recommended Reads”).

### Functionality Overview
• Display multiple carousel-like sections or grids,
each representing a distinct book category.  
• Offer quick access to book details through clickable book items.  
• Allow users to navigate (e.g., via menu or links) to other parts
of the application.

---

## 3. Book Details Page

### Objective
• Show detailed information about a single book.  
• Enable users to read existing reviews and ratings.  
• Provide a structured place for the “Post Review” functionality.

### Functionality Overview
• Display book title, author, synopsis, average rating,
and existing reviews.  
• Provide easy navigation options to return to the Landing Page
or explore other books.  
• Integrate a prominent call-to-action to add a new review.

---

## 4. Post Review Use Case

### Objective
• Allow users to submit new book reviews, including text and rating.  
• Update the book’s overall rating and review list immediately.

### Functionality Overview
• Provide a form where users can enter the review
contents and rating (e.g., star rating and a written string review.).  
• Validate that the review has essential content
(e.g., non-empty text).  
• Save the review to the database and update the Book Details
Page in real time.
---


## Expected Results

By the end of this iteration, we expect the following
capabilities to be fully functional:  
• A visually appealing Landing Page that shows book categories.  
• A Book Details Page containing thorough
information and existing reviews.  
• A Post Review form that enables registered users
to post their reviews and see updates immediately.
