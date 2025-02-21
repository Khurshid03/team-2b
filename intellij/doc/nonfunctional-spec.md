# Nonfunctional Requirements

## 1. Performance Requirements
- **Response Time**:
    - Search operations (for users and books) must 
return results within 2 seconds.
    - Actions such as saving a book to a wishlist, 
posting reviews, liking/unliking reviews, following/unfollowing 
users, and adding comments should update the interface 
in real time (preferably within 1 second).
- **Throughput**:
    - The system must handle a high volume of 
concurrent requests (e.g., multiple users posting reviews 
or interacting with content simultaneously) 
without degradation in performance.

**I am not sure how we are going to handle these but since this is a
social app, these are going to be very important 
in attracting and retaining our users**

## 2. Reliability and Availability
- **Uptime**:
    - The system should maintain an availability of at 
least 99.9% to ensure continuous user access.
- **Fault Tolerance**:
    - The system must gracefully handle failures 
(e.g., network outages, server errors) and provide 
clear error messages to the user.
  
## 3. Usability
- **User Interface**:
    - The UI must be intuitive and consistent 
across all functionalities (searching, posting reviews,
managing wishlist, liking reviews, following users, and commenting).
- **Accessibility**:
    - The application must comply with accessibility standards to ensure users with 
disabilities can effectively use the system.
- **Mobile Responsiveness**:
    - The system must be fully responsive, 
  ensuring a seamless experience across different mobile device sizes.

## 4. Security Requirements
- Only authenticated users can perform actions like
posting reviews, managing wishlists, and interacting with other users.
-All sensitive user information, including passwords must be encrypted. 

## 5. Scalability
- **Load Handling**:
    - The architecture must support horizontal and 
vertical scaling to accommodate increases in user numbers, 
content volume, and concurrent interactions.
- **Performance Optimization**:
    - Utilize caching, load balancing, and database 
indexing strategies to maintain high performance during peak usage times.

## 6. Maintainability
- **Modular Design**:
    - The system should be built in a modular manner, 
  making it easier to update or replace individual components
without affecting the entire system.
- **Documentation**:
    - Comprehensive documentation must be maintained for both
the codebase and the system architecture to facilitate 
future maintenance and enhancements.

## 7. Supportability
- Internationalization of displayed text (text, 
units, number and date formatting).

## 8. Implementation
- Software must be written using java
- Software must be able to run on android devices.


## Interfaces
- Since we are getting our book data from an external service,
our app must be able to correctly communicate with the service
and populate our app with whatever information we need about the books. 


This nonfunctional requirements document establishes the 
baseline quality attributes for the system, ensuring it delivers a 
fast, secure, and reliable user experience across all functionalities.
