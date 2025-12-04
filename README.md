# iLibrary Backend API Documentation

This document provides a comprehensive overview of all backend APIs for the iLibrary project.

## Base URL
```
http://localhost:8080
```

## Authentication
JWT Bearer Token Authorization.

### Example Header
```
Authorization: Bearer <token>
```

---

## 📘 Public APIs
| Method | Endpoint | Description |
|--------|-----------|--------------|
| POST | `/public/signup` | Register a new user |
| POST | `/public/login` | Authenticate and receive JWT token |
| GET | `/public/healthCheck` | Check API health |

---

## 🎫 Booking APIs
| Method | Endpoint | Description |
|--------|-----------|--------------|
| POST | `/booking/seat` | Book a seat |
| DELETE | `/booking/cancel/{bookingId}` | Cancel a booking |

---

## 💳 Subscription APIs
| Method | Endpoint | Description |
|--------|-----------|--------------|
| POST | `/subscription/buy` | Purchase a subscription |
| PUT | `/subscription/renew` | Renew a subscription |
| PUT | `/subscription/cancel` | Cancel subscription |
| GET | `/subscription/status` | Check current subscription status |

---

## 🧑‍💼 Admin APIs
| Method | Endpoint | Description |
|--------|-----------|--------------|
| GET | `/admin/allUsers` | List all users |
| GET | `/admin/allSeats` | List all seats |
| GET | `/admin/allBooking` | List all bookings |
| GET | `/admin/allSubscription` | List all subscriptions |

---

## 💰 Price & Payment APIs
| Method | Endpoint | Description |
|--------|-----------|--------------|
| GET | `/payment/seat` | Seat payment checkout |
| GET | `/payment/subscription` | Subscription payment checkout |

---

## ⚙️ Stripe Webhook APIs
| Method | Endpoint | Description |
|--------|-----------|--------------|
| POST | `/webhook/subscription` | Handle subscription webhook |
| POST | `/webhook/seat` | Handle seat booking webhook |

---

## 📚 Example API Usage
### Signup
```bash
curl -X POST http://localhost:8080/public/signup -H "Content-Type: application/json" -d '{"username":"john","password":"12345","email":"john@gmail.com"}'
```

### Login
```bash
curl -X POST http://localhost:8080/public/login -H "Content-Type: application/json" -d '{"username":"john","password":"12345"}'
```

### Book a Seat
```bash
curl -X POST http://localhost:8080/booking/seat -H "Authorization: Bearer <JWT>" -H "Content-Type: application/json" -d '{"seatNumber":"A12","hours":2}'
```

---

## 🧩 Data Schemas

### User
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "roles": ["ROLE_USER"]
}
```

### Subscription
```json
{
  "type": "WEEKLY | MONTHLY | YEARLY"
}
```

### SeatDTO
```json
{
  "seatNumber": "string",
  "hours": "integer"
}
```

---

## 🧪 Postman Collection
A ready-to-import Postman JSON collection can be generated automatically using this OpenAPI definition.

## Developed By- Himanshu Raturi [![LinkedIn](https://img.shields.io/badge/LinkedIn-Profile-blue?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/himanshu-raturi/)
