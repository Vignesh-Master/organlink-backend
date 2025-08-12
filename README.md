# OrganLink Backend

A blockchain-based decentralized platform for multi-organ donation and transplantation that enhances efficiency, transparency, and security in donor-recipient matching.

## 🏥 Features

- **Multi-Portal System**: Admin, Hospital, and Organization portals
- **AI-Powered Matching**: Advanced donor-patient matching algorithms
- **Blockchain Integration**: Ethereum Sepolia testnet for transparency
- **IPFS Storage**: Decentralized file storage with Pinata
- **OCR Verification**: Tesseract-based signature verification
- **Policy Governance**: Democratic policy voting system
- **Real-time Notifications**: WebSocket-based updates

## 🛠 Technology Stack

- **Framework**: Spring Boot 3.2 with Java 21
- **Database**: MySQL 8.0
- **Security**: Spring Security + JWT
- **Blockchain**: Web3j + Ethereum Sepolia
- **IPFS**: Pinata API integration
- **AI/ML**: Weka library for matching
- **OCR**: Tesseract 4J
- **WebSocket**: STOMP protocol

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0
- Tesseract OCR installed
- Ethereum Sepolia testnet access
- Pinata IPFS account

## ⚙️ Configuration

### Database Setup
```sql
CREATE DATABASE organlink_db;
```

### Environment Variables
The application uses the following configuration in `application.yml`:

- **Database**: MySQL with credentials (root/12345)
- **Blockchain**: Ethereum Sepolia testnet
- **IPFS**: Pinata API integration
- **JWT**: Token-based authentication

### Required External Services

1. **Ethereum Sepolia Testnet**
   - Infura API endpoint configured
   - Private key for transactions

2. **Pinata IPFS**
   - API key and secret configured
   - JWT token for authentication

3. **Tesseract OCR**
   - Installed and accessible in system PATH
   - English language data files

## 🚀 Getting Started

### 1. Clone Repository
```bash
git clone <repository-url>
cd organlink-backend
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Configure Database
- Ensure MySQL is running
- Database will be created automatically

### 4. Run Application
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8081`

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/v1/admin/login` - Admin authentication
- `POST /api/v1/hospital/login` - Hospital authentication
- `POST /api/v1/organization/login` - Organization authentication

### Admin Endpoints
- `GET /api/v1/admin/stats` - System statistics
- `GET /api/v1/admin/hospitals` - Hospital management
- `POST /api/v1/admin/hospitals` - Create hospital
- `GET /api/v1/admin/organizations` - Organization management
- `POST /api/v1/admin/organizations` - Create organization

### Hospital Endpoints
- `GET /api/v1/hospital/dashboard/stats` - Hospital dashboard
- `POST /api/v1/donors` - Register donor
- `GET /api/v1/donors` - List donors
- `POST /api/v1/patients` - Register patient
- `GET /api/v1/patients` - List patients
- `POST /api/v1/ai/find-matches/{patientId}` - AI matching

### Organization Endpoints
- `GET /api/v1/policies` - List policies
- `POST /api/v1/policies` - Create policy
- `POST /api/v1/policies/{id}/vote` - Vote on policy

### Location Endpoints
- `GET /api/v1/locations/countries` - List countries
- `GET /api/v1/locations/states` - List states
- `GET /api/v1/hospital/cities-by-state` - List cities
- `GET /api/v1/hospital/hospitals-by-city` - List hospitals

## 🔐 Security

- **JWT Authentication**: Token-based authentication with refresh tokens
- **Role-Based Access**: ADMIN, HOSPITAL, ORGANIZATION roles
- **Multi-Tenant**: Hospital-specific data isolation
- **CORS**: Configured for frontend origins
- **Password Encryption**: BCrypt hashing

## 🔗 Blockchain Integration

### Smart Contracts
- **PolicyVoting.sol**: Policy governance and voting
- **SignatureVerification.sol**: Document authenticity

### Blockchain Events
- Policy creation and voting
- Donor/Patient registration
- Signature verification
- Match creation

## 📁 Project Structure

```
src/main/java/com/organlink/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data transfer objects
├── entity/         # JPA entities
├── repository/     # Data repositories
├── service/        # Business logic
├── security/       # Security components
├── blockchain/     # Blockchain integration
├── ipfs/          # IPFS integration
└── ai/            # AI matching algorithms
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run integration tests
mvn test -Dtest=*IntegrationTest
```

## 🐳 Docker Support

```bash
# Build image
docker build -t organlink-backend .

# Run with Docker Compose
docker-compose up -d
```

## 📊 Monitoring

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

## 🔄 Version History

- **v1.0.0** - Initial release with core functionality
- **v1.1.0** - Added blockchain integration
- **v1.2.0** - Enhanced AI matching algorithms
- **v1.3.0** - IPFS and OCR integration

---

**OrganLink Backend** - Revolutionizing organ donation through blockchain technology and AI-powered matching.
