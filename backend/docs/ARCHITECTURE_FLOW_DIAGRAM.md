# Humanize Architecture Flow Diagram

```mermaid
flowchart LR
  U[Web/Mobile Client]
  G[API Gateway]

  subgraph Services
    A[auth-service]
    L[library-service]
    I[ingestion-service]
    R[reader-service]
    AC[activity-service]
    RE[recommendation-service]
    N[notification-service]
    AI[ai-service]
  end

  subgraph Kafka["Kafka Topics (Aiven)"]
    T1[user.lifecycle]
    T2[book.uploaded]
    T3[book.processing]
    T4[reader.progress]
    T5[reader.activity]
    T6[notification.lifecycle]
  end

  subgraph Data
    P[(PostgreSQL\nschema-per-service)]
    O[(Cloudflare R2)]
  end

  subgraph External
    GO[Google OIDC]
    EM[Email Provider]
    LLM[LLM Provider / Ollama]
  end

  U --> G
  G --> A
  G --> L
  G --> R
  G --> RE
  G --> N

  A <-- verify --> GO
  A --> T1

  L --> O
  L --> P
  L --> T2

  I --> O
  I --> P
  T2 --> I
  I --> T3

  R --> P
  R --> T4

  T4 --> AC
  AC --> P
  AC --> T5

  T4 --> RE
  T5 --> RE
  T3 --> RE
  RE --> P

  T5 --> N
  N --> P
  N --> T6
  T6 --> AI
  AI --> T6
  AI <-- generate --> LLM
  T6 --> N
  N --> EM
```

## Event Path (Spoiler Notification)

`reader-service -> reader.progress -> activity-service -> reader.activity -> notification-service -> notification.lifecycle(SPOILER_REQUESTED) -> ai-service -> notification.lifecycle(SPOILER_GENERATED) -> notification-service -> email provider`

