# Quiz Game

An educational quiz game for Android built with Jetpack Compose.

## Features

### Quiz System
- **Progressive Quiz Levels**: Users can only play the next quiz after passing the current one
- **Quiz Menu**: Dedicated screen showing all available quiz levels with completion status
- **Level Progression**: 3 difficulty levels (Beginner, Intermediate, Advanced)
- **Completion Tracking**: Visual indicators for completed, available, and locked quizzes

### Game Features
- **Daily Tasks**: Complete daily challenges to earn rewards
- **Achievement System**: Track progress and unlock achievements
- **Shop**: Spend earned coins on items
- **Profile**: View statistics and progress
- **Settings**: Customize notifications and preferences
- **Streak System**: Maintain daily playing streaks

### Quiz Levels
1. **Beginner Level**: Simple questions for young learners (unlocked at player level 1)
2. **Intermediate Level**: Medium difficulty questions (unlocked at player level 3)
3. **Advanced Level**: Complex questions (unlocked at player level 5)
4. **Expert Level**: Specialized questions for experts (unlocked at player level 7)
5. **Master Level**: Challenging questions for masters (unlocked at player level 10)
6. **Grand Master Level**: Ultimate questions for champions (unlocked at player level 15)

### Quiz Menu Features
- **Progress Tracking**: Shows completion progress with visual indicators
- **Level Status**: 
  - ✅ Completed levels (green with checkmark)
  - ▶️ Available levels (can be played)
  - 🔒 Locked levels (require previous level completion)
- **Progression Logic**: Users must complete the previous quiz to unlock the next one
- **Visual Feedback**: Clear indication of what can be played and what's locked

## Navigation Flow
1. **Home Screen** → "Play Quiz" button
2. **Quiz Menu Screen** → Select available quiz level
3. **Quiz Screen** → Answer questions
4. **Quiz Result Screen** → See results and return to menu
5. **Back to Quiz Menu** → Continue with next available quiz

## Technical Implementation

### Quiz Menu Screen (`QuizMenuScreen.kt`)
- Displays all available quiz levels
- Shows completion status for each level
- Implements progression logic (can only play next after completing current)
- Visual progress indicator
- Back navigation to home screen

### Game State Management
- Tracks completed quiz levels in `GameManager`
- Updates progression based on quiz completion
- Maintains unlock status for different difficulty levels

### Quiz Progression Logic
```kotlin
private fun canPlayQuizLevel(
    level: Int,
    completedLevels: Set<Int>,
    unlockedLevels: Int
): Boolean {
    // Level must be unlocked
    if (level > unlockedLevels) return false
    
    // Level 1 can always be played if unlocked
    if (level == 1) return true
    
    // For other levels, previous level must be completed
    return completedLevels.contains(level - 1)
}
```

## Building and Running

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on an Android device or emulator

## Dependencies

- Jetpack Compose for UI
- DataStore for persistent storage
- WorkManager for notifications
- Material Design 3 theming 