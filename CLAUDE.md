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
nohup mvn spring-boot:run > backend.log 2>&1 &  # Run in background
```

### Frontend (Vue 3 + Vite)
```bash
cd order-ticket-frontend
npm install                  # Install dependencies
npm run dev                  # Start development server on port 5173
npm run build               # Build for production
npm run type-check          # Run TypeScript type checking
npm run lint                # Run ESLint
nohup npm run dev > frontend.log 2>&1 &  # Run in background
```

### Running Both Services
For development, both services need to run simultaneously:
- Backend: http://localhost:8080
- Frontend: http://localhost:5173

## Architecture & Key Components

### Backend Architecture (Spring Boot)
- **Entity Layer**: `Order.java` - JPA entity with status enum (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- **Repository Layer**: `OrderRepository.java` - Custom JPA queries with pagination and filtering using Spring Data JPA
- **Service Layer** (Business Logic):
  - `OrderService.java` - CRUD operations, pagination, search filtering
  - `InvoiceService.java` - PDF generation using iText7
  - `ExportService.java` - Excel export using Apache POI
- **Controller Layer**: RESTful APIs with CORS configured for frontend
- **Data Initialization**: `DataInitializer.java` - Creates 8 sample orders on startup

### Frontend Architecture (Vue 3)
- **Views**: Dashboard (`DashboardView.vue`), Order List (`OrderListView.vue`), Search (`OrderSearchView.vue`), Export (`ExportView.vue`)
- **State Management**: Pinia store (`orderStore.ts`) with reactive state, pagination, and search parameters
- **API Layer**: `orderApi.ts` - Axios client with TypeScript interfaces for type safety
- **Routing**: Vue Router with horizontal navigation menu in `App.vue`
- **UI Framework**: Element Plus components with custom CSS styling

### Data Flow Architecture
1. **Frontend→Backend**: Axios HTTP requests from Vue components → Spring Boot REST controllers
2. **State Management**: Pinia store manages global state, pagination, and API calls
3. **Database**: Spring Data JPA handles database operations with automatic query generation
4. **File Generation**: Separate services for PDF (iText7) and Excel (Apache POI) export

### Key Design Patterns
- **Repository Pattern**: Spring Data JPA repositories with custom query methods
- **Service Layer Pattern**: Business logic separated from controllers
- **Composition API**: Vue 3 reactive composition with `ref()` and `computed()`
- **TypeScript Interfaces**: Strong typing across frontend API layer and data models

## API Endpoints

### Order Management
- `GET /api/orders` - Paginated order list with optional filters (orderNumber, customerName, status, dateRange)
- `GET /api/orders/{id}` - Single order details
- `GET /api/orders/statistics` - Dashboard statistics (totalOrders, pendingOrders, processingOrders, deliveredOrders, cancelledOrders)

### Export Functions
- `GET /api/export/excel` - Excel export of all orders
- `GET /api/export/invoice/{id}` - Single PDF invoice generation
- `GET /api/export/invoices` - Batch PDF invoices

## Database Configuration

Development uses H2 in-memory database with console available at `/h2-console`. Sample data includes 8 orders with different statuses. For production, update `application.properties` to use MySQL connection string.

## CORS & Integration

Backend configured for CORS with frontend at localhost:5173. Both services must run simultaneously for full functionality. If frontend port changes, update `spring.web.cors.allowed-origins` in `application.properties`.

## State Management (Pinia)

The `orderStore.ts` manages:
- **orders**: Current order list
- **statistics**: Dashboard metrics
- **pagination**: currentPage, pageSize, totalElements
- **searchParams**: Filter criteria for order queries
- **loading/error**: UI state management

Store methods handle API calls and state updates reactively.

## UI Styling Conventions

- Element Plus components with custom CSS overrides
- Responsive grid layouts using CSS Grid and Flexbox  
- Color scheme: Primary blue (#409eff), success green, warning orange
- Card-based design with subtle shadows and hover effects
- Chinese UI text throughout the application

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