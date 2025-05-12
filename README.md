
# LitLore – Android Prototype

## Features Implemented

- **Login Screen**  
  • Accepts any non‑empty input (username or email) to authenticate a user.  
  • Simple UI with “Login” button and empty‑check validation.

- **Landing Page**  
  • Displays predefined book categories from GoogleBooksAPI (e.g., History, Science)  
  • Grid layout for categories

- **Search & Browse Books**  
  • Integrated with Google Books API in `SearchBooksFragment` & `BrowseBooksFragment`  
  • Users can search by title, author, or keyword  
  • Dynamic results list shows cover, title

- **Book Details Page**  
  • Fetches and displays metadata (title, author, description) from API  
  • Enables users to submit text reviews, add ratings by tapping star icons, save books to their saved books(wishlist) (users can also edit/delete their reviews)
  
- **Profile Page**  
  • Shows username, followers, following and posted reviews     
  • Enables users to view others' profiles and follow/unfollow them as well as read reviews

- **Saved Books Page**  
  • Stores the books user save for later view     
  • Enables users to easily access their saved books list 

- **Search Users Page**  
  • Returns LitLore users based on search query   
  • Enables users to search for others LitLore users and veiw their profile

---
## Limitations & Simplifying Assumptions


- **Static Categories List**  
  Category names are hard‑coded in the landing page. 
  However, **book data** comes live from the Google Books API.

- **Minimal Input Validation**  
  Only non‑empty checks on login and review text
  no content moderation or length enforcement.

- **Basic Error Handling**  
  Network errors during API calls show a generic “Failed to load” snackbar.

- **Login Credentials management**  
  There isn't any authentication of emails (it doesn't check if the emails actually exist) 


---
## How to Build & Run

### Prerequisites
- Android Studio
- Internet connection for API calls

### Steps
1. Clone the repo:
   git clone LINK (you can get the SSH key from the gitlab)

2. Run on emulator or device by clicking ▶️ in Android Studio or:

3. Test features: create an account/login, browse categories, search books, view details, and post reviews.



