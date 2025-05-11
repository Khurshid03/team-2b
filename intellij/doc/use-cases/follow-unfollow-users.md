## 1. Primary Actor and Goals
**Primary Actor**:
- **User**: Seeks to follow or unfollow other users to engage with their content or stop seeing updates from them.

**Goals**:
- Allow users to follow another user, adding the followed user to their following list.
- Allow users to unfollow previously followed users, removing them from their following list.
- Provide immediate feedback about the success or failure of follow/unfollow actions.
- Update the interface dynamically to reflect the follow/unfollow state.

## 2. Preconditions
- The user must be logged into the app.
- The user is on another user's profile or a page where the option to follow/unfollow is available (e.g., a search results page or followers list).

## 3. Postconditions
### **Successful Completion**:
1. If the user clicks **Follow**, they are added to the "follower" list of the target user, and the button/state updates to **Unfollow**.
2. If the user clicks **Unfollow**, they are removed from the "follower" list of the target user, and the button/state updates back to **Follow**.

### **Failure Scenarios**:
1. If the user attempts to follow themselves:
    - The action will not be performed, and an error message like _"You cannot follow yourself"_ is shown.

2. If there’s an issue with the server or action fails:
    - Display an error message like _"Unable to process request. Please try again."_
      
## 4. Workflow
```plantuml
@startuml

skin rose

title Follow/Unfollow Users Without Requests

|#application|User|
|#technology|System|

|User|
start
:Log into the app;
:Navigate to a user's profile;

|System|
:Display Follow/Unfollow button based on the current data (e.g., local follow status);

|User|
:Click on the Follow/Unfollow button;

|System|
if (Is Following?) then (yes)
    :Toggle state to Unfollow;
    :Update local state to remove this user from the "following" list;
else (no)
    :Toggle state to Follow;
    :Update local state to add this user to the "following" list;
endif

stop

@enduml
``````
## Sequence Diagram: Follow

```plantuml
@startuml
skin rose
actor User
participant ViewProfileFragment
participant ControllerActivity
participant FirestoreFacade

User -> ViewProfileFragment      : click Follow button
ViewProfileFragment -> ControllerActivity : follow(profileUsername, onSuccess, onError)
activate ControllerActivity

ControllerActivity -> FirestoreFacade : followUser(myId, profileUsername, onSuccess, onError)
activate FirestoreFacade

FirestoreFacade --> ControllerActivity : onSuccess()
deactivate FirestoreFacade

ControllerActivity --> ViewProfileFragment : Runnable.onSuccess()
deactivate ControllerActivity

ViewProfileFragment -> ViewProfileFragment : updateFollowButtonUI(true)
@enduml
```

## Sequence Diagram: Unfollow
````plantuml
@startuml
skin rose
actor User
participant ViewProfileFragment
participant ControllerActivity
participant FirestoreFacade

User -> ViewProfileFragment      : click Unfollow button
ViewProfileFragment -> ControllerActivity : unfollow(profileUsername, onSuccess, onError)
activate ControllerActivity

ControllerActivity -> FirestoreFacade : unfollowUser(myId, profileUsername, onSuccess, onError)
activate FirestoreFacade

FirestoreFacade --> ControllerActivity : onSuccess()
deactivate FirestoreFacade

ControllerActivity --> ViewProfileFragment : Runnable.onSuccess()
deactivate ControllerActivity

ViewProfileFragment -> ViewProfileFragment : updateFollowButtonUI(false)
@enduml
````