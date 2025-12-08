# CeramicFlow - Demo Instructions

## Quick Start Guide

### 1. Login Screen
When you first launch the app, you'll see the **Login Screen**:
- Enter one of the demo credentials:
  - **admin** / **admin123**
  - **user** / **user123**  
  - **maria** / **maria123**
- The password field has a show/hide toggle
- Press "Login" or hit Enter on keyboard
- A loading spinner shows during authentication

### 2. Booking List (Master Page)
After successful login, you'll see the **Booking List Screen**:
- Displays 5 sample bookings with:
  - Client name (e.g., "Maria Popescu", "Ion Ionescu")
  - Date and time slot
  - Colored status badges (Pending/In Progress/Completed/Cancelled)
  - Number of ceramic items
- Tap the refresh icon to reload bookings
- **Tap any booking card** to view its details

### 3. Booking Details (Detail Page)
When you tap a booking, you'll see the **Booking Detail Screen**:

#### View Information
- Top card shows booking summary (client, date, time, status)
- Below shows list of ceramic items with:
  - Item name (e.g., "Cana mare", "Ceasca cafea")
  - Type (MUG, COFFEE_CUP, PLATE, BOWL, VASE, OTHER)
  - Quantity
  - Description (optional)

#### Add Items
1. Tap the **floating + button** (bottom right)
2. Fill in the dialog:
   - **Item Name**: e.g., "Farfurie decorativa"
   - **Type**: Select from dropdown (Mug, Coffee Cup, Plate, Bowl, Vase, Other)
   - **Quantity**: Enter number
   - **Description**: Optional details
3. Tap "Add"
4. Success message appears and list updates

#### Delete Items
1. Tap the **trash icon** next to any item
2. Item is removed immediately
3. Success message confirms deletion

#### Navigation
- Tap the **back arrow** (top left) to return to booking list
- Android back button also works

## Testing the App

### Test Flow 1: View All Bookings
1. Login with `admin` / `admin123`
2. Scroll through all 5 bookings
3. Notice different statuses and dates
4. Tap refresh icon to reload

### Test Flow 2: Manage Items
1. Login with any credentials
2. Tap on "Maria Popescu" booking
3. See existing items (Cana mare, Ceasca cafea)
4. Tap + to add new item:
   - Name: "Bol ceramica"
   - Type: BOWL
   - Quantity: 3
   - Description: "Pentru salata"
5. See the new item appear in list
6. Delete any item by tapping trash icon
7. Go back and select another booking

### Test Flow 3: Error Handling
1. On login, try empty username/password (error shown)
2. Try wrong credentials (error message appears)
3. Navigate to detail page and back multiple times
4. Add items with different types and quantities

## Features Demonstrated

### Authentication (3 points)
✅ JWT mock authentication
✅ Login validation
✅ Loading states
✅ Error handling

### Master Page (3 points)
✅ List of all bookings
✅ Status indicators
✅ Navigation to details
✅ Refresh functionality

### Detail Page (3 points)
✅ Display booking information
✅ List ceramic items
✅ Add new items (dialog with validation)
✅ Delete items
✅ Real-time updates

## Sample Data Overview

### Bookings
1. **Maria Popescu** - 2024-12-05, 10:00-12:00 (Pending)
   - Cana mare x2, Ceasca cafea x4
2. **Ion Ionescu** - 2024-12-06, 14:00-16:00 (In Progress)
   - Farfurie x6, Bol x3
3. **Ana Marinescu** - 2024-12-07, 09:00-11:00 (Pending)
   - Vaza x1, Cana mica x2
4. **Gheorghe Vasilescu** - 2024-12-08, 15:00-17:00 (Completed)
   - Set cafea x6, Cana mare x1
5. **Elena Dumitrescu** - 2024-12-09, 11:00-13:00 (Pending)
   - Farfurii desert x8, Boluri cereale x4

### Ceramic Item Types
- **MUG**: Coffee mugs
- **COFFEE_CUP**: Espresso/tea cups
- **PLATE**: Dinner/dessert plates
- **BOWL**: Soup/cereal bowls
- **VASE**: Decorative vases
- **OTHER**: Custom items

## Architecture Highlights

### Clean Separation
- **Data Layer**: Models + Repositories (mock data)
- **ViewModel Layer**: Business logic + state management
- **UI Layer**: Composable screens

### State Management
- StateFlow for reactive updates
- Sealed classes for UI states
- Coroutines for async operations

### Navigation
- Type-safe navigation with arguments
- Proper back stack management
- Deep linking ready

## Next Steps (Future Enhancement)

This implementation is ready for REST API integration:
1. Replace `MockDataRepository` with `ApiRepository`
2. Use Retrofit to call real backend
3. Replace mock JWT with real tokens
4. Add SharedPreferences for token persistence
5. Implement proper error handling for network failures

## Notes
- All operations have simulated network delays (200-800ms)
- Data persists only in memory (resets on app restart)
- Romanian sample data for realistic context
- Material Design 3 for modern UI
- Supports Android API 25+ (Android 7.1+)
