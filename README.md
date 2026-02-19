# 🐐 Goatly – Kotlin App

Mobile application developed in Kotlin for the course **Construcción de Aplicaciones Móviles**.

Goatly is a LinkedIn-style mobile platform designed for **occasional job opportunities at Universidad de los Andes**. It centralizes job postings, applications, and status tracking, replacing the current email-based and low-visibility system.

---

## 📌 About

The problem Goatly addresses is the inefficient and poorly communicated process for occasional jobs at the university. Currently:

- Job postings have low visibility.
- Applications are handled through scattered platforms and mass emails.
- Students lack transparency about their application status.
- The overall process is disorganized and manual.

The Kotlin Student App provides:

- A centralized job feed with category filtering.
- A structured application system with one-tap apply.
- Real-time application status tracking.
- A clean and intuitive native Android experience.

---

## 🎯 Objective

To improve visibility, organization, and transparency in the occasional job application process at Universidad de los Andes by delivering a **native Android experience built with Kotlin**.

This project focuses exclusively on:

- The **Student Flow**
- Clean and scalable Android architecture
- Modern development practices

> Staff features (creating offers, reviewing applications) are implemented separately in the Flutter project.

---

## ✨ Features (MVP)

### 👩‍🎓 Student Flow

- User authentication (Login / Register)
- Browse job feed
- Filter job opportunities by category
- View job details
- Apply to jobs
- Track application status (Pending / Accepted / Rejected)
- View application history with summary stats
- Student profile with career information

---

## 🏗 Project Structure

The project follows an **MVVM architecture** inspired by Clean Architecture principles, ensuring clear separation of concerns.

```
app/
├── data/
│   ├── mock/
│   │   └── MockDataSource.kt
│   ├── model/
│   │   ├── UserModel.kt
│   │   ├── OfferModel.kt
│   │   └── ApplicationModel.kt
│   └── repository/
│       ├── AuthRepository.kt
│       ├── OfferRepository.kt
│       ├── ApplicationRepository.kt
│       ├── MockAuthRepository.kt
│       ├── MockOfferRepository.kt
│       ├── MockApplicationRepository.kt
│       └── RepositoryProvider.kt
│
├── ui/
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   ├── StudentLoginScreen.kt
│   │   └── StudentRegisterScreen.kt
│   ├── home/
│   │   ├── HomeViewModel.kt
│   │   ├── StudentHomeScreen.kt
│   │   ├── OfferDetailViewModel.kt
│   │   └── OfferDetailScreen.kt
│   ├── applications/
│   │   ├── ApplicationsViewModel.kt
│   │   └── MyApplicationsScreen.kt
│   ├── profile/
│   │   └── StudentProfileScreen.kt
│   ├── navigation/
│   │   ├── Routes.kt
│   │   └── StudentShell.kt
│   └── theme/
│       └── Theme.kt
│
└── MainActivity.kt
```

### Architectural Principles

- MVVM (Model–View–ViewModel)
- Repository Pattern
- Unidirectional Data Flow
- Separation of UI and business logic
- Single Source of Truth
- Scalable feature-based modularization

---

## 🎨 Design System

The application shares the **same visual design system as the Flutter Staff app**, ensuring a consistent Goatly brand experience across both projects.

- Primary Color: `#F2B705` (Goatly Yellow)
- Dark Text: `#1F2328`
- Background: `#F3F2EF`
- Surface: `#FFFFFF`
- Border: `#E6E6E6`
- Success: `#1A7F37`
- Danger: `#D1242F`

Reusable components:

- Job offer cards
- Status chips
- Filter chips by category
- Text fields
- Bottom navigation shell

All styles are centralized in `Theme.kt` to ensure visual consistency.

---

## 🔄 State Management

The application uses:

- **ViewModel** for state handling
- **StateFlow** for reactive UI updates
- Immutable UI state models
- Unidirectional data flow

UI components observe state, while business logic resides in ViewModels and Repositories.

---

## 🔌 Backend Integration Strategy

The frontend is developed using a **mock-first approach**.

1. Mock repositories simulate backend responses.
2. UI flows and state management are validated.
3. Mock services will later be replaced with real API integrations.

To swap mock for real API, only `RepositoryProvider.kt` needs to change — no ViewModels or screens are affected.

### Planned API Endpoints

- `POST /auth/login`
- `POST /auth/register`
- `GET /offers`
- `GET /offers/{id}`
- `POST /applications`
- `GET /applications?studentId=...`

---

## 🚀 Getting Started

### 1️⃣ Clone the repository

```
git clone https://github.com/Moviles202620/Kotlin-G25.git
```

### 2️⃣ Open in Android Studio

Open the project using the latest stable version of Android Studio.

### 3️⃣ Run the app

```
Shift + F10
```

---

## 🧠 Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **ViewModel**
- **StateFlow**
- **Navigation Compose**
- **Kotlin Coroutines**
- **Material 3**

---

## 🧪 Development Guidelines

- Follow MVVM structure strictly.
- Keep Composables small and reusable.
- Avoid business logic inside UI files.
- Maintain consistent naming conventions.
- All new features must be developed in separate branches.
- Keep commit history clean and descriptive.

---

## 👥 Team

Kotlin Team – G25  
Construcción de Aplicaciones Móviles  
Universidad de los Andes

---

## 📚 Course Context

This project is part of the academic development process for:

**Construcción de Aplicaciones Móviles – 2026-10**  
Universidad de los Andes

---

## 📌 Status

🚧 In active development – MVP Phase
