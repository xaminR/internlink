# InternLink Completion Notes

This version focuses on stabilizing the Java/XML Android project and completing the proposal-level flows that can be implemented inside the app code.

## Implemented / Improved
- Light mode is forced globally through `BaseActivity`.
- Google login and forgot password UI remain removed.
- Student profile supports proposal fields: university, semester, courses, programming languages, phone, address, hobbies, education, projects, LinkedIn and GitHub.
- CV generation flow is connected to profile save.
- Company verification form supports company name, email, website, phone, address, registration status, years of operation and NTN/registration number.
- Companies must be admin-approved before posting internships.
- Bronze package is 2 posts/month; Gold package is 10 posts/month.
- Bronze internships expire after 7 days; Gold internships expire after 14 days.
- Expired internships are marked `EXPIRED` and blocked from student applications.
- Search now filters title, company, skills, location, duration, type and semester eligibility.
- Eligibility checks now compare student semester and skills before applying.
- Duplicate applications are blocked before creating a new application.
- Applicant status updates create in-app notification records.
- Interview email uses Android email intent.
- FCM notification channel, topic subscription, token storage and notification click handling are included.
- New internship posting creates a `notificationRequests` Firestore document for server-side FCM delivery.
- Android 13+ notification permission request is included for the student dashboard.
- Back button handling in toolbar is fixed in `BaseActivity`.

## Important Firebase Requirement
Android clients cannot safely send push notifications directly to all users because the Firebase server key must not be stored in the app. For production startup use, deploy a Firebase Cloud Function that listens to:

`notificationRequests/{requestId}`

Then send FCM to topic `jobs` and update request status from `PENDING_SERVER_DELIVERY` to `SENT`.

Suggested payload fields already written by the app:
- topic
- title
- body
- internshipId
- createdAt
- status

## Recommended Firestore Collections
- `users/{uid}`
- `users/{uid}/profile/info`
- `users/{uid}/applications/{internshipId}`
- `users/{uid}/notifications/{notificationId}`
- `companies/{uid}`
- `internships/{internshipId}`
- `internships/{internshipId}/applications/{uid}`
- `notificationRequests/{requestId}`

## Build Note
Gradle build verification could not be completed in this sandbox because the Gradle wrapper attempted to download Gradle from `services.gradle.org`, and internet access is unavailable here. Open the project in Android Studio with internet enabled and run Gradle Sync + Build.
