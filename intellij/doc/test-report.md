# Test Report

We only covered manual system testing.

#### Execution Command:
```
  javac model/*.java view/*.java controller/*.java Main.java
  java Main
```

## Use Case: Authenticate

#### Feature tested: 
User registration via email and username

#### Steps:

- Enter Felix as username
- Enter felix@gmail.com as email.

Username and email can be anything as long as it's a string. 
we don't check whether the email is valid, so any string would work. 

#### Expected Results:
```
=== Welcome to the Book Review App ===

Enter your username: Felix
Enter your email: felix@gmail.com
HELLO, Felix!
```

Even though we initially initialized a userID, we did not use it at this time.
We will use it later on when we have real users.

## Use Case: View Landing Page

#### feature tested:
Display the landing page when the application starts after the user is 
authenticated. 

Landing page in this case means a list of genres for users to pick from. 

#### Expected Results:
```
Available genres:
- Fiction
- Science
- Programming
- History
```

## Use Case: Post View

### Scenario 1 : Correct Genre, bookID, and rating

#### Steps:

genre: Science </br>
bookID: 12 </br>
rating: 2.5 </br>
comment: Great book! I enjoyed reading it but it felt a bit boring!


#### Expected Results:
```
Choose a genre: Science

Great choice there Felix!

Books in selected genre:
[11] A Brief History of Time by Stephen Hawking
[12] The Selfish Gene by Richard Dawkins
[13] Cosmos by Carl Sagan
[14] The Gene by Siddhartha Mukherjee
[15] The Origin of Species by Charles Darwin
[16] The Elegant Universe by Brian Greene
[17] Astrophysics for People in a Hurry by Neil deGrasse Tyson
[18] The Immortal Life of Henrietta Lacks by Rebecca Skloot
[19] Silent Spring by Rachel Carson
[20] The Structure of Scientific Revolutions by Thomas S. Kuhn
Enter the ID of the book you'd like to review: 12

You selected: [12] The Selfish Gene by Richard Dawkins
Enter your rating (1.0–5.0): 2.5
Enter your comment: Great book! I enjoyed reading it but it felt a bit boring!

✅ Review submitted successfully!

Reviews for The Selfish Gene:
- "Great book! I enjoyed reading it but it felt a bit boring!" — Felix (2.5/5, 2025-03-26 14:29)
```


### Scenario 2 :  Invalid Genre → user is expected to retry until they get the correct genre

#### Steps:

genre: Mystery (which is not in our hardcoded list)

#### Expected Results:

```
Available genres:
- Programming
- Science
- History
- Fiction
Choose a genre: Mystery
❌ Genre not found or no books available. Please try again.
Choose a genre: 
```

### Scenario 3: Invalid bookID - user is given the chance to retry until they give a correct ID
#### Steps:
 bookID: 99

#### Expected Results:
```
Enter the ID of the book you'd like to review: 99
❌ Book ID not found. Please try again.
Enter the ID of the book you'd like to review: 
```

### Scenario 4: Invalid rating

The star rating should be either a double or an integer between 1.0-5.0. 

If a user gives something that is not in this range, they should be prompted to retry. 

Similarly, if they give a string, they should be prompted to retry. 

#### Steps:
rating: 7


#### Expected Results:
```
Enter your rating (1.0–5.0): 7
❌ Rating must be between 1.0 and 5.0. Please try again.
Enter your rating (1.0–5.0): 
```

#### Steps:
rating: Two

#### Expected Results
```
Enter your rating (1.0–5.0): Two
❌ Invalid input. Please enter a numeric rating (e.g., 4.5).
Enter your rating (1.0–5.0): 
```

We tried to cover most of the fail scenarios and allow the users to continue writing 
their review even after giving a wrong input instead of restarting the whole process 
afresh. 








