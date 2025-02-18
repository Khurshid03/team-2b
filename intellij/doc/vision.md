
# LitLore( A Social book reviewing System) - Vision document

## 1. Introduction
 A social app where users can rate and review books that they have read. Follow their friends and like their friend's reviews 
and comments. 

## 2. Business case
Our Social book reviewing app addresses customer needs that other products do not:
1. Social - It allows more social activities between users something which is not common among major book reviewing platforms.
         In our platform, users will be able to follow their friends , like their friends' reviews and comment on them.
2. Authenticity - Users tend to relate more to reviews written by their friends rather than by random people. Since we are 
         going to introduce a following system, users will be able to see their friend's reviews and will most probably be confident
         with the reviews from our platform.
3. Users will be able to easily keep track of their reviews. All of their reviews will be posted to their profile.

## 3. Key functionality
Users:
- Search for books
- post review 
- Edit the review (including a delete review option)
- Like a review
- search for other users/friends
- See friends' profile
- Follow each other
- manage friends
- User stats (number of likes and reviews). This should be in their profile.


- save books(wishlist)

## 4. Stakeholder goals summary
- **User**: Write a review, edit the review/delete the review, like a comment, follow other users, 
            see other people comments and likes, search for other users and books, look at friends' profiles 
- **Administrator**: manage users/user activities


## Use case diagram

```plantuml
skin rose

' human actors
actor "User" as user
actor "Administrator" as admin

' system actors


' list all use cases in package
usecase "Manage Users" as manageUsers
 
package LitLore{
    usecase "CreateAccount" as createAccount
    usecase "Authenticate" as authenticate
    usecase "View Landing Page" as viewLandingPage
    usecase "View Profile" as viewProfile
    usecase "Search Users" as searchUsers
    usecase "Search Books" as searchBooks
    usecase "View Book" as viewBook
    usecase "write Review" as writeReview
    usecase "Manage Reviews" as manageReviews
    usecase "Comment on a Review" as commentOnAReview
    usecase "Save Book" as saveBook
    usecase "Manage Friends" as manageFriends
    usecase " Manage Account" as manageAccount
    usecase "Manage Saved Books" as manageSavedBooks
    usecase "viewUserProfile" as viewUserProfile
    usecase "Follow User" as followUser
   
}

' list relationships between actors and use cases

user --> createAccount
user --> authenticate
authenticate --> viewLandingPage : <<includes>>
viewLandingPage -left-> viewProfile
viewProfile <|-- manageAccount : <<extends>>
viewProfile <|-- manageSavedBooks : <<extends>>
viewProfile <|-- manageFriends : <<extends>>
searchUsers --> viewUserProfile
viewUserProfile <|-- followUser : <<extends>>
viewLandingPage -right-> searchBooks
viewLandingPage --> searchUsers
searchBooks --> viewBook
viewBook <|-right- saveBook : <<extends>>
viewBook --> writeReview
writeReview <|-- manageReviews : <<extends>>
writeReview <|-right- commentOnAReview : <<extends>>
manageReviews <|-- viewUserProfile : <<extends>>

admin --> manageUsers
```