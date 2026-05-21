# DriveCoach
App - An application for managing driving instructors and students

<img width="280" alt="App Screenshot" src="https://github.com/user-attachments/assets/68f13956-cea8-4d96-9afe-5d708729f787" />

 This is a dedicated mobile application (Android Native) built to organize and smartly manage the entire world of driving lessons – both from the driving instructor's side and the student's side.
 
## About the App and System Operations
The app is divided into two main areas depending on the type of user logging in:

1. **Instructor Side:**
  * The instructor can view their daily lesson schedule.
  * The instructor sets up and manages their active student list (name, phone, city, email, and ID).
  * The instructor defines the school's "set of rules and quotas" (the minimum number of lessons required for each task, such as parking, highway driving, etc.) and can update them dynamically.

 <img width="280" alt="App Screenshot" src="https://github.com/user-attachments/assets/381c8c6e-6b65-4b81-9113-5319bf32f354" />

2. **Student Side:**
  * The student logs in and gets a home screen with a personalized welcome title.
  * The student can see their clean schedule (dates and times of lessons scheduled for them).
  * The student tracks the progress of their tasks (how many lessons they completed out of the quota defined by the instructor) to know when they are ready for a test.
  * Student password reset can only be performed by the instructor.

 <img width="280" alt="App Screenshot" src="https://github.com/user-attachments/assets/b14cbc8f-4b99-4446-86c7-8704b6f1809b" />

---
 
## File Distribution and Architecture (Separation of Concerns)
 
There is a separation between the User Interface (UI) layer and the data and logic layer (Business Logic). The files in the project are divided into defined packages:
 
* **`activity`:** The UI folder. Contains all the app's Activities (screens) (such as `LoginActivity`, `StudentDashboardActivity`, `StudentProfileActivity`). Their responsibility is strictly to display components on the screen, respond to clicks, and inject text.
* **`manager`:** The business logic folder. This is where the app's "brain" is located (such as `StudentManager`, `LessonManager`). These classes are the only ones that communicate with the cloud database (**Cloud Firestore**) and execute queries.
* **`model`:** The data entities folder. Contains pure mapping classes of type `data class` (such as `Student`, `Lesson`, `LessonRule`). These classes hold the structure of the objects in memory and include default values to allow Firebase to convert data from the cloud into code automatically and stably.
* **`adapter`:** List adapters. Contain the `RecyclerView.Adapter` classes responsible for taking static data lists from memory and translating them into dynamic visual cards displayed to the user on the screen (for example, displaying the lesson list or quota rules).
 
---
 
## 🛠️ Key Points and Special Features in the Project

### 1. Creating an external library (Module) for variable validation named **`validation_helper`**.
Inside this library, we created the `ValidationUtils` class, which contains static functions to check variables and inputs:
* Validating the correctness of an Israeli ID (ensuring it consists of exactly 9 digits).
* Checking the validity of a mobile phone number structure.
* Validating a proper email address format.
 
### 2. Student management, archiving, and deletion by the instructor
The instructor has full control and absolute security over the list of registered students under them in the system:
* **Full deletion option:** In case a student decides to stop learning, moves to another instructor, or for any other reason – the instructor has the option to completely delete the student from the system. The app does not delete information on its own; **the instructor is the sole manager and they are the one who decides when to permanently delete a record** from the cloud database, without it moving to the archive if it is no longer needed.
 
---
 
## ⚙️ Technologies and Libraries in the Project
* **Firebase Authentication:** For managing secure login and session saving for instructors, including an option to reset the password in English for students if necessary.
* **Shared Preferences:** Permanent local memory on the device to save the student's session, which prevents them from logging out automatically when the app is closed or moves to the background, until they actively click the logout button (Logout).
* **Cloud Firestore:** A real-time managed NoSQL cloud database that links students and instructors and manages the lessons table (`Lessons`).

  ***** The system stores a "Snapshot" of quota requirements directly within the student's profile upon registration, ensuring that subsequent instructor updates only apply to new students and preserve the original learning terms for existing ones.
