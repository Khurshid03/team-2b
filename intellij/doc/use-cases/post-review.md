# Post Review

## 1. Primary actor and goals
__Primary Actor__:

* __User__(Registered): Intends to post a review for a book they have read.
* Wants an intuitive and fast review posting experience with real-time updates after submission.

__Goals__:
* Allow users to seamlessly post reviews for books within the app while ensuring the input is processed, validated, and displayed instantly.

## 2. Preconditions
* User must be registered and authenticated (logged in).
* The book for which the review is being posted exists in the app's database. 
* The app has an active internet connection to post the review to the server backend.


## 3. Postconditions

The system ensures the following upon successful completion of this use case:
* The review is saved in the database against the book and user profile. 
* The review becomes visible to other users in real-time. 
* Any associated rating with the review (e.g., a star rating) 
updates the book's average rating. 
* Optionally, trigger notifications to friends/followers of this activity.

__Failure Scenarios__:
* If the review cannot be saved due to network or server issues, 
the app must notify the user and prompt the to retry.



## 4. Workflow
__Fully-dressed workflow diagram__:


```plantuml
@startuml

skin rose

title Post Review - Fully Dressed

|#application|User|
|#technology|App Interface|
|#Implementation|Backend System|

|User|
start
:Log in to the app;
:Navigate to the book's page;
:Click on "Add Review" button;

|App Interface|
:Display review input form (text, ratings, optional tags);

|User|
:Fill in review details including star rating;
:Submit the review;

|App Interface|
:Capture review data;
:Validate review input (e.g. non-empty, rating within range);

|Backend System|
if (Valid Input?) then (yes)
  :Process and save review to database;
  :Save user-book rating;
  :Update book's overall rating;
  :Broadcast review update to other users (real time);
  :Respond with success message;
  
else (no)
  |App Interface|
  :Display error message to user;

endif

|App Interface|
:Refresh book page with the new review;
:Show "Review posted successfully" confirmation;


|User|
:View posted review in the book's review feed;
stop


@enduml
```

