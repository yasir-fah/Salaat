# 📘 Salaat صالات  

## 📑 Table of Contents  
- [ Introduction](#-introduction) 
- [🚀 Features](#-features)  
- [🛠 Technology](#-technology)  
- [📊 Architecture](#-architecture)  
- [🔐 Auth](#-auth)  
- [🏠 Hall](#-hall)  
- [🏢 SubHall](#-subhall)  
- [🎮 Game](#-game)  
- [👤 Customer](#-customer)  
- [📅 Booking](#-booking)  
- [🧑‍💼 Owner](#-owner)  
- [⭐ ReviewHall](#-reviewhall)  
- [⭐ ReviewSubHall](#-reviewsubhall)  
- [💳 Payments](#-payments)  

---

## Introduction
-This project is a complete game hall and sub-hall management system. It provides booking for customers, payments, customer and owner management, reviews, and multi-role security. Built with Spring Boot and MySQL/PostgreSQL, it follows a RESTful architecture. The sections below detail the system’s features, technology stack, architecture, and full list of API endpoints.


## 🚀 Features  

- 🏠 **Hall & Sub-Hall Management**  
  - Full management of Halls and Sub-Halls.  
  - Add, update, delete, and view details easily.  
  - Set budgets for each Sub-Hall and manage associated assets.  

- 👤 **Customer Registration & Profile Management**  
  - Easy registration for new customers.  
  - Update profile information and view usage statistics.  
  - Manage personal bookings and cancel when needed.  

- 📅 **Booking & Payment System**  
  - Complete booking system for Sub-Halls and games inside Halls.  
  - Integrated payment gateway for seamless transactions.  
  - Track payment status and download invoices easily.  

- 🎮 **Game Scheduling & Management**  
  - Schedule and manage games inside Halls and Sub-Halls.  
  - Add, update, and delete games.  
  - Control game availability for each Sub-Hall.  

- ⭐ **Reviews & Ratings**  
  - Rate and review Halls and Sub-Halls.  
  - View average ratings for each venue.  
  - Support adding images or assets with reviews.  

- 🔒 **Multi-role Security**  
  - Multi-role access control: Admin, Owner, Customer.  
  - Full control over endpoint access based on roles.  
  - Secure data management for a safe user experience.  

---

## 🛠 Technology  

| Technology            | Purpose                        |
| --------------------- | ------------------------------ |
| Java                  | programming language           |
| Spring Boot           | Backend framework              |
| Spring Web            | Build RESTful APIs             |
| Spring Data JPA       | Database operations            |
| Spring Security       | Authentication & Authorization |
| Spring AI             | AI integration with spring project |
| openAI API            | AI fetched Agent               |
| JWT                   | enhanced Authentication & Authorization |
| Spring Mail           | Email notifications            |
| Spring Validation     | User input validation          |
| MySQL / PostgreSQL    | Relational databases           |
| Lombok                | Reduce boilerplate code        |
| UltraMessage          | WhatsApp integration           |
| Adobe PDF             | Generate & export invoices     |
| Moyasar API           | Secure online payments         |
| AWS                   | Cloud deployment               |
| S3 bucket             | Cloud storage                  |
| JUnit                 | Unit testing                   |
| Postman               | API testing                    |
| Swagger               | API documentation              |
| Figma                 | UI/UX design                   |
| Maven                 | Build & dependency management  |
| Linear                | Task management platform       |

---

## 📊 Architecture  

- Class Diagram:
<img width="1520" height="982" alt="Screenshot 2025-09-09 015535" src="https://github.com/user-attachments/assets/d2807678-bc10-4724-922f-d591bce557c4" />
  
- Use Case Diagram:

   <img width="736" height="696" alt="image" src="https://github.com/user-attachments/assets/8c624368-4c6a-4f90-88b9-4045ceba5ab1" />
  
- Figma Design: 🔗 [Link  ](https://www.figma.com/proto/ImpAUj7GfdwHzNIl8hSUaE/final-project?node-id=151-1297&p=f&t=C1hLcuKVNaPO5njg-1&scaling=scale-down&content-scaling=fixed&page-id=0%3A1&starting-point-node-id=128%3A1143&show-proto-sidebar=1)
- Postman Documentation: 🔗 [Link  ](https://documenter.getpostman.com/view/48183973/2sB3HnJegq)

- postman demo: 🔗 [Link  ](https://drive.google.com/file/d/1KQODyG9AAUMsARQMiI7XoxjZQ4O17uY6/view?usp=sharing)

- figma demo:  🔗 [Link  ](https://drive.google.com/file/d/1M4ZRa0wmTf3bHm5Eq8OOprwHoconLW1v/view?usp=sharing)
---

## 🔐 JWT Auth  

| #   | Endpoint           | Method | Description   | Contributor |
| --- | ------------------ | ------ | ------------- | ----------- |
| 1   | /api/v1/auth/login | POST   | User login    | ZIYAD        |

---

## 🏠 Hall  

| #   | Endpoint                           | Method | Description              | Contributor |
| --- | ---------------------------------- | ------ | ------------------------ | ----------- |
| 2   | /api/v1/hall/get                   | GET    | Get all halls            | ZYAD        |
| 3   | /api/v1/hall/get/{hallId}          | GET    | Get hall by ID           | ZYAD        |
| 4   | /api/v1/hall/get-subhalls/{hallId} | GET    | Get subhalls of a hall   | ZYAD        |
| 5   | /api/v1/hall/add                   | POST   | Add new hall             | ZYAD        |
| 6   | /api/v1/hall/update/{hallId}       | PUT    | Update hall by ID        | ZYAD        |
| 7   | /api/v1/hall/delete/{hallId}       | DELETE | Delete hall by ID        | ZYAD        |
| 8   | /api/v1/hall/get/my                | GET    | Get halls owned by owner | ZYAD        |
| 9   | /api/v1/hall/get/available         | GET    | Get available halls      | ZYAD        |
| 10  | /api/v1/hall/get/unavailable       | GET    | Get unavailable halls    | ZYAD        |
| 11  | /api/v1/hall/get/asset/**          | GET    | Get hall assets          | ZYAD        |
| 12  | /api/v1/hall/add/asset/**          | POST   | Add hall assets          | ZYAD        |

---

## 🏢 SubHall  

| #   | Endpoint                                                                | Method | Description          | Contributor |
| --- | ----------------------------------------------------------------------- | ------ | -------------------- | ----------- |
| 13  | /api/v1/subhall/get/{subHallId}                                         | GET    | Get subhall by ID    | ZYAD        |
| 14  | /api/v1/subhall/add/{hallId}                                            | POST   | Add subhall to hall  | ZYAD        |
| 15  | /api/v1/subhall/update/{subHallId}                                      | PUT    | Update subhall by ID | ZYAD        |
| 16  | /api/v1/subhall/delete/{subHallId}                                      | DELETE | Delete subhall by ID | ZYAD        |
| 17  | /api/v1/subhall/hall/{hallId}/subhall/{subHallId}/budget/{pricePerHour} | POST   | Set budget per hour  | ABDULRAHMAN |
| 18  | /api/v1/subhall/get/asset/**                                            | GET    | Get subhall assets   | ZYAD        |
| 19  | /api/v1/subhall/add/asset/**                                            | POST   | Add subhall assets   | ZYAD        |

---

## 🎮 Game  

| #   | Endpoint                                          | Method | Description       | Contributor |
| --- | ------------------------------------------------- | ------ | ----------------- | ----------- |
| 20  | /api/v1/game/get                                  | GET    | Get all games     | ABDULRAHMAN |
| 21  | /api/v1/game/add/{hallId}/{subHallId}             | POST   | Add new game      | ABDULRAHMAN |
| 22  | /api/v1/game/update/{hallId}/{subHallId}/{gameId} | PUT    | Update game by ID | ABDULRAHMAN |
| 23  | /api/v1/game/delete/{hallId}/{subHallId}/{gameId} | DELETE | Delete game by ID | ABDULRAHMAN |

---

## 👤 Customer  

| #   | Endpoint                                    | Method | Description                   | Contributor |
| --- | ------------------------------------------- | ------ | ----------------------------- | ----------- |
| 24  | /api/v1/customer/register                   | POST   | Register new customer         | YASIR       |
| 25  | /api/v1/customer/get                        | GET    | Get customer info             | YASIR       |
| 26  | /api/v1/customer/update                     | PUT    | Update customer info          | YASIR       |
| 27  | /api/v1/customer/delete                     | DELETE | Delete customer               | YASIR       |
| 28  | /api/v1/customer/analyse                    | GET    | Analyse customer stats        | YASIR       |
| 29  | /api/v1/customer/cancel/booking/{bookingId} | PUT    | Cancel a booking              | YASIR       |
| 30  | /api/v1/customer/game/analyse               | GET    | Analyse customer games        | ZYAD        |
| 31  | /api/v1/customer/booking/advice             | GET    | Customer booking advice       | ZYAD        |
| 32  | /api/v1/customer/getall                     | GET    | Get all customers (Admin)     | YASIR       |

---

## 📅 Booking  

| #   | Endpoint                                   | Method | Description                | Contributor |
| --- | ------------------------------------------ | ------ | -------------------------- | ----------- |
| 33  | /api/v1/booking/get                        | GET    | Get all bookings           | YASIR       |
| 34  | /api/v1/booking/add/subhall/{subhallId}    | POST   | Add booking to subhall     | YASIR       |
| 35  | /api/v1/booking/update/booking/{bookingId} | PUT    | Update booking by ID       | YASIR       |
| 36  | /api/v1/booking/delete/booking/{bookingId} | DELETE | Delete booking by ID       | YASIR       |
| 37  | /api/v1/booking/initiated/hall/{hallId}    | GET    | Get initiated bookings     | ABDULRAHMAN |
| 38  | /api/v1/booking/approved/hall/{hallId}     | GET    | Get approved bookings      | ABDULRAHMAN |
| 39  | /api/v1/booking/remind-unpaid/{hallId}     | POST   | Remind for unpaid bookings | ABDULRAHMAN |

---

## 🧑‍💼 Owner  

| #   | Endpoint                                   | Method | Description                 | Contributor |
| --- | ------------------------------------------ | ------ | --------------------------- | ----------- |
| 40  | /api/v1/owner/register                     | POST   | Register new owner          | ZYAD        |
| 41  | /api/v1/owner/get                          | GET    | Get owner info              | ZYAD        |
| 42  | /api/v1/owner/update                       | PUT    | Update owner info           | ZYAD        |
| 43  | /api/v1/owner/delete                       | DELETE | Delete owner                | ZYAD        |
| 44  | /api/v1/owner/feedback/hall/{hallId}       | GET    | Get feedback for hall       | YASIR       |
| 45  | /api/v1/owner/feedback/subhall/{subHallId} | GET    | Get feedback for subhall    | YASIR       |
| 46  | /api/v1/owner/cancel/booking/{bookingId}   | PUT    | Cancel a booking            | YASIR       |
| 47  | /api/v1/owner/get-all                      | GET    | Get all owners (Admin only) | ZYAD        |

---

## ⭐ ReviewHall  

| #   | Endpoint                                           | Method | Description              | Contributor |
| --- | -------------------------------------------------- | ------ | ------------------------ | ----------- |
| 48  | /api/v1/review-hall/getAll                         | GET    | Get all hall reviews     | ABDULRAHMAN |
| 49  | /api/v1/review-hall/add/{hallId}                   | POST   | Add new hall review      | ABDULRAHMAN |
| 50  | /api/v1/review-hall/update/{hallId}/{reviewHallId} | PUT    | Update hall review by ID | ABDULRAHMAN |
| 51  | /api/v1/review-hall/delete/{hallId}/{reviewHallId} | DELETE | Delete hall review by ID | ABDULRAHMAN |
| 52  | /api/v1/review-hall/hall/{hallId}/rating           | GET    | Get hall rating          | ABDULRAHMAN |
| 53  | /api/v1/review-hall/get/asset/**                   | GET    | Get hall review assets   | ZIYAD       |
| 54  | /api/v1/review-hall/get                            | GET    | Get hall reviews (Admin) | ZIYAD       |

---

## ⭐ ReviewSubHall  

| #   | Endpoint                                                     | Method | Description               | Contributor |
| --- | ------------------------------------------------------------ | ------ | ------------------------- | ----------- |
| 55  | /api/v1/review-sub-hall/add/{subHallId}                      | POST   | Add new subhall review    | ABDULRAHMAN |
| 56  | /api/v1/review-sub-hall/update/{subHallId}/{reviewSubHallId} | PUT    | Update subhall review     | ABDULRAHMAN |
| 57  | /api/v1/review-sub-hall/delete/{subHallId}/{reviewSubHallId} | DELETE | Delete subhall review     | ABDULRAHMAN |
| 58  | /api/v1/review-sub-hall/get                                  | GET    | Get all subhall reviews   | ABDULRAHMAN |
| 59  | /api/v1/review-sub-hall/get/asset/**                         | GET    | Get subhall review assets | ZIYAD       |

---

## 💳 Payments  

| #   | Endpoint                                      | Method | Description              | Contributor |
| --- | --------------------------------------------- | ------ | ------------------------ | ----------- |
| 60  | /api/v1/payments/pay/for/{bookingId}          | POST   | Pay for booking          | YASIR       |
| 61  | /api/v1/payments/card                         | POST   | Payment via card         | YASIR       |
| 62  | /api/v1/payments/get/status/{paymentId}       | GET    | Get payment status       | YASIR       |
| 63  | /api/v1/payments/get/all                      | GET    | Get all payments         | YASIR        |
| 64  | /api/v1/payments/get/status/filter/{status}   | GET    | Filter payment by status | YASIR        |
| 65  | /api/v1/payments/callback                     | POST   | Payment gateway callback | YASIR        |
| 66  | /api/v1/payments/download/invoice/{bookingId} | GET    | Download invoice PDF     | YASIR        |

---

## 📞 Contact Information  

### 🔗 LinkedIn  

- yasir alateeq
- Ziyad Alghamdi  
- Abdulrahman Lami


For questions or support:  

- **Email:** yasiralateeq178@gmail.com
- **Email:** zyadexec@gmail.com
- **Email:** mr.lamics@gmail.com  

---

