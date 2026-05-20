# InternLink 2026 Fixes

Implemented fixes requested on May 19, 2026:

1. Notifications
   - Removed company-only local notification after posting.
   - Company post now creates notificationRequests for the jobs topic.
   - Student and Admin dashboards subscribe/listen for new job notifications.
   - Per-user in-app notification documents are created for Student and Admin users.
   - Android 13+ notification permission request added for Admin as well.

2. Student Dashboard Filters
   - Remote chip now filters remote internships only.
   - On-site chip now filters onsite/on-site internships only.
   - Paid chip filters paid/stipend/salary/compensation internships.
   - Search and chip filters now work together.

3. See All Button
   - See all now clears search and all active filters, then shows all active internships.

4. Company Dashboard Stats
   - Removed placeholder values 12 and 48.
   - Total Jobs, Active Jobs, and Applicants now load from real Firestore data.
   - Applicant count is calculated from application subcollections with cached fallback.

5. Year Update
   - Replaced Summer 2024 Tech Internships with Summer 2026 Tech Internships.

Note: True push delivery while the student/admin app is fully closed requires Firebase Cloud Messaging server-side delivery through Firebase Console or Cloud Functions. This project now creates notificationRequests and local listeners; configure Cloud Functions if you need guaranteed background/killed-state delivery automatically from Firestore.
