# Display Landing Page

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
|#technology|App Interface|


|User|
start
:Log in or create Account;
 |App Interface|
:Display Landing/Home page(Inform of a welcome message);
:Display a list of book categories for users to pick from;
|User|
: Pick the category of books;

|App Interface|
: Display the list of books available in that specific category;

|User|
:View Landing/Home page;

stop


@enduml
```

## Sequence Diagram
```plantuml
actor User as user
participant "UI" as UI
participant "Database" as database


UI -> user : Display login/signup page
user -> UI : user logs in/creates an account
UI -> database :authenticate user
database -> UI : displayBooks()
UI -> user : viewAllBooks()

```

