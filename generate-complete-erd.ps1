# Generate Complete ERD for All Databases
# This script creates a comprehensive ERD diagram using Mermaid

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "        Generate Complete ERD for All Databases                 " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

$outputFile = "database-docs\COMPLETE-SYSTEM-ERD.md"

# Ensure output directory exists
if (-not (Test-Path "database-docs")) {
    New-Item -ItemType Directory -Path "database-docs" | Out-Null
}

# Create comprehensive ERD content
$erdContent = @"
# ABC Interview System - Complete Database ERD

Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

## System Overview

This document contains the Entity Relationship Diagrams for all microservices in the ABC Interview System.

## Microservices Architecture

The system consists of 7 microservices, each with its own database:

1. **Auth Service** (authdb) - Authentication and authorization
2. **User Service** (userdb) - User management and profiles
3. **Question Service** (questiondb) - Question bank management
4. **Exam Service** (examdb) - Exam creation and management
5. **Career Service** (careerdb) - Career and job management
6. **News Service** (newsdb) - News and articles
7. **Social Service** (socialdb) - Social features and interactions

---

## 1. Question Service Database (questiondb)

### ERD Diagram

``````mermaid
erDiagram
    FIELDS {
        bigint id PK
        varchar name
        text description
    }
    
    TOPICS {
        bigint id PK
        varchar name
        text description
        bigint field_id FK
    }
    
    LEVELS {
        bigint id PK
        varchar name
        text description
        int min_score
        int max_score
    }
    
    QUESTION_TYPES {
        bigint id PK
        varchar name
        text description
    }
    
    QUESTIONS {
        bigint id PK
        bigint user_id
        bigint topic_id FK
        bigint field_id FK
        bigint level_id FK
        bigint question_type_id FK
        text question_content
        text question_answer
        double similarity_score
        varchar status
        varchar language
        timestamp created_at
        timestamp approved_at
        bigint approved_by
        int useful_vote
        int unuseful_vote
    }
    
    ANSWERS {
        bigint id PK
        bigint user_id
        bigint question_id FK
        bigint question_type_id FK
        text content
        boolean is_correct
        boolean is_sample_answer
        int order_number
        int useful_vote
        int unuseful_vote
        timestamp created_at
    }
    
    FIELDS ||--o{ TOPICS : "has"
    FIELDS ||--o{ QUESTIONS : "categorizes"
    TOPICS ||--o{ QUESTIONS : "contains"
    LEVELS ||--o{ QUESTIONS : "defines_difficulty"
    QUESTION_TYPES ||--o{ QUESTIONS : "defines_type"
    QUESTION_TYPES ||--o{ ANSWERS : "defines_type"
    QUESTIONS ||--o{ ANSWERS : "has"
``````

### Tables Description

- **FIELDS**: Main categories (Frontend, Backend, Database, etc.)
- **TOPICS**: Specific topics within fields (ReactJS, Spring Boot, etc.)
- **LEVELS**: Difficulty levels (Intern, Junior, Senior, etc.)
- **QUESTION_TYPES**: Types of questions (Single Choice, Multiple Choice, Fill in the Blank)
- **QUESTIONS**: The actual interview questions
- **ANSWERS**: Answer options for questions

---

## 2. User Service Database (userdb)

### ERD Diagram

``````mermaid
erDiagram
    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password
        varchar full_name
        varchar phone
        date date_of_birth
        varchar gender
        varchar avatar_url
        varchar status
        timestamp created_at
        timestamp updated_at
    }
    
    ROLES {
        bigint id PK
        varchar name UK
        text description
    }
    
    USER_ROLES {
        bigint user_id FK
        bigint role_id FK
    }
    
    ELO_RANKS {
        bigint id PK
        bigint user_id FK UK
        int elo_score
        varchar rank_name
        timestamp last_updated
    }
    
    USER_PROFILES {
        bigint id PK
        bigint user_id FK UK
        text bio
        varchar location
        varchar website
        varchar linkedin
        varchar github
        text skills
        text experience
    }
    
    USERS ||--o{ USER_ROLES : "has"
    ROLES ||--o{ USER_ROLES : "assigned_to"
    USERS ||--o| ELO_RANKS : "has"
    USERS ||--o| USER_PROFILES : "has"
``````

### Tables Description

- **USERS**: Core user information
- **ROLES**: User roles (Admin, User, Moderator, etc.)
- **USER_ROLES**: Many-to-many relationship between users and roles
- **ELO_RANKS**: User ranking based on ELO system
- **USER_PROFILES**: Extended user profile information

---

## 3. Exam Service Database (examdb)

### ERD Diagram

``````mermaid
erDiagram
    EXAMS {
        bigint id PK
        varchar title
        text description
        bigint creator_id
        int duration_minutes
        int total_questions
        varchar difficulty_level
        varchar status
        timestamp start_time
        timestamp end_time
        timestamp created_at
    }
    
    EXAM_QUESTIONS {
        bigint id PK
        bigint exam_id FK
        bigint question_id FK
        int order_number
        int points
    }
    
    EXAM_REGISTRATIONS {
        bigint id PK
        bigint exam_id FK
        bigint user_id FK
        varchar status
        timestamp registered_at
        timestamp started_at
        timestamp submitted_at
    }
    
    EXAM_ANSWERS {
        bigint id PK
        bigint registration_id FK
        bigint question_id FK
        bigint answer_id FK
        text answer_text
        boolean is_correct
        int points_earned
        timestamp answered_at
    }
    
    EXAM_RESULTS {
        bigint id PK
        bigint registration_id FK UK
        int total_score
        int max_score
        double percentage
        varchar grade
        int correct_answers
        int wrong_answers
        int unanswered
        timestamp graded_at
    }
    
    EXAMS ||--o{ EXAM_QUESTIONS : "contains"
    EXAMS ||--o{ EXAM_REGISTRATIONS : "has"
    EXAM_REGISTRATIONS ||--o{ EXAM_ANSWERS : "has"
    EXAM_REGISTRATIONS ||--o| EXAM_RESULTS : "produces"
``````

### Tables Description

- **EXAMS**: Exam definitions
- **EXAM_QUESTIONS**: Questions included in exams
- **EXAM_REGISTRATIONS**: User registrations for exams
- **EXAM_ANSWERS**: User answers to exam questions
- **EXAM_RESULTS**: Final exam results and scores

---

## 4. Social Service Database (socialdb)

### ERD Diagram

``````mermaid
erDiagram
    POSTS {
        bigint id PK
        bigint user_id
        varchar post_type
        varchar title
        text content
        varchar status
        int view_count
        int comment_count
        int upvote_count
        int downvote_count
        double elo_weight
        timestamp created_at
        timestamp updated_at
    }
    
    COMMENTS {
        bigint id PK
        bigint post_id FK
        bigint user_id
        bigint parent_comment_id FK
        text content
        int upvote_count
        int downvote_count
        timestamp created_at
        timestamp updated_at
    }
    
    VOTES {
        bigint id PK
        bigint user_id
        bigint post_id FK
        bigint comment_id FK
        varchar vote_type
        double weight
        timestamp created_at
    }
    
    POST_TAGS {
        bigint id PK
        bigint post_id FK
        varchar tag_name
    }
    
    POSTS ||--o{ COMMENTS : "has"
    POSTS ||--o{ VOTES : "receives"
    POSTS ||--o{ POST_TAGS : "tagged_with"
    COMMENTS ||--o{ COMMENTS : "replies_to"
    COMMENTS ||--o{ VOTES : "receives"
``````

### Tables Description

- **POSTS**: User posts (questions, discussions, articles)
- **COMMENTS**: Comments on posts
- **VOTES**: Upvotes/downvotes on posts and comments
- **POST_TAGS**: Tags for categorizing posts

---

## 5. News Service Database (newsdb)

### ERD Diagram

``````mermaid
erDiagram
    NEWS {
        bigint id PK
        varchar title
        text content
        text summary
        varchar author
        varchar category
        varchar status
        varchar image_url
        int view_count
        timestamp published_at
        timestamp created_at
        timestamp updated_at
    }
    
    NEWS_CATEGORIES {
        bigint id PK
        varchar name UK
        text description
        int display_order
    }
    
    NEWS_TAGS {
        bigint id PK
        bigint news_id FK
        varchar tag_name
    }
    
    NEWS_COMMENTS {
        bigint id PK
        bigint news_id FK
        bigint user_id
        text content
        timestamp created_at
    }
    
    NEWS }o--|| NEWS_CATEGORIES : "belongs_to"
    NEWS ||--o{ NEWS_TAGS : "tagged_with"
    NEWS ||--o{ NEWS_COMMENTS : "has"
``````

### Tables Description

- **NEWS**: News articles
- **NEWS_CATEGORIES**: News categories
- **NEWS_TAGS**: Tags for news articles
- **NEWS_COMMENTS**: Comments on news articles

---

## 6. Career Service Database (careerdb)

### ERD Diagram

``````mermaid
erDiagram
    JOBS {
        bigint id PK
        varchar title
        text description
        varchar company_name
        varchar location
        varchar job_type
        varchar experience_level
        varchar salary_range
        varchar status
        timestamp posted_at
        timestamp expires_at
        timestamp created_at
    }
    
    JOB_APPLICATIONS {
        bigint id PK
        bigint job_id FK
        bigint user_id
        text cover_letter
        varchar resume_url
        varchar status
        timestamp applied_at
        timestamp reviewed_at
    }
    
    JOB_SKILLS {
        bigint id PK
        bigint job_id FK
        varchar skill_name
        varchar proficiency_level
    }
    
    COMPANIES {
        bigint id PK
        varchar name UK
        text description
        varchar website
        varchar logo_url
        varchar industry
        int employee_count
    }
    
    JOBS }o--|| COMPANIES : "posted_by"
    JOBS ||--o{ JOB_APPLICATIONS : "receives"
    JOBS ||--o{ JOB_SKILLS : "requires"
``````

### Tables Description

- **JOBS**: Job postings
- **JOB_APPLICATIONS**: User applications for jobs
- **JOB_SKILLS**: Required skills for jobs
- **COMPANIES**: Company information

---

## 7. Auth Service Database (authdb)

### ERD Diagram

``````mermaid
erDiagram
    AUTH_USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        boolean email_verified
        varchar status
        timestamp last_login
        timestamp created_at
    }
    
    REFRESH_TOKENS {
        bigint id PK
        bigint user_id FK
        varchar token UK
        timestamp expires_at
        boolean is_revoked
        timestamp created_at
    }
    
    PASSWORD_RESET_TOKENS {
        bigint id PK
        bigint user_id FK
        varchar token UK
        timestamp expires_at
        boolean is_used
        timestamp created_at
    }
    
    LOGIN_HISTORY {
        bigint id PK
        bigint user_id FK
        varchar ip_address
        varchar user_agent
        boolean success
        timestamp login_at
    }
    
    AUTH_USERS ||--o{ REFRESH_TOKENS : "has"
    AUTH_USERS ||--o{ PASSWORD_RESET_TOKENS : "requests"
    AUTH_USERS ||--o{ LOGIN_HISTORY : "has"
``````

### Tables Description

- **AUTH_USERS**: Authentication user data
- **REFRESH_TOKENS**: JWT refresh tokens
- **PASSWORD_RESET_TOKENS**: Password reset tokens
- **LOGIN_HISTORY**: User login history

---

## Cross-Service Relationships

While each microservice has its own database, they are connected through user IDs and other foreign keys at the application level:

``````mermaid
graph LR
    A[Auth Service] -->|user_id| B[User Service]
    B -->|user_id| C[Question Service]
    B -->|user_id| D[Exam Service]
    B -->|user_id| E[Social Service]
    B -->|user_id| F[News Service]
    B -->|user_id| G[Career Service]
    C -->|question_id| D
    B -->|elo_score| E
``````

---

## How to View These Diagrams

### Option 1: GitHub/GitLab
Upload this markdown file - Mermaid diagrams will render automatically.

### Option 2: Mermaid Live Editor
1. Go to https://mermaid.live
2. Copy any Mermaid code block above
3. Paste to see the diagram

### Option 3: VS Code
Install "Markdown Preview Mermaid Support" extension.

### Option 4: Online Markdown Viewers
- https://dillinger.io
- https://stackedit.io

---

## Database Connection Information

| Service | Database | Port | Username | Password |
|---------|----------|------|----------|----------|
| Auth | authdb | 5432 | postgres | 123456 |
| User | userdb | 5432 | postgres | 123456 |
| Question | questiondb | 5432 | postgres | 123456 |
| Exam | examdb | 5432 | postgres | 123456 |
| Career | careerdb | 5432 | postgres | 123456 |
| News | newsdb | 5432 | postgres | 123456 |
| Social | socialdb | 5432 | postgres | 123456 |

**Host**: localhost (or postgres from Docker)

---

Generated by generate-complete-erd.ps1
"@

# Write to file
$erdContent | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "[OK] Complete ERD generated: $outputFile" -ForegroundColor Green

Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "                  ERD Generation Complete!                      " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nGenerated File:" -ForegroundColor Cyan
Write-Host "   $outputFile" -ForegroundColor White

Write-Host "`nHow to View:" -ForegroundColor Cyan
Write-Host "   1. Open in VS Code with Mermaid extension" -ForegroundColor Gray
Write-Host "   2. Upload to GitHub/GitLab (auto-renders)" -ForegroundColor Gray
Write-Host "   3. Copy Mermaid code to https://mermaid.live" -ForegroundColor Gray
Write-Host "   4. Use online markdown viewer (dillinger.io, stackedit.io)" -ForegroundColor Gray

Write-Host "`nOpening file..." -ForegroundColor Yellow
Start-Sleep -Seconds 1
Start-Process $outputFile

Write-Host "`n[OK] Complete!`n" -ForegroundColor Green
