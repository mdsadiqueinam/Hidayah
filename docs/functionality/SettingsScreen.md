# Settings & App Configuration Functionality

The application features two levels of settings: Global Shield Settings and individual App Settings.

## 1. Global Shield Settings
These settings define the appearance and default behavior of the blocking "Shield" screen that appears when a controlled app is opened.

- **Headline Customization**: Users can write a custom headline to be displayed on the shield (e.g., "Pause. Think. Decide.").
- **Sub-headline Customization**: Users can add a supportive message or instruction (e.g., "Take a breath before opening this app.").
- **Visual Customization**: Allows users to select a custom image from their gallery to be displayed on the shield screen.
- **Reset to Defaults**: Provides a quick way to revert the shield's appearance to the original design.
- **Real-time Preview**: Changes made to the headline, sub-headline, or image are saved and can be seen instantly on the shield.

## 2. Individual App Settings
Accessible by selecting a specific app from the Home Screen, these settings define the restrictions for that particular app.

- **Daily Usage Limit**: Set a maximum total time allowed for the app per day (e.g., 30 minutes total).
- **Session Limit**: Set a maximum duration for a single continuous session. Once this limit is reached, the shield is triggered (e.g., block after 10 minutes of continuous use).
- **Open Delay**: (Coming soon) Introduces a mandatory wait time before the app can be accessed to reduce impulsive opening.
- **Hard Lock**: (Coming soon) A stricter mode to prevent bypassing the limit.
- **App Removal**: Allows removing the app from the controlled list, effectively stopping all monitoring and restrictions for it.

## 3. App Tracking & Enforcement (Service Logic)
- **Instant Detection**: Monitors foreground app changes with high frequency (300ms) to trigger the shield immediately upon opening a restricted app.
- **Session Tracking**: Tracks how long the current app has been in the foreground to enforce session limits.
- **Unlock Awareness**: Only triggers the shield when the device is unlocked to prevent background interference.
- **Overlay Enforcement**: Requires and checks for "Display over other apps" permission to ensure the shield can actually appear.
