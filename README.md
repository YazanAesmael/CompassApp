# Android Compass App

This GitHub repository contains the source code for an Android Compass App. The app utilizes Kotlin and Jetpack Compose to create a simple yet functional compass that provides real-time heading information.

## Features

- Real-time compass heading display.
- Smooth and responsive compass needle animation.
- Integration with sensors to provide accurate heading data.
- Hilt for dependency injection.
- Utilizes Android's sensor framework for orientation data.
- Clean and well-structured codebase.

## Getting Started

To get started with this app, follow these steps:

1. Clone this repository to your local machine:

   ```bash
   git clone https://github.com/YazanAesmael/CompassApp.git
   ```

2. Open the project in Android Studio.

3. Build and run the app on an Android emulator or a physical device.

## Usage

The app provides a compass view on the home screen, along with the current heading displayed in degrees. The compass needle rotates to reflect the device's orientation, giving you a real-time compass reading.

## Dependencies

The app uses the following dependencies and libraries:

- Kotlin
- Jetpack Compose
- Hilt for dependency injection
- Android's Sensor framework for sensor data
- Android's Material Design components for UI elements

Make sure to check the `build.gradle` files for specific versions of these dependencies.

## Code Structure

- `HomeScreen.kt`: The main screen of the app, displaying the compass view and current heading.
- `CompassView.kt`: Custom Composable that draws the compass view.
- `HomeViewModel.kt`: ViewModel responsible for managing sensor data and compass heading.
- `SensorDataManager.kt`: Class for handling sensor data and filtering.
- `Compass.kt`: Class for managing compass functionality and sensor integration.

## Contributing

If you would like to contribute to this project, please follow these guidelines:

1. Fork the repository.

2. Create a new branch for your feature or bug fix:

3. Make your changes and commit them with clear and concise messages.

4. Push your changes to your fork:

5. Create a pull request to the main repository.

## License

This Android Compass App is open-source and available under the MIT License. See the [LICENSE](LICENSE) file for details.
