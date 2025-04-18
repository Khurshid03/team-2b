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
participant Main
participant CmdLineUI
participant BookViewController
participant BookRepository
participant Book

Main -> CmdLineUI : create
Main -> BookViewController : create\n(set CmdLineUI listener)
CmdLineUI -> BookViewController : onBookClicked(bookId)

BookViewController -> BookRepository : getBookById(bookId)
BookRepository -> BookViewController : return Book

BookViewController -> CmdLineUI : displayBookDetail(Book)
User -> CmdLineUI : viewDisplayedDetails()

@enduml
```
