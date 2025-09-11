# Digital Paper Approval System (Prototype)

## Module: Paper Submission

This project is a prototype of a Digital Paper Approval System that allows students to submit papers online for approval. The approval process moves step by step from Tutor → HOD → Principal. 
The project is designed in Java using simple core classes.


### Core Classes
1. **User**
   - Base class for all types of users.
   - Stores details such as username, password, and role.
   - Provides login functionality.

2. **Student**
   - Inherits from `User`.
   - Can create and submit a document for approval.
   - Can view the status of their submitted paper.

3. **Document**
   - Represents a student’s paper.
   - Stores document ID, title, content, and approval status.
   - Status changes as it moves through Tutor, HOD, and Principal.

4. **Main**
   - Entry point of the program.
   - Provides a simple text-based user interface.
   - Handles login and directs users to their role-based actions.


### Workflow (Paper Submission Module)
1. **Login** – User enters credentials.
2. **Student Actions**
   - Create and submit a document.
   - Wait for approval.
3. **Tutor Actions**
   - Review the student’s document.
   - Approve or reject.
4. **HOD Actions**
   - Review documents approved by Tutor.
   - Approve or reject.
5. **Principal Actions**
   - Review documents approved by HOD.
   - Give final approval or rejection.
6. **Status Tracking**
   - Each document has a status: `Submitted → Approved by Tutor → Approved by HOD → Approved by Principal → Final Decision`.


### Features Implemented
- Multiple user roles (Student, Tutor, HOD, Principal).
- Document submission by students.
- Step-by-step approval workflow.
- Status updates for each paper.


### Example Scenario
- **Student** logs in → submits a document.  
- **Tutor** logs in → reviews → approves.  
- **HOD** logs in → reviews → approves.  
- **Principal** logs in → gives final approval.  
- **Student** can check status → sees "Approved by Principal".  


### Project Status
- Core classes created  
- Paper submission flow works  
- ⚡ Next: Persistent storage & GUI  
