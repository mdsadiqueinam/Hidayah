# App Settings Screen Functionality

The App Settings screen allows for granular control over a specific restricted app.

## 1. Restriction Configuration
- **Daily Time Limit**: Allows setting a specific number of minutes/hours for total daily usage. Once exceeded, the app is blocked for the rest of the day.
- **Session Duration Limit**: Allows setting a time limit for a single use of the app. If the user stays in the app longer than this limit, the shield is triggered.
- **Open Delay**: (Feature Placeholder) Intended to force a waiting period before the app actually opens.
- **Hard Lock Toggle**: (Feature Placeholder) Intended for a stricter version of blocking that is harder to dismiss.

## 2. App Management
- **Un-monitor/Remove**: Provides an option to remove the app from the controlled list, deleting all its specific settings and stopping its tracking.

## 3. Data Persistence
- **Automatic Saving**: Any changes made to the limits or toggles are immediately saved to the local database and synced with the background tracking service.
