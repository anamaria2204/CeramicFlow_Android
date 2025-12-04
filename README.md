# CeramicFlow Android - Homework Implementation

## Overview
CeramicFlow is a ceramic course management application that implements a master-detail user interface with JWT authentication and mock data storage.

## Features Implemented

### ✅ Authentication (3p)
- **Login Page** with username and password fields
- **JWT Mock Authentication** using AuthRepository
- Password visibility toggle
- Loading state during authentication
- Error handling with user-friendly messages
- Demo credentials:
  - `admin` / `admin123`
  - `user` / `user123`
  - `maria` / `maria123`

### ✅ Master-Detail UI (6p)

#### Master Page - Booking List (3p)
- Displays all ceramic course bookings
- Shows client name, date, time slot, and status
- Color-coded status chips (Pending, In Progress, Completed, Cancelled)
- Pull to refresh functionality
- Click on booking to navigate to details
- Loading and error states with retry option

#### Detail Page - Booking Details (3p)
- Shows booking information header
- Lists all ceramic items for the booking
- **Add functionality**: Floating action button opens dialog to add new items
  - Item name input
  - Type selection (MUG, COFFEE_CUP, PLATE, BOWL, VASE, OTHER)
  - Quantity input
  - Optional description
- **Delete functionality**: Each item has a delete button
- Real-time updates after add/delete operations
- Snackbar notifications for operation success/failure
- Back navigation to booking list

## Architecture

### Data Models
- **User**: User information with ID, username, and email
- **Booking**: Represents a ceramic course booking with client info, date, time, status
- **CeramicItem**: Represents ceramic items (mugs, cups, etc.) with type, quantity, description
- **LoginRequest/Response**: Authentication data structures

### Repositories
- **AuthRepository**: Handles authentication with mock JWT token generation
- **MockDataRepository**: Manages mock data with simulated network delays
  - Pre-populated with 5 sample bookings
  - CRUD operations for ceramic items
  - Simulates async operations with coroutines

### ViewModels
- **LoginViewModel**: Manages login state and authentication flow
- **BookingListViewModel**: Handles booking list data and loading states
- **BookingDetailViewModel**: Manages booking details and item operations

### UI Layer
- **LoginScreen**: Material 3 login form with validation
- **BookingListScreen**: Master screen with scrollable booking list
- **BookingDetailScreen**: Detail screen with item management
- **AppNavigation**: Navigation graph with route management

## Technology Stack
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Design system
- **Navigation Compose**: Screen navigation
- **ViewModel & StateFlow**: State management
- **Coroutines**: Asynchronous operations
- **Retrofit & Gson**: Prepared for future REST integration

## Mock Data
The app comes with pre-populated mock data:
- 5 sample bookings for different clients
- Each booking contains 2 ceramic items
- Various types of ceramic items (mugs, cups, plates, bowls, vases)
- Different booking statuses

## Project Structure
```
app/src/main/java/com/example/ceramicflow_android/
├── data/
│   ├── model/
│   │   ├── Booking.kt          # Booking data class and status enum
│   │   ├── CeramicItem.kt      # Ceramic item data class and type enum
│   │   └── User.kt             # User, LoginRequest, LoginResponse
│   └── repository/
│       ├── AuthRepository.kt   # Authentication with JWT mock
│       └── MockDataRepository.kt # Mock data management
├── ui/
│   ├── screens/
│   │   ├── LoginScreen.kt      # Login page (3p)
│   │   ├── BookingListScreen.kt # Master page (3p)
│   │   └── BookingDetailScreen.kt # Detail page (3p)
│   ├── viewmodel/
│   │   ├── LoginViewModel.kt
│   │   ├── BookingListViewModel.kt
│   │   └── BookingDetailViewModel.kt
│   └── theme/                  # Material theme configuration
├── navigation/
│   └── AppNavigation.kt        # Navigation graph
└── MainActivity.kt             # App entry point
```

## How to Run
1. Open project in Android Studio
2. Sync Gradle dependencies
3. Run on emulator or device (API 25+)
4. Use demo credentials to login:
   - Username: `admin` or `user` or `maria`
   - Password: `admin123` or `user123` or `maria123`

## Implementation Highlights

### Clean Architecture
- Separation of concerns: Data, Domain, UI layers
- Repository pattern for data access
- ViewModel for business logic and state management
- Composable functions for UI

### State Management
- StateFlow for reactive UI updates
- Sealed classes for UI state representation
- Loading, Success, and Error states handled consistently

### User Experience
- Smooth navigation with back stack management
- Loading indicators during operations
- Error messages with retry options
- Success notifications for operations
- Material Design 3 components
- Responsive layout

### Future Enhancements (Ready for REST API)
The architecture is prepared for REST API integration:
- Retrofit already added as dependency
- Repository pattern abstracts data source
- Easy to replace mock repository with API calls
- JWT token management in place
- Error handling structure ready for network errors

## Assessment Criteria Met
✅ **Login page (3p)**: Full JWT authentication with mock backend
✅ **List page (3p)**: Master screen with all bookings, status indicators, navigation
✅ **Edit page (3p)**: Detail screen with add/delete operations for ceramic items

## Notes
- Mock data simulates network delays (200-800ms) for realistic behavior
- JWT tokens are generated with mock format: `mock.jwt.token.{username}.{timestamp}`
- All CRUD operations work with in-memory data
- Romanian sample data for realistic local context
