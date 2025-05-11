# Search For Users

## 1. Primary Actor and Goals

**Primary Actor**:
- **User**: Seeks to locate specific users within the system to see others' profile

**Goals**:
- Search for users based on username.
- Return a list of users matching the search parameters.
- View and interact with additional details of a user, such as their profile.

## 2. Preconditions
- The user must be logged into the system.


## 3. Postconditions
### **Successful Completion**:
1. The user is presented with an accurate and relevant list of users matching the search criteria.
2. The user can navigate to specific user details for further view (viewing their profile)

### **Failure Scenarios**:
- If no matching users exist:
    - Display a message like "No users found"



## 4. Workflow
```plantuml
@startuml

skin rose

title Search for Books

|#application|User|
|#technology|System|

|User|
start
:Log in to the app;
:Navigate to search page or access search bar;


repeat
|System|
:Display Search Bar;
|User|
:Enter search criteria (username, name);

|System|
  :Send user query to backend;
:Query database for matching results;
backward: No users found;
repeat while (Results Found?) is (no)
->yes;
  :Display search results;
  
 |User|
  :Select a user from the results;

|System|
:Navigate to user profile;
stop


@enduml
``````

## Sequence Diagram 

```plantuml
@startuml
skin rose

actor User
participant MainUI
participant SearchUsersFragment
participant ControllerActivity
participant FirestoreFacade
participant SearchUsersAdapter

'--- Navigation to Search Users ---
User -> MainUI                        : tap “Search Users” nav item
MainUI -> SearchUsersFragment         : displayFragment(SearchUsersFragment)

'--- Enter Query & Execute Search ---
User -> SearchUsersFragment           : enter query\nclick Go
SearchUsersFragment -> ControllerActivity : searchUsers(query, this)
activate ControllerActivity

ControllerActivity -> FirestoreFacade : searchUsers(query, listener)
activate FirestoreFacade

FirestoreFacade --> ControllerActivity : onResults(List<User>)
deactivate FirestoreFacade

ControllerActivity --> SearchUsersFragment : displaySearchResults(users)
deactivate ControllerActivity
activate SearchUsersFragment

SearchUsersFragment -> SearchUsersAdapter : setData(users)
activate SearchUsersAdapter
SearchUsersAdapter --> SearchUsersFragment : notifyDataSetChanged()
deactivate SearchUsersAdapter

SearchUsersFragment --> User           : show updated list
deactivate SearchUsersFragment
@enduml
```