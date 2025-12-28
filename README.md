# 🌟 Infinity-Mart

**Infinity-Mart** is a full-featured **e-commerce web application** built with **Spring Boot** for the backend and **React (Vite) with Redux** for the frontend. The platform provides **end-to-end e-commerce functionality**, including authentication, role-based authorization, product management, cart and order processing, address management, analytics, and secure online payments via **Stripe**.  

Infinity-Mart follows a **modern, scalable architecture** with a clear separation between backend services and frontend presentation, enabling maintainability and production-level scalability.

---

## 🏗️ Project Architecture

The project is divided into two independent modules:

### Backend (Spring Boot)
- Handles business logic, database operations, authentication & authorization
- Exposes **RESTful APIs** secured with **JWT-based authentication stored in HTTP cookies**
- Implements **role-based access control** for Admin, Seller, and User roles
- Provides APIs for products, categories, cart, orders, addresses, and analytics

### Frontend (React + Redux)
- Multi-page application (MPA) consuming backend APIs
- Responsive and interactive UI for **customers, sellers, and administrators**
- Features include product listing with **search, sort, filter, and pagination**, cart management, checkout, and dashboards
- Uses **Redux** for centralized state management and **Vite** for fast development

---

## 🔐 Authentication & Authorization

- **JWT Cookie-Based Authentication:**  
  Users log in and receive a **JWT token stored in a secure HTTP-only cookie**, preventing XSS attacks.  
- **Role-Based Access Control (RBAC):**  
  - **Admin:** Full access to all system operations including analytics, products, orders, categories, and seller management  
  - **Seller:** Manage own products and orders  
  - **User:** Browse products, manage cart, checkout, and track personal orders  

This approach ensures **secure API access** and **session persistence** without storing sensitive tokens in local storage.

---

## 🖥️ Frontend Overview

### Pages & Features

#### Common Pages (Accessible to all roles)
- Homepage  
- Login & Registration  
- Contact Us  
- About Us  
- Cart Page  
- Checkout Flow:  
  - Address Page → Order Summary → Payment Page with Stripe  

#### Product Pages
- Product Listing with **search, filter, sort, and pagination**
- Product Details Page

#### Admin Dashboard
1. **Dashboard:** Revenue, total orders, analytics  
2. **Products:** CRUD operations for all products  
3. **Orders:** Manage all orders (CRUD)  
4. **Sellers:** Manage sellers (CRUD)  
5. **Categories:** Manage product categories (CRUD)

#### Seller Dashboard
1. **Products:** Manage own products (CRUD)  
2. **Orders:** Manage orders for their products (CRUD)

#### User Shopping & Checkout
- Add/remove items from cart  
- Manage addresses  
- View order summary  
- Complete payment via **Stripe**  
- Order confirmation  

---

## ⚙️ Backend Overview

### Key Modules
- **Authentication & Authorization:** JWT cookie-based authentication with RBAC  
- **Product Management:** CRUD operations with image upload; accessible by Admin and Seller roles  
- **Category Management:** CRUD operations; Admin only  
- **Cart Management:** Add, update, remove items; linked to authenticated users  
- **Order Management:** Create orders, track history, view order details  
- **Address Management:** Full CRUD operations for delivery addresses  
- **Analytics (Admin):** Revenue metrics, total orders, system insights  
- **Payment Integration:** Stripe payment gateway for secure checkout  
- **Pagination & Sorting:** Optimized API responses for large datasets  
- **Persistence Layer:** JPA/Hibernate ORM with MySQL database  

### Backend Controllers
- `AuthController` – JWT cookie-based authentication  
- `ProductController` – Product CRUD operations  
- `CategoryController` – Category CRUD operations  
- `CartController` – Cart operations  
- `OrderController` – Order CRUD operations  
- `AddressController` – User addresses management  
- `AnalyticsController` – Admin insights  

### Role-Based Access
| Role   | Permissions |
|--------|-------------|
| Admin  | Full access to products, orders, sellers, categories, and analytics |
| Seller | Manage own products and related orders |
| User   | Browse products, manage cart, checkout, track personal orders |

---

## 💳 Checkout & Payment Flow

1. User adds products to cart  
2. Cart data managed via **Redux**  
3. User selects or adds a delivery address  
4. Order summary displays cart and address details  
5. Stripe processes payment securely  
6. Order is created and confirmed upon successful payment  


