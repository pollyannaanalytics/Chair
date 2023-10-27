
# Chair - Dating Android App

![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![ChatGPT](https://img.shields.io/badge/chatGPT-74aa9c?style=for-the-badge&logo=openai&logoColor=white)

<h1 align = "center">
    <img src = "https://github.com/pollyannaanalytics/Chair/assets/114213570/c9369859-f7e0-471b-a71e-7ad93b19de33">
</h1>

Chair is an unconventional dating app designed to connect people who share common worries.

When users enter Chair, they can input their worries, and we'll use OpenAI to categorize and prioritize matching them with people who share similar worries. In FriendConnect's chat rooms, users can use video functionality in addition to text messages, fostering deeper and more authentic connections.

This makes it easier for people to find others who understand their concerns and, in turn, build more meaningful friendships.




<img src="https://github.com/pollyannaanalytics/Chair/assets/114213570/08db57e1-8db4-4b01-bf61-08028880a031" width="240" height="520">


## Skills
* WebRTC
* Firebase
* MVVM
* Singleton
* SharedPreferences
* Unit test
* Apply third parties services
    * Google Login
    * OpenAI


## Feature
**WebRTC**

* I used WebRTC to enable direct real-time communication. without backend server support for signalling server, I utilized Firebase as a signaling server due to its real-time database capabilities to facilitate this peer-to-peer communication.
* At the beginning of meeting, I set the local description as an Offerer to the peerConnection. Then, I stored the SDP offer, including the local description, in a document within a Firebase collection.
* When another user joins the meeting as an Answerer, they retrieve their local description as an SDP answer from Firebase and send the SDP answer back to Firebase. 
* Upon receiving the SDP answer and offer, the Offerer and Answerer exchange ICE candidates through Firebase to establish the optimal connection.

![WebRTC Structure](https://github.com/pollyannaanalytics/Chair/assets/114213570/b2f2d576-cfa2-4d56-8b26-c1ea49e6f4eb)




**3rd Party Solutions**

1. Google Login:

<img src="https://github.com/pollyannaanalytics/Chair/assets/114213570/d70a7c29-3158-4089-94ec-cb70ade43182" width="240" height="520">




2. OpenAI:
* Matching users together and categorizing their worries through OpenAI.

<img src="https://github.com/pollyannaanalytics/Chair/assets/114213570/7a7fbd86-48e8-4caa-8690-27cc8dcb2f58" width="240" height="520">


## Installation

#### Get this Project

```kotlin=
git clone https://github.com/pollyannaanalytics/Chair.git
```

#### How to Set Up Enviroment Variable
You need to set up files before building up Chair as following:
1. create a google.service from firebase: https://console.firebase.google.com/u/0/
2. get openAI key from openAI reference: 
https://platform.openai.com/docs/api-reference/authentication
4. create a variable named as OPEN_AI_KEY in local.properties


## Contacts 
![Gmail](https://img.shields.io/badge/Gmail-D14836?style=for-the-badge&logo=gmail&logoColor=white)
![LinkedIn](https://img.shields.io/badge/linkedin-%230077B5.svg?style=for-the-badge&logo=linkedin&logoColor=white)
![Medium](https://img.shields.io/badge/Medium-12100E?style=for-the-badge&logo=medium&logoColor=white)

For any comments or suggestions, please feel free to reach out to me via the following channels:

* Gmail: pinyunwuu@gmail.com
* linkedin: https://www.linkedin.com/in/pin-yun-wu-1aab06231/
* Medium: https://medium.com/@androidpollyanna


