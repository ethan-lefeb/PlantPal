Purpose

PlantPal is a mobile application that gamifies houseplant care through digital avatars that respond to real-world plant maintenance. By combining nostalgic Tamagotchi-style interactions with AI-powered plant identification and personalized care recommendations, PlantPal transforms plant care from a chore into an engaging, educational experience that helps users successfully maintain their houseplants.

Goal

Many plant enthusiasts struggle with inconsistent care routines, leading to plant death and discouragement. PlantPal addresses this by providing reminders, guidance, and gamified feedback to promote consistent care habits.

Existing Solution

Current plant care apps like PlantIn, Blossom, and Vera focus primarily on identification or basic reminders but lack engaging, interactive elements that sustain long-term user engagement.

Definitions

Digital Avatar: Virtual representation of a user's real plant that reflects health status.

Plant.id API: Third-party service for AI-powered plant species identification.

Care Profile: Customized watering, sunlight, and fertilizer schedule for specific plant species.

Overall Description
Product Perspective

PlantPal is a mobile-first app that combines gamification with AI-powered plant care. Users interact through touchscreen gestures, voice input, and keyboard input for naming or notes.

User Interface Hardware

Primary: Touchscreen

Additional: Voice input (optional), Keyboard input for text entry

Hardware Interfaces & Memory Constraints

Device Requirements: Smartphone or tablet (Android)

Additional Hardware: Camera (for plant identification), Microphone (optional), Internet connection

Software Interfaces

Operating System: Android 12+

Third-Party Integrations:

Native camera apps

System notifications

Calendar integration

Social media sharing

Library Dependencies

Framework: Android SDK (Java/Kotlin)

UI: Jetpack libraries (Lifecycle, ViewModel, LiveData), Material Components

Networking: Retrofit2 / OkHttp

Image Handling: Glide or Coil

Local Storage: Room (SQLite wrapper)

Database

Cloud: Firebase Firestore (user accounts, plant profiles, care logs)

Offline: Room (care reminders, avatar state, last synced data)

3rd Party Data and APIs

Plant.id API – AI-powered plant species identification

Firebase Authentication – Account management

Firebase Cloud Messaging – Push notifications
