## Face Camera X

Face camera is a module for cameras with face and location detection using supporting modules, including:

- **[CameraX](https://developer.android.com/training/camerax)**: CameraX is a Jetpack library, built to make developing camera apps easy.
- **[ML Kit](https://developers.google.com/ml-kit)**: ML Kit’s processing happens on-device. This makes it fast and unlocks real-time use cases like processing of camera input. It also works while offline and can be used for processing images that need to remain on the device.
- **[Location](https://developers.google.com/android/guides/setup)**: To get the location of a specific coordinate from maps
- **[Navigation](https://developer.android.com/guide/navigation)**: Navigation is an interaction that allows users to browse, enter, and exit various content within an application.
- **[Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)**: Lifecycle-based components perform actions in response to changes in the lifecycle state of other components, such as activities and fragments.
- **[Compressor](https://github.com/zetbaitsu/Compressor)**: Compressor is a lightweight and powerful android image compression library.
- **[Dexter](https://github.com/Karumi/Dexter)**: Dexter is an Android library that simplifies the process of requesting permissions at runtime.

# Preview


   Main Sample    |  Camera with No Face Detection  | Camera with Face Detection |
:-------------------------:|:-------------------------:|:-------------------------:
![](https://github.com/mnaufalhamdani/facecamerax/blob/master/image/photo_2023-03-17_10-02-52.jpg)  |  ![](https://github.com/mnaufalhamdani/facecamerax/blob/master/image/photo_2023-03-17_10-03-00.jpg)  |  ![](https://github.com/mnaufalhamdani/facecamerax/blob/master/image/photo_2023-03-17_10-03-03.jpg)


# Usage


1. Gradle dependency:

	```groovy
	allprojects {
	   repositories {
           	maven { url "https://jitpack.io" }
	   }
	}
	```

    ```groovy
   implementation 'com.github.mnaufalhamdani:facecamerax:1.0.8'
    ```

2. The Face Camera X configuration is created using the builder pattern.

	**Kotlin**

	```kotlin
    FaceCameraX.with(this)
    	.customPath("YOUR_PATH")					//Custom path photo
            .compress(80)							//Default compress is 80
            .coordinat(0.0, 0.0)		                          	//Default coordinat is 0.0
            .defaultCamera(FaceCameraX.LensCamera.LENS_BACK_CAMERA)      	//Default camera is Front Camera
            .isFaceDetection(true)                                        	//Default is true
            .isWaterMark(true)                                            	//Default is true
            .start()  
    ```
    
3. Handling results

    **Override `onActivityResult` method and handle Face Camera X result.**

    ```kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    it.data?.let { uri ->
                        val path = uri.toFile().absolutePath
                        binding.tvPath.text = path
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    ```
    

## License

    Copyright 2023, mnaufalhamdani

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
   
   
