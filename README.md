# Resumora Backend

> Spring Boot REST API for the Resumora resume builder SaaS platform.

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7-47A248?logo=mongodb&logoColor=white)

---

## Tech Stack

- **Java 21** + **Spring Boot 3.5**
- **MongoDB** - document storage
- **Spring Security** + **JWT** - stateless authentication
- **Cloudinary** - image upload & storage
- **Brevo (SMTP)** - transactional email
- **Stripe Java SDK** - payment processing
- **Lombok** - boilerplate reduction

## API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT |
| GET | `/api/auth/verify-email?token=` | Verify email |
| POST | `/api/auth/resend-verification` | Resend verification email |
| GET | `/api/auth/profile` | Get current user profile |
| POST | `/api/auth/upload-image` | Upload profile photo |

### Resumes
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/resumes` | Get all user resumes |
| POST | `/api/resumes` | Create new resume |
| GET | `/api/resumes/:id` | Get resume by ID |
| PUT | `/api/resumes/:id` | Update resume |
| DELETE | `/api/resumes/:id` | Delete resume |
| PUT | `/api/resumes/:id/upload-images` | Upload resume images |

### Templates
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/templates` | Get available templates for user's plan |

### Payments
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/payment/create-order` | Create Stripe PaymentIntent |
| POST | `/api/payment/verify` | Verify payment after completion |
| POST | `/api/payment/webhook` | Stripe webhook handler |
| GET | `/api/payment/history` | Get user payment history |

### Email
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/email/send-resume` | Send resume PDF via email |

## Setup

### Prerequisites
- Java 21
- Maven 3.8+
- MongoDB running locally or MongoDB Atlas URI

### Configuration

Create `src/main/resources/application.properties`:

```properties
spring.application.name=resume-builder

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/resumebuilder_db
# OR for Atlas:
# spring.data.mongodb.uri=mongodb+srv://USER:PASS@cluster.mongodb.net/resumebuilder_db

# Email (Brevo SMTP)
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=YOUR_BREVO_SMTP_LOGIN
spring.mail.password=YOUR_BREVO_SMTP_KEY
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.starttls.enable=true
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.from=your@email.com

# App URL
app.base.url=http://localhost:8080

# Cloudinary
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET

# JWT
jwt.secret=your-very-long-secret-key-at-least-32-chars
jwt.expiration=604800000

# Stripe
stripe.secret.key=sk_test_YOUR_STRIPE_SECRET_KEY
stripe.webhook.secret=whsec_YOUR_WEBHOOK_SECRET
```

### Run Locally

```bash
# Clone
git clone https://github.com/Pasan-Yashobha/resumora-backend.git
cd resumora-backend

# Build and run
./mvnw spring-boot:run

# Or build JAR
./mvnw clean package -DskipTests
java -jar target/resume-builder-0.0.1-SNAPSHOT.jar
```

Server starts at `http://localhost:8080`

## Deployment

### Railway 

1. Push code to GitHub
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Add a MongoDB plugin or use MongoDB Atlas
4. Set all environment variables in Railway dashboard
5. Railway auto-detects Spring Boot and builds with Maven

### Render

1. New Web Service → Connect GitHub repo
2. Build command: `./mvnw clean package -DskipTests`
3. Start command: `java -jar target/resume-builder-0.0.1-SNAPSHOT.jar`
4. Add environment variables

### Docker

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/resume-builder-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
./mvnw clean package -DskipTests
docker build -t resumora-backend .
docker run -p 8080:8080 --env-file .env resumora-backend
```

---

Made with love by Pasan Yashobha
