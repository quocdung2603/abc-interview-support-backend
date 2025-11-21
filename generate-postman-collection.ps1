# =============================================
# Generate Postman Collection for Interview Microservice ABC
# =============================================

$baseUrl = "http://localhost:8080"
$collection = @{
    info = @{
        name = "Interview Microservice ABC - Complete API Collection"
        description = "Complete API collection for all microservices: Auth, User, Question, Exam, News, Career"
        schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        _postman_id = [guid]::NewGuid().ToString()
    }
    variable = @(
        @{ key = "baseUrl"; value = $baseUrl; type = "string" }
        @{ key = "adminToken"; value = ""; type = "string" }
        @{ key = "userToken"; value = ""; type = "string" }
        @{ key = "recruiterToken"; value = ""; type = "string" }
    )
    item = @()
}

# Helper function to create request
function New-Request {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [string]$Description = ""
    )
    
    $request = @{
        name = $Name
        request = @{
            method = $Method
            header = @()
            url = @{
                raw = "{{baseUrl}}$Url"
                host = @("{{baseUrl}}")
                path = $Url.TrimStart('/').Split('/')
            }
            description = $Description
        }
    }
    
    # Add headers
    foreach ($h in $Headers.GetEnumerator()) {
        $request.request.header += @{
            key = $h.Key
            value = $h.Value
            type = "text"
        }
    }
    
    # Add body if present
    if ($Body) {
        $request.request.body = @{
            mode = "raw"
            raw = ($Body | ConvertTo-Json -Depth 5)
            options = @{
                raw = @{
                    language = "json"
                }
            }
        }
    }
    
    return $request
}

Write-Host "Generating Postman Collection..." -ForegroundColor Cyan

# ==================== AUTH SERVICE ====================
$authFolder = @{
    name = "Auth Service"
    description = "Authentication and authorization endpoints"
    item = @(
        (New-Request -Name "Register" -Method "POST" -Url "/auth/register" `
            -Headers @{"Content-Type"="application/json"} `
            -Body @{email="testuser@example.com";password="Pass@123";fullName="Test User";phone="0901234567";roleName="USER"} `
            -Description "Register a new user account"),
        
        (New-Request -Name "Login (Admin)" -Method "POST" -Url "/auth/login" `
            -Headers @{"Content-Type"="application/json"} `
            -Body @{email="admin@example.com";password="admin123"} `
            -Description "Login as admin to get access token"),
        
        (New-Request -Name "Login (User)" -Method "POST" -Url "/auth/login" `
            -Headers @{"Content-Type"="application/json"} `
            -Body @{email="user@example.com";password="admin123"} `
            -Description "Login as user to get access token"),
        
        (New-Request -Name "Login (Recruiter)" -Method "POST" -Url "/auth/login" `
            -Headers @{"Content-Type"="application/json"} `
            -Body @{email="recruiter@example.com";password="admin123"} `
            -Description "Login as recruiter to get access token"),
        
        (New-Request -Name "Get User Info" -Method "GET" -Url "/auth/user-info" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get current user information from token"),
        
        (New-Request -Name "Verify Token" -Method "GET" -Url "/auth/verify?token={{userToken}}" `
            -Description "Verify if token is valid"),
        
        (New-Request -Name "Refresh Token" -Method "POST" -Url "/auth/refresh" `
            -Headers @{"Content-Type"="application/json"} `
            -Body @{refreshToken="your-refresh-token"} `
            -Description "Refresh access token using refresh token")
    )
}

# ==================== USER SERVICE ====================
$userFolder = @{
    name = "User Service"
    description = "User management endpoints"
    item = @(
        # Internal endpoints (for inter-service communication)
        @{
            name = "Internal APIs"
            item = @(
                (New-Request -Name "Create User (Internal)" -Method "POST" -Url "/users/internal/create" `
                    -Headers @{"Content-Type"="application/json"} `
                    -Body @{email="internal@test.com";password="Pass@123";fullName="Internal User";roleId=1} `
                    -Description "Internal endpoint for auth-service to create users"),
                
                (New-Request -Name "Get User by ID (Internal)" -Method "GET" -Url "/users/internal/user/1" `
                    -Description "Internal endpoint to get user"),
                
                (New-Request -Name "Verify Token" -Method "POST" -Url "/users/verify-token" `
                    -Headers @{"Content-Type"="application/json"} `
                    -Body @{token="verify-token-here"} `
                    -Description "Verify user email token")
            )
        },
        
        (New-Request -Name "Get All Users" -Method "GET" -Url "/users?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get paginated list of all users"),
        
        (New-Request -Name "Get User by ID" -Method "GET" -Url "/users/1" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get user details by ID"),
        
        (New-Request -Name "Get User by Email" -Method "GET" -Url "/users/by-email/admin@example.com" `
            -Description "Get user by email address"),
        
        (New-Request -Name "Check Email Exists" -Method "GET" -Url "/users/check-email/test@example.com" `
            -Description "Check if email already exists"),
        
        (New-Request -Name "Get Users by Role" -Method "GET" -Url "/users/role/1?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get users by role ID"),
        
        (New-Request -Name "Get Users by Status" -Method "GET" -Url "/users/status/ACTIVE?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get users by status"),
        
        (New-Request -Name "Update User" -Method "PUT" -Url "/users/3" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{email="user@example.com";password="admin123";fullName="Updated Name";address="Ha Noi"} `
            -Description "Update user information (requires email and password)"),
        
        (New-Request -Name "Update User Role" -Method "PUT" -Url "/users/1/role" `
            -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
            -Body @{roleId=2} `
            -Description "Update user role"),
        
        (New-Request -Name "Update User Status" -Method "PUT" -Url "/users/1/status" `
            -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
            -Body @{status="ACTIVE"} `
            -Description "Update user status"),
        
        (New-Request -Name "Update User ELO" -Method "POST" -Url "/users/elo" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{userId=3;action="WIN";points=50;description="Won a match"} `
            -Description "Update user ELO score"),
        
        (New-Request -Name "Validate Password" -Method "POST" -Url "/users/validate-password" `
            -Headers @{"Content-Type"="application/json"} `
            -Body @{email="admin@example.com";password="admin123"} `
            -Description "Validate user password"),
        
        (New-Request -Name "Delete User" -Method "DELETE" -Url "/users/99" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Delete user by ID"),
        
        (New-Request -Name "Get All Roles" -Method "GET" -Url "/users/roles" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get all user roles")
    )
}

# ==================== QUESTION SERVICE ====================
$questionFolder = @{
    name = "Question Service"
    description = "Question, Answer, Topic, Field management"
    item = @(
        # Fields
        @{
            name = "Fields"
            item = @(
                (New-Request -Name "Create Field" -Method "POST" -Url "/questions/fields" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
                    -Body @{name="New Field";description="Field description"} `
                    -Description "Create a new field"),
                
                (New-Request -Name "Get All Fields" -Method "GET" -Url "/questions/fields" `
                    -Description "Get all fields"),
                
                (New-Request -Name "Get Field by ID" -Method "GET" -Url "/questions/fields/1" `
                    -Description "Get field by ID"),
                
                (New-Request -Name "Update Field" -Method "PUT" -Url "/questions/fields/1" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
                    -Body @{name="Updated Field";description="Updated description"} `
                    -Description "Update field"),
                
                (New-Request -Name "Delete Field" -Method "DELETE" -Url "/questions/fields/99" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Delete field")
            )
        },
        
        # Topics
        @{
            name = "Topics"
            item = @(
                (New-Request -Name "Create Topic" -Method "POST" -Url "/questions/topics" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
                    -Body @{fieldId=1;name="New Topic";description="Topic description"} `
                    -Description "Create a new topic"),
                
                (New-Request -Name "Get All Topics" -Method "GET" -Url "/questions/topics" `
                    -Description "Get all topics"),
                
                (New-Request -Name "Get Topic by ID" -Method "GET" -Url "/questions/topics/1" `
                    -Description "Get topic by ID"),
                
                (New-Request -Name "Update Topic" -Method "PUT" -Url "/questions/topics/1" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
                    -Body @{name="Updated Topic";description="Updated description"} `
                    -Description "Update topic"),
                
                (New-Request -Name "Delete Topic" -Method "DELETE" -Url "/questions/topics/99" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Delete topic"),
                
                (New-Request -Name "Get Questions by Topic" -Method "GET" -Url "/questions/topics/1/questions?page=0&size=10" `
                    -Description "Get all questions for a topic")
            )
        },
        
        # Levels
        @{
            name = "Levels"
            item = @(
                (New-Request -Name "Create Level" -Method "POST" -Url "/questions/levels" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
                    -Body @{name="Expert";description="Expert level"} `
                    -Description "Create a new level"),
                
                (New-Request -Name "Get All Levels" -Method "GET" -Url "/questions/levels" `
                    -Description "Get all levels"),
                
                (New-Request -Name "Get Level by ID" -Method "GET" -Url "/questions/levels/1" `
                    -Description "Get level by ID"),
                
                (New-Request -Name "Update Level" -Method "PUT" -Url "/questions/levels/1" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
                    -Body @{name="Updated Level"} `
                    -Description "Update level"),
                
                (New-Request -Name "Delete Level" -Method "DELETE" -Url "/questions/levels/99" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Delete level")
            )
        },
        
        # Question Types
        @{
            name = "Question Types"
            item = @(
                (New-Request -Name "Create Question Type" -Method "POST" -Url "/questions/question-types" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
                    -Body @{name="Essay";description="Essay questions"} `
                    -Description "Create a new question type"),
                
                (New-Request -Name "Get All Question Types" -Method "GET" -Url "/questions/question-types" `
                    -Description "Get all question types"),
                
                (New-Request -Name "Get Question Type by ID" -Method "GET" -Url "/questions/question-types/1" `
                    -Description "Get question type by ID"),
                
                (New-Request -Name "Update Question Type" -Method "PUT" -Url "/questions/question-types/1" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
                    -Body @{name="Updated Type"} `
                    -Description "Update question type"),
                
                (New-Request -Name "Delete Question Type" -Method "DELETE" -Url "/questions/question-types/99" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Delete question type")
            )
        },
        
        # Questions
        @{
            name = "Questions"
            item = @(
                (New-Request -Name "Create Question" -Method "POST" -Url "/questions" `
                    -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
                    -Body @{userId=3;topicId=1;fieldId=1;levelId=2;questionTypeId=1;content="What is Spring Boot?";answer="Spring Boot is a framework that simplifies Java development";language="JAVA"} `
                    -Description "Create a new question"),
                
                (New-Request -Name "Get All Questions" -Method "GET" -Url "/questions?page=0&size=10" `
                    -Description "Get paginated questions"),
                
                (New-Request -Name "Get Question by ID" -Method "GET" -Url "/questions/1" `
                    -Description "Get question by ID"),
                
                (New-Request -Name "Update Question" -Method "PUT" -Url "/questions/1" `
                    -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
                    -Body @{questionContent="Updated content"} `
                    -Description "Update question"),
                
                (New-Request -Name "Delete Question" -Method "DELETE" -Url "/questions/99" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Delete question"),
                
                (New-Request -Name "Approve Question" -Method "POST" -Url "/questions/1/approve" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Approve question"),
                
                (New-Request -Name "Reject Question" -Method "POST" -Url "/questions/1/reject" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Reject question")
            )
        },
        
        # Answers
        @{
            name = "Answers"
            item = @(
                (New-Request -Name "Create Answer" -Method "POST" -Url "/questions/answers" `
                    -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
                    -Body @{userId=3;questionId=1;questionTypeId=1;content="My answer content";isCorrect=$true} `
                    -Description "Create answer for a question"),
                
                (New-Request -Name "Get All Answers" -Method "GET" -Url "/questions/answers?page=0&size=10" `
                    -Description "Get all answers"),
                
                (New-Request -Name "Get Answer by ID" -Method "GET" -Url "/questions/answers/1" `
                    -Description "Get answer by ID"),
                
                (New-Request -Name "Get Answers by Question" -Method "GET" -Url "/questions/1/answers?page=0&size=10" `
                    -Description "Get all answers for a question"),
                
                (New-Request -Name "Update Answer" -Method "PUT" -Url "/questions/answers/1" `
                    -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
                    -Body @{content="Updated answer"} `
                    -Description "Update answer"),
                
                (New-Request -Name "Delete Answer" -Method "DELETE" -Url "/questions/answers/99" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Delete answer"),
                
                (New-Request -Name "Mark Answer as Sample" -Method "POST" -Url "/questions/answers/1/sample?isSample=true" `
                    -Headers @{"Authorization"="Bearer {{adminToken}}"} `
                    -Description "Mark answer as sample answer")
            )
        }
    )
}

# ==================== EXAM SERVICE ====================
$examFolder = @{
    name = "Exam Service"
    description = "Exam, Registration, Result, Answer management"
    item = @(
        (New-Request -Name "Create Exam" -Method "POST" -Url "/exams" `
            -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
            -Body @{title="Java Spring Boot Exam";description="Test your Spring Boot knowledge";duration=60;passingScore=70} `
            -Description "Create a new exam"),
        
        (New-Request -Name "Get All Exams" -Method "GET" -Url "/exams?page=0&size=10" `
            -Description "Get all exams"),
        
        (New-Request -Name "Get Exam by ID" -Method "GET" -Url "/exams/1" `
            -Description "Get exam by ID"),
        
        (New-Request -Name "Update Exam" -Method "PUT" -Url "/exams/1" `
            -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
            -Body @{title="Updated Exam Title"} `
            -Description "Update exam"),
        
        (New-Request -Name "Delete Exam" -Method "DELETE" -Url "/exams/99" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Delete exam"),
        
        (New-Request -Name "Get Exams by User" -Method "GET" -Url "/exams/user/1?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get exams created by user"),
        
        (New-Request -Name "Get Exams by Type" -Method "GET" -Url "/exams/type?examType=PRACTICE&page=0&size=10" `
            -Description "Get exams by type"),
        
        (New-Request -Name "Get Exam Types" -Method "GET" -Url "/exams/types" `
            -Description "Get all exam types"),
        
        (New-Request -Name "Publish Exam" -Method "POST" -Url "/exams/1/publish" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Publish exam"),
        
        (New-Request -Name "Start Exam" -Method "POST" -Url "/exams/1/start" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Start an exam session"),
        
        (New-Request -Name "Complete Exam" -Method "POST" -Url "/exams/1/complete" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Complete exam session"),
        
        # Exam Questions
        (New-Request -Name "Add Questions to Exam" -Method "POST" -Url "/exams/questions" `
            -Headers @{"Authorization"="Bearer {{adminToken}}";"Content-Type"="application/json"} `
            -Body @{examId=1;questionId=1;orderNumber=1} `
            -Description "Add question to exam"),
        
        (New-Request -Name "Remove Question from Exam" -Method "DELETE" -Url "/exams/1/questions?questionId=1" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Remove question from exam"),
        
        # Exam Registrations
        (New-Request -Name "Register for Exam" -Method "POST" -Url "/exams/registrations" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{examId=1;userId=2} `
            -Description "Register user for exam"),
        
        (New-Request -Name "Get Registration by ID" -Method "GET" -Url "/exams/registrations/1" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get registration by ID"),
        
        (New-Request -Name "Get Registrations by Exam" -Method "GET" -Url "/exams/1/registrations?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get exam registrations"),
        
        (New-Request -Name "Get Registrations by User" -Method "GET" -Url "/exams/registrations/user/2?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get user registrations"),
        
        (New-Request -Name "Cancel Exam Registration" -Method "POST" -Url "/exams/registrations/1/cancel" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Cancel exam registration"),
        
        # Exam Results
        (New-Request -Name "Submit Exam Result" -Method "POST" -Url "/exams/results" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{examId=1;userId=2;score=85;isPassed=$true} `
            -Description "Submit exam result"),
        
        (New-Request -Name "Get Result by ID" -Method "GET" -Url "/exams/results/1" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get result by ID"),
        
        (New-Request -Name "Get Results by Exam" -Method "GET" -Url "/exams/1/results?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get results by exam"),
        
        (New-Request -Name "Get Results by User" -Method "GET" -Url "/exams/results/user/2?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get results by user"),
        
        # Exam Answers
        (New-Request -Name "Submit Answer" -Method "POST" -Url "/exams/answers" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{examId=1;questionId=1;userId=2;answerContent="My answer"} `
            -Description "Submit answer for exam question"),
        
        (New-Request -Name "Get Answer by ID" -Method "GET" -Url "/exams/answers/1" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get answer by ID"),
        
        (New-Request -Name "Get User Answers for Exam" -Method "GET" -Url "/exams/1/answers/2" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get user's answers for exam")
    )
}

# ==================== NEWS SERVICE ====================
$newsFolder = @{
    name = "News Service"
    description = "News and Recruitment management"
    item = @(
        (New-Request -Name "Create News" -Method "POST" -Url "/news" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{title="Breaking News";content="News content here";fieldId=1;newsType="NEWS"} `
            -Description "Create news article"),
        
        (New-Request -Name "Get All News" -Method "GET" -Url "/news?page=0&size=10" `
            -Description "Get all news"),
        
        (New-Request -Name "Get News by ID" -Method "GET" -Url "/news/1" `
            -Description "Get news by ID"),
        
        (New-Request -Name "Get News Types" -Method "GET" -Url "/news/types" `
            -Description "Get available news types"),
        
        (New-Request -Name "Get News by Type" -Method "GET" -Url "/news/type?newsType=NEWS&page=0&size=10" `
            -Description "Get news by type"),
        
        (New-Request -Name "Get News by Field" -Method "GET" -Url "/news/field/1?page=0&size=10" `
            -Description "Get news by field"),
        
        (New-Request -Name "Get News by User" -Method "GET" -Url "/news/user/1?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get news by user"),
        
        (New-Request -Name "Get News by Status" -Method "GET" -Url "/news/status/PUBLISHED?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get news by status"),
        
        (New-Request -Name "Get Pending Moderation" -Method "GET" -Url "/news/moderation/pending?page=0&size=10" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Get pending news for moderation"),
        
        (New-Request -Name "Update News" -Method "PUT" -Url "/news/1" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{title="Updated Title";content="Updated content"} `
            -Description "Update news"),
        
        (New-Request -Name "Delete News" -Method "DELETE" -Url "/news/99" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Delete news"),
        
        (New-Request -Name "Approve News" -Method "POST" -Url "/news/1/approve" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Approve news"),
        
        (New-Request -Name "Reject News" -Method "POST" -Url "/news/1/reject" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Reject news"),
        
        (New-Request -Name "Publish News" -Method "POST" -Url "/news/1/publish" `
            -Headers @{"Authorization"="Bearer {{adminToken}}"} `
            -Description "Publish news"),
        
        (New-Request -Name "Vote News" -Method "POST" -Url "/news/1/vote" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{voteType="USEFUL"} `
            -Description "Vote on news"),
        
        # Recruitments
        (New-Request -Name "Create Recruitment" -Method "POST" -Url "/recruitments" `
            -Headers @{"Authorization"="Bearer {{recruiterToken}}";"Content-Type"="application/json"} `
            -Body @{title="Senior Developer";content="Job description";fieldId=1;companyName="ABC Corp";location="Ha Noi";salary="2000-3000 USD"} `
            -Description "Create recruitment post"),
        
        (New-Request -Name "Get All Recruitments" -Method "GET" -Url "/recruitments?page=0&size=10" `
            -Description "Get all recruitment posts"),
        
        (New-Request -Name "Get Recruitments by Company" -Method "GET" -Url "/recruitments/company/ABC Corp?page=0&size=10" `
            -Description "Get recruitments by company name")
    )
}

# ==================== CAREER SERVICE ====================
$careerFolder = @{
    name = "Career Service"
    description = "Career preference management"
    item = @(
        (New-Request -Name "Create Career Preference" -Method "POST" -Url "/career" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{userId=2;fieldId=1;experienceLevel="INTERMEDIATE";preferredLocation="Ha Noi"} `
            -Description "Create user career preference"),
        
        (New-Request -Name "Get Career by ID" -Method "GET" -Url "/career/1" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get career preference by ID"),
        
        (New-Request -Name "Get Career Preferences by User" -Method "GET" -Url "/career/preferences/2" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Get all career preferences for user"),
        
        (New-Request -Name "Update Career Preference" -Method "PUT" -Url "/career/update/1" `
            -Headers @{"Authorization"="Bearer {{userToken}}";"Content-Type"="application/json"} `
            -Body @{experienceLevel="SENIOR";preferredLocation="Ho Chi Minh"} `
            -Description "Update career preference"),
        
        (New-Request -Name "Delete Career Preference" -Method "DELETE" -Url "/career/99" `
            -Headers @{"Authorization"="Bearer {{userToken}}"} `
            -Description "Delete career preference")
    )
}

# ==================== INFRASTRUCTURE ====================
$infraFolder = @{
    name = "Infrastructure"
    description = "Gateway, Eureka, Health checks"
    item = @(
        (New-Request -Name "Gateway Health" -Method "GET" -Url "/actuator/health" `
            -Description "Check gateway health"),
        
        (New-Request -Name "Eureka Dashboard" -Method "GET" -Url "/eureka/web" `
            -Description "Access Eureka dashboard"),
        
        (New-Request -Name "Eureka Apps" -Method "GET" -Url "/eureka/apps" `
            -Description "Get registered applications")
    )
}

# Add all folders to collection
$collection.item += $authFolder
$collection.item += $userFolder
$collection.item += $questionFolder
$collection.item += $examFolder
$collection.item += $newsFolder
$collection.item += $careerFolder
$collection.item += $infraFolder

# Export to JSON
$outputPath = "Interview-Microservice-ABC-Postman-Collection.json"
$collection | ConvertTo-Json -Depth 20 | Set-Content $outputPath -Encoding UTF8

Write-Host "`n===============================================" -ForegroundColor Green
Write-Host "   Postman Collection Generated Successfully!" -ForegroundColor Green
Write-Host "===============================================`n" -ForegroundColor Green
Write-Host "File: $outputPath" -ForegroundColor Cyan
Write-Host "`nCollection includes:" -ForegroundColor Yellow
Write-Host "  - Auth Service: 7 endpoints" -ForegroundColor White
Write-Host "  - User Service: 13 endpoints" -ForegroundColor White
Write-Host "  - Question Service: 40+ endpoints (Fields, Topics, Levels, Types, Questions, Answers)" -ForegroundColor White
Write-Host "  - Exam Service: 17 endpoints (Exams, Registrations, Results, Answers)" -ForegroundColor White
Write-Host "  - News Service: 18 endpoints (News, Recruitments)" -ForegroundColor White
Write-Host "  - Career Service: 5 endpoints" -ForegroundColor White
Write-Host "  - Infrastructure: 3 endpoints" -ForegroundColor White
Write-Host "`nTotal: 100+ API endpoints" -ForegroundColor Green
Write-Host "`nHow to use:" -ForegroundColor Yellow
Write-Host "  1. Open Postman" -ForegroundColor White
Write-Host "  2. Click Import > Upload Files" -ForegroundColor White
Write-Host "  3. Select: $outputPath" -ForegroundColor Cyan
Write-Host "  4. Start with 'Auth Service > Login (Admin)' to get token" -ForegroundColor White
Write-Host "  5. Copy the accessToken and set it in Collection Variables > adminToken" -ForegroundColor White
Write-Host "`n" -ForegroundColor White
