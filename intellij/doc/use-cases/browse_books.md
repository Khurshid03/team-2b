# Browse Books

## 1. Primary actor and goals
__Primary Actor__:

* __User__(Registered): Intends to view all the books available.

__Goals__:
* Allow users to seamlessly view all the books 
available in our system arranged in some kind of order/categories
* 
## 2. Preconditions
* User must be registered and authenticated (logged in).


## 3. Postconditions

The system ensures the following upon successful completion of this use case:
* The user's details are saved and can be used the next the user wants to
access the app.



## 4. Workflow
__Fully-dressed workflow diagram__:
```plantuml
@startuml
skin rose

title Display Home/Landing Page

|#application|User|
|#technology|System|


|User|
start
:Log in or create Account;
 |System|
:Display Landing/Home page(Inform of a welcome message);
:Display a list of book categories for users to pick from;
|User|
: Pick the category of books;

|System|
: Display the list of books available in that specific category;

|User|
:View Landing/Home page;

stop


@enduml
```

## Sequence Diagram

```plantuml
@startuml
skin rose
actor User
participant Main
participant UI
participant BrowseBooksFragment
participant Book

Main -> UI : create
Main -> BrowseBooksFragment : create\n(set UI listener)
UI -> BrowseBooksFragment : onLogin()

BrowseBooksFragment -> Book : getAvailableGenres()
BrowseBooksFragment -> Book : getTopRatedBooks()

Book --> BrowseBooksFragment : List of genres
Book --> BrowseBooksFragment : List of top-rated books

BrowseBooksFragment -> UI : showBrowseBooksPage()

@enduml

```

