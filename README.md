PlantPal
An Android app that gamifies houseplant care through interactive digital avatars
PlantPal transforms plant maintenance from a chore into an engaging experience by combining Tamagotchi-style interactions with AI-powered plant identification and personalized care recommendations.

Features
Current Implementation (MVP)

User Authentication: Sign up and log in with Firebase Authentication
Photo Capture: Take photos of your plants using device camera
Plant Management: Add plants with custom names and notes
Secure Storage: Plant photos stored locally with Firebase Firestore integration
Modern UI: Material Design 3 with Jetpack Compose

Planned Features (Alpha/Beta)

AI Plant Recognition: Identify plant species using Plant.id API
Digital Avatars: Cute, customizable plant avatars that reflect real-world care
Smart Reminders: Push notifications for watering, fertilizing, and sunlight needs
Care Tracking: Monitor watering history, growth milestones, and health status
Gamification: Earn badges and maintain care streaks
Plant Library: Educational tips and care profiles for different species
Multi-plant Dashboard: Manage all your plants in one place


Tech Stack

Platform: Android (minSdk 24, targetSdk 36)
Language: Kotlin
UI Framework: Jetpack Compose with Material 3
Architecture: MVVM with StateFlow
Backend: Firebase

Authentication
Firestore (NoSQL database)
Storage (planned for cloud photo backup)


Build System: Gradle with Kotlin DSL
Camera: AndroidX Camera with FileProvider


Design Inspiration
PlantPal's UI draws inspiration from:

Apple Health: Card-based dashboard with pinned highlights
Tamagotchi: Nostalgic digital pet interactions
Material You: Dynamic theming and modern Android design patterns


Permissions
The app requires the following permissions:

CAMERA: To capture plant photos
INTERNET: For Firebase authentication and Firestore sync
ACCESS_NETWORK_STATE: To check network connectivity


Development Roadmap
MVP

User authentication system
Plant photo capture
Basic plant profile creation
Local photo storage
Firebase integration

Alpha 

AI plant identification (Plant.id API)
Avatar customization
Custom care reminders
Progress tracking and badges

Beta 

Calendar sync
AI early warning system
Group/household accounts
Voice assistant integration
AR overlay for care tips

Version 2.0 

Smart home sensor integration
Avatar marketplace
Community features
AI chat companion
Web dashboard


Team

Backend: Devin McLaughlin
Frontend: Ethan LeFebvre
UX Design: Joshua Lindgren


License
This project is part of a Full Sail University capstone course.


Acknowledgments

Plant.id API for plant recognition (planned integration)
Firebase for backend infrastructure
Full Sail University for project guidance
Android Developer Community for excellent documentation
