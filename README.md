# Book Review CLI Prototype



## Features Implemented

- **Landing Page** with welcome message and genre display
- **User authentication** via input of username and email (assigned a unique user ID)
- **Genre selection** from a hardcoded list
- **Book selection** within a genre, based on book ID
- **Decimal or integer review ratings** (e.g., `4.5`, `3`)
- **Review creation**, tied to a user and a book


---

## Limitations and Simplifying Assumptions

- All books and genres are **hardcoded** inside the `Book` class (40 books across 4 genres).
- User data is **not persisted** — it's created anew each time the app runs.
- No authentication or user history tracking is implemented.
- No input validation beyond what's required to run the flow.

---

## How to Run the Prototype

#### 1. Navigate to the src folder:
```
cd intellij
cd src 
```

#### 2. Compile the code:
```
javac model/*.java view/*.java controller/*.java Main.java
```

#### 3. Run the Application:
```
java Main
```

#### Optionally, users can navigate to our Main class and run it directly using the intellij "run" icon

#### We decided to separate the main method from the other parts of the project and gave it its own class: Main.java

