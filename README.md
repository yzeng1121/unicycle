# UniCycle

A peer-to-peer campus marketplace app that makes it easy for college students to buy and sell items within their campus community.

## Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Frontend    | React Native                        |
| Backend     | Spring Boot (Java)                  |
| Database    | PostgreSQL (via Supabase)           |
| Auth        | JWT (JSON Web Tokens)               |
| Storage     | AWS S3 (images/media)               |
| Hosting     | AWS EC2                             |

## Project Structure

```
unicycle/
├── frontend/            # React Native mobile app
│   ├── src/
│   │   ├── components/  # Reusable UI components
│   │   ├── screens/     # App screens (Home, Listing, Profile, etc.)
│   │   ├── navigation/  # React Navigation setup
│   │   ├── services/    # API client & auth helpers
│   │   └── assets/      # Images, fonts, icons
│   ├── package.json
│   └── app.json
├── backend/             # Spring Boot REST API
│   ├── src/main/java/
│   │   └── com/unicycle/
│   │       ├── controller/   # REST controllers
│   │       ├── service/      # Business logic
│   │       ├── repository/   # JPA repositories
│   │       ├── model/        # Entity classes
│   │       ├── config/       # Security & app config
│   │       └── dto/          # Data transfer objects
│   ├── src/main/resources/
│   │   └── application.properties
│   └── build.gradle
└── README.md
```

> **Note:** Adjust the directory tree above to match your actual repo layout.

## Prerequisites

- **Node.js** (v18+) and **npm** or **yarn**
- **Java 17+** and **Gradle**
- **React Native CLI** (or Expo, if applicable)
- **PostgreSQL** — or a [Supabase](https://supabase.com) project
- **AWS account** with S3 bucket and EC2 instance configured
- Android Studio / Xcode for mobile emulation

## Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/<your-username>/unicycle.git
cd unicycle
```

### 2. Backend Setup

```bash
cd backend

# Configure your database and AWS credentials
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Edit `application.properties` with your values:

```properties
spring.datasource.url=jdbc:postgresql://<SUPABASE_HOST>:5432/<DB_NAME>
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASSWORD>

aws.s3.bucket=<YOUR_S3_BUCKET>
aws.s3.region=<YOUR_REGION>

jwt.secret=<YOUR_JWT_SECRET>
```

Build and run:

```bash
./gradlew build
./gradlew bootRun
```

The API will start on `http://localhost:8080`.

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npx react-native start
```

In a separate terminal:

```bash
# Run on Android
npx react-native run-android

# Run on iOS (macOS only)
npx react-native run-ios
```

## Environment Variables

| Variable                | Description                     |
|-------------------------|---------------------------------|
| `DB_URL`                | PostgreSQL connection string    |
| `DB_USER`               | Database username               |
| `DB_PASSWORD`           | Database password               |
| `AWS_ACCESS_KEY_ID`     | AWS access key                  |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key                  |
| `S3_BUCKET`             | S3 bucket name for uploads      |
| `JWT_SECRET`            | Secret key for JWT signing      |

## Deployment

The backend is deployed on **AWS EC2** and managed as a `systemd` service:

```bash
# On the EC2 instance
sudo systemctl start unicycle
sudo systemctl status unicycle

# View logs
journalctl -u unicycle -f
```

## Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m "Add your feature"`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

## License

MIT

---

Built by [Yuxin](https://github.com/yzeng1121)
