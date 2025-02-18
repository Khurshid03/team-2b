# Comment-on-a-Review Use Case

---

## **1. Primary Actor and Goals**

### **Primary Actor**:
- **User**: Users who want to add comments to a review or 
manage (edit/delete) their own comments.

### **Goals**:
- Allow users to add a comment to a specific review.
- Allow users to delete their comments if they no longer want them displayed.
- Ensure all changes (add/edit/delete) are reflected in real-time.

---

## **2. Preconditions**

1. The user must be logged into the system (authenticated).
2. The user must have navigated to the **Book Detail Page** 
and selected a review to comment on.
3. The user must have permission to edit or delete 
their own comments on a review.

---

## **3. Postconditions**

### **Successful Completion**:
1. If the user adds a comment:
    - The comment is successfully added to the selected review.
    - The system persists the comment and reflects it 
in the review's comment section.
2. If the user edits a comment:
    - The comment is successfully updated with the new content.
    - The system persists the updated comment and reflects the 
changes on the interface.
3. If the user deletes a comment:
    - The comment is completely removed from the system and no 
longer displayed in the review's comment section.

### **Failure Scenarios**:
- If an error occurs while adding, editing, or deleting 
a comment:
    - Display an appropriate error message (e.g., 
"Unable to process the request. Please try again.") and 
retain the current state temporarily.

---

## **4. Workflow**

### 4.1 **Primary Workflow (Adding a Comment)**
```plantuml
@startuml

skin rose

title Add Comment Workflow

|#application|User|
|#technology|App Interface|

|User|
start
:Log in;
:navigate to the book detail page;

|App Interface|
:Display reviews with a "Comments" section;

|User|
:Enter the comment text in the comment box and submit;

|App Interface|
:Process and persist the comment(save comment to Database);
:Return success response with the new comment data;

|App Interface|
:Display the new comment in the review's comment section;

|User|

stop

@enduml
```

---

### 4.3 **Primary Workflow (Deleting a Comment)**
```plantuml
@startuml

skin rose

title Delete Comment Workflow

|#application|User|
|#technology|App Interface|


|User|
start
:Log in and navigate to the book detail page;

|App Interface|
:Display user's own comments with a "Delete" option;

|User|
:Click the "Delete" button for a specific comment;

|App Interface|
:Show a confirmation dialog (e.g., "Are you sure you want to delete this comment?");

|User|
if (Confirms deletion?) then (yes)
  
  |App Interface|
  :Remove the comment from the interface;
  :Show success message to the user;

else (no)
  :Cancel the deletion action;
endif 
stop



@enduml
```