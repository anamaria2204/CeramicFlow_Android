# CeramicFlow - Project Structure

## Application Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      MainActivity                            │
│                    (Entry Point)                             │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│                    AppNavigation                             │
│              (Navigation Controller)                         │
└─────┬───────────────────┬───────────────────┬───────────────┘
      │                   │                   │
      ▼                   ▼                   ▼
┌──────────┐      ┌──────────────┐     ┌─────────────────┐
│  Login   │──►   │BookingList   │──►  │BookingDetail    │
│  Screen  │      │   Screen     │     │    Screen       │
│  (3p)    │      │   (3p)       │     │    (3p)         │
└────┬─────┘      └──────┬───────┘     └────────┬────────┘
     │                   │                      │
     ▼                   ▼                      ▼
┌──────────┐      ┌──────────────┐     ┌─────────────────┐
│  Login   │      │BookingList   │     │BookingDetail    │
│ViewModel │      │  ViewModel   │     │   ViewModel     │
└────┬─────┘      └──────┬───────┘     └────────┬────────┘
     │                   │                      │
     │                   └───────────┬──────────┘
     ▼                               ▼
┌──────────┐              ┌────────────────────┐
│   Auth   │              │   MockData         │
│Repository│              │   Repository       │
└──────────┘              └────────────────────┘
```

## File Organization

```
CeramicFlow_Android/
│
├── app/
│   ├── build.gradle.kts ........................... Dependencies & Config
│   └── src/main/
│       ├── AndroidManifest.xml .................... App permissions
│       └── java/com/example/ceramicflow_android/
│           │
│           ├── MainActivity.kt .................... App entry point
│           │
│           ├── data/
│           │   ├── model/
│           │   │   ├── Booking.kt ................. Booking data + status enum
│           │   │   ├── CeramicItem.kt ............. Item data + type enum
│           │   │   └── User.kt .................... Auth models
│           │   └── repository/
│           │       ├── AuthRepository.kt .......... JWT authentication
│           │       └── MockDataRepository.kt ...... Mock data management
│           │
│           ├── ui/
│           │   ├── screens/
│           │   │   ├── LoginScreen.kt ............. 🔐 Login UI (3p)
│           │   │   ├── BookingListScreen.kt ....... 📋 Master UI (3p)
│           │   │   └── BookingDetailScreen.kt ..... 📝 Detail UI (3p)
│           │   ├── viewmodel/
│           │   │   ├── LoginViewModel.kt .......... Login logic
│           │   │   ├── BookingListViewModel.kt .... List logic
│           │   │   └── BookingDetailViewModel.kt .. Detail logic
│           │   └── theme/
│           │       ├── Color.kt ................... Color palette
│           │       ├── Theme.kt ................... Material theme
│           │       └── Type.kt .................... Typography
│           │
│           └── navigation/
│               └── AppNavigation.kt ............... Navigation graph
│
├── gradle/
│   └── libs.versions.toml ......................... Dependency versions
│
├── README.md ...................................... Full documentation
└── DEMO.md ........................................ Demo instructions
```

## Data Models

```
┌─────────────────────────────────────────────────────────┐
│                        Booking                          │
├─────────────────────────────────────────────────────────┤
│ - id: String                                            │
│ - clientName: String                                    │
│ - date: String                                          │
│ - timeSlot: String                                      │
│ - status: BookingStatus (enum)                          │
│ - items: MutableList<CeramicItem>                       │
└─────────────────┬───────────────────────────────────────┘
                  │
                  │ contains many
                  ▼
┌─────────────────────────────────────────────────────────┐
│                     CeramicItem                         │
├─────────────────────────────────────────────────────────┤
│ - id: String                                            │
│ - name: String                                          │
│ - type: CeramicType (enum)                              │
│ - quantity: Int                                         │
│ - description: String                                   │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│              BookingStatus (enum)                       │
├─────────────────────────────────────────────────────────┤
│ • PENDING                                               │
│ • IN_PROGRESS                                           │
│ • COMPLETED                                             │
│ • CANCELLED                                             │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│              CeramicType (enum)                         │
├─────────────────────────────────────────────────────────┤
│ • MUG                                                   │
│ • COFFEE_CUP                                            │
│ • PLATE                                                 │
│ • BOWL                                                  │
│ • VASE                                                  │
│ • OTHER                                                 │
└─────────────────────────────────────────────────────────┘
```

## Screen Flows

### 1. Login Flow
```
┌────────────┐
│   Start    │
└─────┬──────┘
      │
      ▼
┌────────────────────┐
│  LoginScreen       │
│  - Username input  │
│  - Password input  │
│  - Login button    │
└─────┬──────────────┘
      │
      ├─► Enter credentials
      │
      ▼
┌────────────────────┐
│  LoginViewModel    │
│  - Validate input  │
│  - Call AuthRepo   │
└─────┬──────────────┘
      │
      ├─► Success ────────┐
      │                   ▼
      │           ┌───────────────┐
      │           │BookingList    │
      │           │Screen         │
      │           └───────────────┘
      │
      └─► Error ──► Show message
```

### 2. Booking List Flow
```
┌────────────────────┐
│  BookingListScreen │
│  - Load bookings   │
└─────┬──────────────┘
      │
      ├─► Tap booking ────┐
      │                   ▼
      │           ┌───────────────────┐
      │           │BookingDetail      │
      │           │Screen             │
      │           └───────────────────┘
      │
      └─► Refresh ──► Reload data
```

### 3. Booking Detail Flow
```
┌────────────────────┐
│BookingDetailScreen │
│  - View items      │
└─────┬──────────────┘
      │
      ├─► Tap + (Add) ───┐
      │                  ▼
      │          ┌──────────────┐
      │          │ AddItemDialog│
      │          │ - Name       │
      │          │ - Type       │
      │          │ - Quantity   │
      │          │ - Description│
      │          └──────┬───────┘
      │                 │
      │                 ▼
      │          Add to booking
      │                 │
      │                 ▼
      │          Refresh list
      │
      └─► Tap 🗑️ (Delete) ──► Remove item
                              │
                              ▼
                        Refresh list
```

## State Management

```
ViewModel Layer
├── Manages UI State
├── Handles Business Logic
└── Communicates with Repositories

StateFlow Pattern:
┌──────────────┐
│ ViewModel    │
│   (State)    │
└──────┬───────┘
       │ emits
       ▼
┌──────────────┐
│  StateFlow   │◄─── Collects ───┐
└──────┬───────┘                 │
       │                         │
       └─► Updates ──►  ┌────────┴──────┐
                        │ Composable UI │
                        └───────────────┘
```

## UI State Classes

```kotlin
// Login States
sealed class LoginUiState {
    object Idle
    object Loading
    data class Success(val user: User)
    data class Error(val message: String)
}

// Booking List States
sealed class BookingListUiState {
    object Loading
    data class Success(val bookings: List<Booking>)
    data class Error(val message: String)
}

// Booking Detail States
sealed class BookingDetailUiState {
    object Loading
    data class Success(val booking: Booking)
    data class Error(val message: String)
}
```

## Navigation Routes

```
"login" ──────────────────────────► LoginScreen
                                          │
                                          │ onLoginSuccess
                                          ▼
"booking_list" ───────────────────► BookingListScreen
                                          │
                                          │ onBookingClick(id)
                                          ▼
"booking_detail/{bookingId}" ─────► BookingDetailScreen
                                          │
                                          │ onNavigateBack
                                          ▼
                                    BookingListScreen
```

## Dependencies

```toml
[Key Libraries]
├── androidx.compose.* ................ UI Framework
├── androidx.navigation ............... Navigation
├── androidx.lifecycle ................ ViewModel & StateFlow
├── retrofit .......................... Future REST API
└── gson .............................. JSON parsing

[Versions]
├── Compose BOM: 2024.09.00
├── Navigation: 2.7.5
├── Lifecycle: 2.6.2
└── Kotlin: 2.0.21
```

## Assessment Mapping

```
┌──────────────────────────────────────────────────────┐
│              Homework Requirements                   │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ✅ Login Page (3p)                                 │
│     └─ LoginScreen.kt                               │
│     └─ AuthRepository.kt (JWT mock)                 │
│     └─ LoginViewModel.kt                            │
│                                                      │
│  ✅ List Page (3p)                                  │
│     └─ BookingListScreen.kt (Master)                │
│     └─ MockDataRepository.kt                        │
│     └─ BookingListViewModel.kt                      │
│                                                      │
│  ✅ Edit Page (3p)                                  │
│     └─ BookingDetailScreen.kt (Detail)              │
│     └─ Add/Delete operations                        │
│     └─ BookingDetailViewModel.kt                    │
│                                                      │
│  ✅ Master-Detail UI                                │
│     └─ Navigation flow implemented                  │
│                                                      │
│  ✅ REST Service (Ready)                            │
│     └─ Retrofit dependency added                    │
│     └─ Repository pattern for easy API swap         │
│                                                      │
│  ✅ JWT Authentication                              │
│     └─ Mock JWT token generation                    │
│     └─ Token storage in AuthRepository              │
│                                                      │
└──────────────────────────────────────────────────────┘
```
