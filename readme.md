# _Remote Classroom_

#### _An Android app for teaching foreign languages_

#### By _**Sam Gespass**_

## Description

_This app is geared towards teaching foreign languages on a smartphone. One of the issues I've found for most applications used for this purpose is that they're not specifically geared towards foreign language teaching. I want to be able to engage students in activities as well as talk to them._

## Specs

| Spec | Input | Output |
| :-------------     | :------------- | :------------- |
| It can create a user | create user | creates user |
| A user can sign in | sign in | signs in |
| It can create a session | create session | creates session |
| It can connect to a session given a session id | mSession.createToken(sessionId) | Connects to session |
| It can send receive video and audio from another user | Two users connect | Shows audio and video from other user |
| It can make the bottom frame larger and smaller | touch button to make activity larger or smaller | Makes activity larger or smaller |
| It can flip camera | touch flip camera button | flips camera |
| It can disconnect | touch disconnect button | disconnects |
| It can show a small memory game in the frame on the bottom | Change spinner to "memory game" | Shows game in bottom frame |
| It can show the card to all users when it's pressed | press card | shows to all users |
| It hides the card if there's no match | wrong card | hides all cards |
| It shows and alerts when card and words match | cards and words match | shows card and word |

## Future Features

* Enable dragging for division bar to make bottom frame larger or smaller
* Differentiate between students and teachers
* Create website so teachers can make activities
* Add more activities
* Empty bottom frame when "No Activities" is selected
* Improve layouts by implementing RecyclerViews
* Allow users to toggle audio/video


## Setup/Installation Requirements

* Click on the following [link](https://github.com/darthtoad/RemoteClassroomOpenTok) to download Word Up
* Open Android Studio (preferably version 3.0.1)
* Open the project in Android Studio
* Go to [TokBox](https://TokBox.com/). Create an account and start a project. Find your API key, create a SessionId, and generate a token.
* Create a file in your root directory called "gradle.properties". This file should have the following content: org.gradle.jvmargs=-Xmx1536m  ApiKey = "API KEY HERE" ApiSecret = "API SECRET HERE"
* Run project

## Known Bugs

_This project currently has no known bugs. If you find any, please [message](mailto:darth.toad@gmail.com) me._

## Technologies Used

* _Java_
* _Android Studio_
* _Git_
* _GitHub_
* _XML_
* _OpenTok/TokBox API and SDK_
* _Firebase_
* _Javascript_
* _Node.js_
* _Express.js_
* _Babel_
* _zeit.co_

### License

Copyright (c) 2017 ****_Sam Gespass_****

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.