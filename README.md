# Aether


## Sample App

This repository includes a [sample app](sample).

## Requirements for showing a Google Map

* [Sign up with Google Maps Platform]
* A Google Maps Platform [project] with the **Maps SDK for Android** enabled
- An [API key] associated with the project above ... follow the [API key instructions] if you're new to the process

To run the demo app, ensure you've met the requirements above then:

1. Open the `secrets.properties` file in your top-level directory, and then add the following code. Replace YOUR_API_KEY with your API key. Store your key in this file because secrets.properties is excluded from being checked into a version control system.
   If the `secrets.properties` file does not exist, create it in the same folder as the `local.properties` file.
   ```
   MAPS_API_KEY=YOUR_API_KEY
   ```
2. Build and run


[API key]: https://developers.google.com/maps/documentation/android-sdk/get-api-key
[API key instructions]: https://developers.google.com/maps/documentation/android-sdk/config#step_3_add_your_api_key_to_the_project
[project]: https://developers.google.com/maps/documentation/android-sdk/cloud-setup
