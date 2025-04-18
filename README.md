
# LitLore – Android Prototype

## Features Implemented

- **Login Screen**  
  • Accepts any non‑empty input (username or email) to authenticate a user  
  • Simple UI with “Login” button and empty‑check validation

- **Landing Page**  
  • Displays predefined book categories (e.g., History, Science)  
  • Grid layout for categories

- **Search & Browse Books**  
  • Integrated with Google Books API in `SearchBooksFragment` & `BrowseBooksFragment`  
  • Users can search by title, author, or keyword  
  • Dynamic results list shows cover, title

- **Book Details Page**  
  • Fetches and displays metadata (title, author, description) from API  
  • Enables users to submit text reviews and add ratings by tapping star icons
  

---
## Limitations & Simplifying Assumptions

- **No Persistence Across Restarts**  
  All user sessions and posted reviews live in memory; 
- quitting the app clears all data.

- **Static Categories List**  
  Category names are hard‑coded in the landing page. 
- However, **book data** comes live from the Google Books API.

- **Minimal Input Validation**  
  Only non‑empty checks on login and review text
- no content moderation or length enforcement.

- **Basic Error Handling**  
  Network errors during API calls show a generic “Failed to load” snackbar.

- **API Key Management**  
  You must provide your own Google Books API key in places 
- where it says `PUT_YOUR_API_KEY_HERE`

---
## How to Build & Run

### Prerequisites
- Android Studio
- Internet connection for API calls

### Steps
1. Clone the repo:
   git clone LINK (you can get the SSH key from the gitlab)


2. Add your Google Books API key:
   
   ### in BrowseBooksFragment and SearchBooksFragment
   PUT_YOUR_API_KEY_HERE


3. Run on emulator or device by clicking ▶️ in Android Studio or:

6. Test features: login, browse categories, search books, view details, and post reviews.



