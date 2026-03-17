# Event Flow Smoke Test (Scaffold)

This flow is implemented in the scaffold via Kafka listeners/publishers.

## Sequence

1. `auth-service` publishes `USER_AUTHENTICATED` to `user.lifecycle`.
2. `library-service` publishes `BOOK_UPLOADED` to `book.uploaded`.
3. `ingestion-service` consumes and publishes processing events to `book.processing` (`STARTED`, `METADATA_EXTRACTED`, `COMPLETED` or `FAILED`).
4. `reader-service` publishes `READER_PROGRESS_UPDATED` to `reader.progress`.
5. `activity-service` consumes progress and publishes activity to `reader.activity`.
6. `notification-service` consumes activity and publishes spoiler requests to `notification.lifecycle`.
7. `ai-service` consumes spoiler requests and publishes `NOTIFICATION_SPOILER_GENERATED`.
8. `notification-service` sends email and publishes `NOTIFICATION_EMAIL_SENT` or `NOTIFICATION_EMAIL_FAILED`.
9. `recommendation-service` consumes progress/activity + metadata extracted events for scoring and ranking.

## Sample calls

```bash
curl -X POST http://localhost:8081/api/auth/google/id-token \
  -H 'Content-Type: application/json' \
  -d '{"idToken":"<google-id-token>"}'

curl -X POST http://localhost:8082/api/library/books/upload-intent \
  -H 'Content-Type: application/json' \
  -d '{"userId":"user-1","bookId":"book-1","fileName":"book-1.pdf","contentType":"application/pdf"}'

curl -X POST http://localhost:8082/api/library/books/uploaded \
  -H 'Content-Type: application/json' \
  -d '{"bookId":"book-1","userId":"user-1","objectKey":"uploads/book-1.pdf","contentType":"application/pdf"}'

curl -X POST http://localhost:8084/api/reader/progress \
  -H 'Content-Type: application/json' \
  -d '{"userId":"user-1","bookId":"book-1","page":12,"progressPercent":17.5}'

curl http://localhost:8083/api/ingestion/books/book-1/status
```
