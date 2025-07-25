# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a full-stack order management system with frontend/backend separation:
- **Backend**: Java Spring Boot 3.2.0 with REST API
- **Frontend**: Vue 3 with TypeScript, Element Plus UI, and Pinia state management
- **Database**: H2 in-memory for development, MySQL-ready for production
- **Features**: Order querying, PDF invoice generation, Excel export

## Development Commands

### Backend (Spring Boot)
```bash
cd order-ticket-backend
mvn spring-boot:run          # Start development server on port 8080
mvn clean compile            # Compile the project
mvn test                     # Run tests
```

### Frontend (Vue 3 + Vite)
```bash
cd order-ticket-frontend
npm install                  # Install dependencies
npm run dev                  # Start development server on port 5173
npm run build               # Build for production
npm run type-check          # Run TypeScript type checking
npm run lint                # Run ESLint
```

## Architecture & Key Components

### Backend Structure
- **Entity Layer**: `Order.java` - JPA entity with status enum (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- **Repository Layer**: `OrderRepository.java` - Custom JPA queries with pagination and filtering
- **Service Layer**: 
  - `OrderService.java` - Business logic for order operations
  - `InvoiceService.java` - PDF generation using iText7
  - `ExportService.java` - Excel export using Apache POI
- **Controller Layer**: RESTful APIs with CORS configured for frontend at localhost:5173
- **Data Initialization**: `DataInitializer.java` - Creates sample data on startup

### Frontend Structure
- **Views**: Dashboard, Order List, Order Search, Export functionality
- **State Management**: Pinia store (`orderStore.ts`) for centralized order data
- **API Layer**: `orderApi.ts` - Axios-based API client
- **Routing**: Vue Router with navigation menu

### API Endpoints
- `GET /api/orders` - Paginated order list with optional filters
- `GET /api/orders/{id}` - Single order details
- `GET /api/orders/statistics` - Dashboard statistics
- `GET /api/export/excel` - Excel export
- `GET /api/export/invoice/{id}` - Single PDF invoice
- `GET /api/export/invoices` - Batch PDF invoices

## Database Configuration

Development uses H2 in-memory database with sample data auto-generated on startup. For production, update `application.properties` to use MySQL connection string.

## CORS & Integration

Backend is configured for CORS with frontend running on port 5173. If frontend port changes, update the `spring.web.cors.allowed-origins` property in `application.properties`.

## Key Dependencies

### Backend
- Spring Boot 3.2.0 (Web, Data JPA)
- iText7 for PDF generation
- Apache POI for Excel export
- H2/MySQL for database

### Frontend  
- Vue 3 with Composition API
- Element Plus UI components
- Pinia for state management
- Axios for HTTP requests
- TypeScript for type safety

## Development Environment

- **Java**: Requires JDK 17+
- **Node.js**: Project uses Node v18.20.5 (compatibility issues with newer versions)
- **Build Tools**: Maven for backend, Vite for frontend