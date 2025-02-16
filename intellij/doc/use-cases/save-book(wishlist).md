## 1. Primary Actor and Goals
**Primary Actor**:
- **User**: Seeks to save or remove books from their wishlist for later review, rating.

**Goals**:
- Allow users to save books to their wishlist with a single action (e.g., clicking a button).
- Allow users to remove books from their wishlist using the same toggle mechanism.
- Provide immediate feedback about the success of the action.
- Update the interface dynamically to reflect whether a book is in the wishlist.

## 2. Preconditions
- The user must be logged into the application.
- The user is browsing books (e.g., on a book detail page, search-for-books bar)

## 3. Postconditions
### **Successful Completion**:
1. If the user clicks **Save to Wishlist**, the book is added to their "wishlist" and the button text/icon updates to **Remove from Wishlist**.
2. If the user clicks **Remove from Wishlist**, the book is removed from their "wishlist," and the button text/icon updates back to **Save to Wishlist**.

### **Failure Scenarios**:
1. If there’s an issue (e.g., local storage is full) during the save/remove action:
    - Display an error message like _"Unable to update wishlist. Please try again."_

## 4. Workflow
The following workflow illustrates the process:
``` plantuml
@startuml

skin rose

title Save/Remove Book from Wishlist with State Awareness

|#application|User|
|#technology|App Interface|
|#Implementation|Back-End System|


|User|
start
:Log into the app;
:View a book's details (fetched dynamically via Google Books API);

|App Interface|
:Query back end to check if the book is already in the wishlist;

|Back-End System|
:Check if the user has saved this book;
if (Book is in Wishlist?) then (yes)
  :Return "in wishlist" state;
else (no)
  :Return "not in wishlist" state;
endif

|App Interface|
if (Book is in Wishlist?) then (yes)
  :Display "Remove from Wishlist" button;
else (no)
  :Display "Save to Wishlist" button;
endif

|User|
:Click on "Save to Wishlist" or "Remove from Wishlist" button;

|App Interface|
if (Action is Save?) then (yes)
  :Send user_id and book details to back end to add book;
else (no)
  :Send user_id and book_id to back end to remove book;
endif

|Back-End System|
if (Action is Save?) then (yes)
  :Add book details to wishlist in database;
else (no)
  :Remove book details from wishlist in database;
endif
:Return success or error response;

|App Interface|
if (Response is success?) then (yes)
else (no)
:Repeat the save process;
endif  
stop
```
