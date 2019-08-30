# RtmpPublisher

<img src="https://github.com/TakuSemba/RtmpPublisher/blob/master/arts/logo.png" alt="alt text" style="width:200;height:200">

![Platform](http://img.shields.io/badge/platform-android-green.svg?style=flat)
![Download](https://api.bintray.com/packages/takusemba/maven/rtmppublisher/images/download.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-18%2B-brightgreen.svg?style=flat)

## Gradle

```groovy
dependencies {
    compile 'com.github.takusemba:rtmppublisher:x.x.x'
}
```


<br/>

## Usage

usage is simple. RtmpPublisher does everything.

### create a Publisher
```kt
val publisher: Publisher = Publisher.Builder(this)
  .setGlView(glView)
  .setUrl(rtmpUrl)
  .setSize(Publisher.Builder.DEFAULT_WIDTH, Publisher.Builder.DEFAULT_HEIGHT)
  .setAudioBitrate(Publisher.Builder.DEFAULT_AUDIO_BITRATE)
  .setVideoBitrate(Publisher.Builder.DEFAULT_VIDEO_BITRATE)
  .setCameraMode(Publisher.Builder.DEFAULT_MODE)
  .setListener(this)
  .build()
```

<img src="https://github.com/TakuSemba/RtmpPublisher/blob/master/arts/sample.gif" align="right" width="30%">

### start RTMP Streaming

```kt

// start publishing!
publisher.startPublishing()

// switch camera between front and back
publisher.switchCamera()

// stop publishing!
publisher.stopPublishing()


publisher.setOnPublisherListener(object: PublisherListener {
  override fun onStarted() {
    // do something
  }
    override fun onStopped() {
    // do something
  }
    override fun onFailedToConnect() {
    // do something
  }
    override fun onDisconnected() {
    // do something
  }
})
```

<br/>

## Quick Start

<img src="https://github.com/TakuSemba/RtmpPublisher/blob/master/arts/architecture.png" alt="alt text" style="width:200;height:200">

Try this [sample](https://github.com/TakuSemba/RtmpPublisher/tree/master/app) here, but this is just a RTMP android client. You also need RTMP server and player to do publish and play them.

You can either make the server and player yourself or use the ones below that i prepared.

#### Server
https://github.com/TakuSemba/docker-nginx-rtmp

#### RTMP Player
https://github.com/TakuSemba/RtmpPlayer

#### HLS Player
https://github.com/TakuSemba/HlsPlayer


## Change Log

### Version: 1.0.3

  * camera switcher added


### Version: 1.0.0

  * first release


## Author

* **Taku Semba**
    * **Github** - (https://github.com/takusemba)
    * **Twitter** - (https://twitter.com/takusemba)
    * **Facebook** - (https://www.facebook.com/takusemba)

## Licence
```
Copyright 2017 Taku Semba.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
