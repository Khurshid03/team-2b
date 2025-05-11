# View Book

## 1. Primary actor and goals

**Primary Actor**:
- **User**: Seeks to see book details to learn more about the book they are looking for

**Goals**:
- See/read book's details (e.g., description, genre, author).


## **2. Preconditions**
- The user must be a registered and authenticated (logged in).
- The user must have looked up and accessed a specific book they want to review

## **3. Postconditions**
### **Successful Completion**:
1. The user is presented with the details (description) of the book

### **Failure Scenarios**:
- No such scenarios since users just view the book details that are already stored in the system

## **4. Workflow**
```plantuml
@startuml

skin rose

title View Book

|#application|User|
|#technology|System|


|User|
start
:Click on a chosen book;

|System|
:Display the book detail page;

|User|  
:View book details/Rate and/or post review;
stop 

@enduml
``````

# Sequence Diagram 

```plantuml

@startuml
skin rose

actor User
participant BrowseBooksFragment
participant ControllerActivity
participant MainUI
participant ViewBookFragment
participant FirestoreFacade

'--- User selects a book to view ---
User -> BrowseBooksFragment          : tap on a book item
BrowseBooksFragment -> ControllerActivity : onBookSelected(book)
activate ControllerActivity

ControllerActivity -> ViewBookFragment : newInstance(book)
ControllerActivity -> ViewBookFragment : setListener(this)
ControllerActivity -> MainUI             : displayFragment(viewBookFragment)
deactivate ControllerActivity

MainUI -> ViewBookFragment             : fragment transaction (replace container)
activate ViewBookFragment

'--- Fragment lifecycle and UI setup ---
ViewBookFragment -> ViewBookFragment   : onCreateView(...)
ViewBookFragment -> ViewBookFragment   : onViewCreated(...)
ViewBookFragment -> ViewBookFragment   : updateBookDetails(book)

'--- Load reviews ---
ViewBookFragment -> ControllerActivity : fetchReviews(book, this)
activate ControllerActivity
ControllerActivity -> FirestoreFacade  : fetchReviewsForBook(book, listener)
activate FirestoreFacade
FirestoreFacade --> ControllerActivity  : onFetched(List<Review>)
deactivate FirestoreFacade
ControllerActivity --> ViewBookFragment : displayReviews(reviews)
deactivate ControllerActivity

'--- Check saved state ---
ViewBookFragment -> ControllerActivity : isBookSaved(book, this)
activate ControllerActivity
ControllerActivity -> FirestoreFacade  : isBookSaved(uid, book, callback)
activate FirestoreFacade
FirestoreFacade --> ControllerActivity  : callback(isSaved)
deactivate FirestoreFacade
ControllerActivity --> ViewBookFragment : onBookSaveState(isSaved)
deactivate ControllerActivity

ViewBookFragment -> ViewBookFragment   : update UI (reviews & save button)
@enduml
```
