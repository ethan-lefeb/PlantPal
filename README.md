PlantPal 🌱
Introduction
PlantPal is a mobile application designed to gamify houseplant care and help users keep their plants alive and thriving. Inspired by the nostalgia of 90s Tamagotchis, PlantPal transforms plant maintenance from a chore into an engaging, interactive experience. Each real-life plant gets a cute digital avatar that responds to how well you care for it—water your plant and watch the avatar perk up; forget to water it and see it droop. By combining AI-powered plant identification with personalized care reminders and gamification, PlantPal makes plant parenting accessible and fun for everyone.

Features
Core Features (MVP)

User Authentication: Create an account and securely log in to access your plant collection
Plant Photo Capture: Add new plants by taking photos directly within the app
Digital Plant Avatars: Each plant gets a unique digital representation
Plant Profile Management: Store plant information including common name, scientific name, photos, and personal notes
Care Logging: Track watering, fertilizing, and other plant care activities
Firebase Integration: Secure cloud storage for user data and plant profiles

Planned Features (In Development)

AI Plant Recognition: Identify plant species from photos using Plant.id or Google Vision API
Personalized Care Profiles: Automated watering schedules based on plant species
Smart Reminders: Push notifications for watering, sunlight rotation, and fertilizing
Avatar Health System: Avatar mood/health reacts to real-life care patterns
Achievement System: Earn badges for maintaining healthy plant streaks
Multi-Plant Dashboard: Manage multiple plants with at-a-glance health status
Social Sharing: Share plant avatars and achievements on social media


Technologies
Development Framework

Platform: Android (Native)
Language: Kotlin
UI Framework: Jetpack Compose
Minimum SDK: 24 (Android 7.0 Nougat)
Target SDK: 36 (Android 14+)

Backend & Cloud Services

Firebase Authentication: User account management
Firebase Firestore: NoSQL cloud database for plant profiles and user data
Firebase Storage: Cloud storage for plant photos (planned)

Architecture & Libraries

Navigation: Jetpack Navigation Compose
Lifecycle: AndroidX Lifecycle & ViewModel
Image Handling: Android Camera API with FileProvider
Async Operations: Kotlin Coroutines & Flow

Planned API Integrations

Plant.id API: AI-powered plant species identification
Firebase Cloud Messaging: Push notifications for care reminders


Installation
For End Users

Download the APK (when available via release page)
Enable Installation from Unknown Sources:

Go to Settings → Security → Unknown Sources
Toggle on to allow installation


Install PlantPal: Open the downloaded APK and follow the installation prompts
Create an Account: Launch the app and sign up with your email
Add Your First Plant: Tap the "+" button and take a photo of your plant

System Requirements

Android device running Android 7.0 (Nougat) or higher
Camera (optional but recommended)
Internet connection for cloud sync


Development Setup
Prerequisites

Android Studio: Koala Feature Drop | 2024.1.2 or later
JDK: Version 11 or higher
Gradle: 8.13 
Firebase Project: Required for authentication and database

Initial Setup

Clone the Repository

bash   git clone https://github.com/yourusername/plantpal.git
   cd plantpal

Open in Android Studio

Launch Android Studio
Select "Open an Existing Project"
Navigate to the cloned plantpal directory


Firebase Configuration

Create a new Firebase project at Firebase Console
Add an Android app to your Firebase project
Package name: com.example.plantpal
Download google-services.json
Place google-services.json in the app/ directory
Enable Authentication (Email/Password) in Firebase Console
Enable Cloud Firestore in Firebase Console


Sync Gradle

Android Studio will prompt you to sync Gradle
Click "Sync Now" or use File → Sync Project with Gradle Files


Build Configuration

The project uses Gradle 8.13 (defined in gradle-wrapper.properties)
Build variants: Debug and Release
Compile SDK: 36
Min SDK: 24


Run the App

Connect an Android device or launch an emulator (API 24+)
Click the green "Run" button or press Shift + F10
Select your target device


License
This project is licensed under the MIT License.
MIT License

Copyright (c) 2025 PlantPal Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

Contributors
Development Team

Devin McLaughlin - UX/UI Design
Joshua Lindgren - Frontend Development 
Ethan LeFebvre - Backend Development

Maintenance
This project is actively maintained by the PlantPal development team as part of the Full Sail University Capstone program.

Project Status
Completed Milestones

✅ User authentication (sign up/login)
✅ Firebase Firestore integration
✅ Camera integration for plant photos
✅ Basic plant profile storage
✅ Navigation system with bottom tab bar
✅ Local photo storage with FileProvider

In Progress

🔄 AI plant identification (Plant.id API integration)
🔄 Avatar generation system
🔄 Care reminder notifications
🔄 Plant health tracking

Upcoming (Beta)

📋 Multi-plant dashboard with health indicators
📋 Achievement/badge system
📋 Social sharing features
📋 Smart home sensor integration

Roadmap
Version 1.0 (Target: TBD)

Full AI plant identification
Automated care schedules
Push notification system
Avatar health system with animations
Achievement badges

Version 2.0 (Future)

Smart home sensor integration
AR plant overlay mode
Community features (forums, plant trading)
Web dashboard companion app
Marketplace for digital avatar customization

Acknowledgments
Special thanks to:

Full Sail University for project guidance
Firebase for backend infrastructure
The Android developer community for Jetpack Compose resources
Plant.id for planned API integration
