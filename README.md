<h1>Level / Level SDK Documentation</h1>
<h3>About</h3>
Level is an activity tracker in the form factor of glasses. The sensors it contains are an accelerometer, gyroscope and a magnetometer. The device is split up into 8 "reporters", these reporters can be setup to report on different sensors or aggregated values from the sensors, like steps. Things that can be reporter on are:
  - Accelerometer raw data
  - Gyrometer raw data 
  - Steps per unit time. 
  
  
The typical connection interaction is:
  - App scans for Level devices
  - App sends a connection request to the device
  - The app and device pair/bond
  - Upon successful pairing/bonding, the app key is sent to the device
  - The app then queries the device lock
      - If the lock is locked, perform Blink to Link (see below)
      - If the lock is unlocked, proceed as normal
<h3>Blink to Link</h3>
This is an interaction between the device and the user, it ensures that the device being connected to, is the device that the user is holding. Upon successful connection and pairing, the led on the device will illuminate. The user will then have to input the color of the led into the app somhow. Color codes are:
White = 0x00
Purple = 0x01
Red = 0x02
Yellow = 0x03


The color sequence is 4 lights long and the led will turn off briefly between colors. Upon sending the 4th color to the device the sdk will signal if the sequence was successful, by calling the onLedCodeDone callback. If the sequence was input incorrectly: oneLedCodeFailed will be called. Blink to link only needs to be done once, as long and the pair/bond is good onLedCodeNotNeeded will be called when it is not required.


A note of warning, if the glasses are charging, it can be difficult to tell if blink to link has started. It is best to disconnect from the charger, if blink to link is going to be performed.

<h3>iOS and Android SDK</h3>

The iOS and Android sdk's are nearly identical, with very few differences in implementation. <br />
The iOS sdk is more immature, hence there is only an example app and no real distribution of it. So you can hack the example app or extract/copy over the LevelIOSSdk folder. XCode 8.1 and swift 3 is required. <br />
<br />The android sdk is set up as an aar file, so you just need to import the sdk-debug1.02.aar file into your android studio project (New -> New Module -> Import aar/jar library) See the readme in levelSDK/LevelAndroidSDK.
<br /><br />

The example apps using the Level SDK are not good app, but it demonstrates how to interact with the Level SDK. The UI is not complete and using the Android Monitor/console to view the logs is essetential.

There are 2 screens in both apps, Blink to Link, which facillitates the pairing of the device to the phone and the Dashboard. The dashboard facilitates all communication to the device. The UI is lacking when it comes to displaying the output from the device, but it is in the logs.

For SDK documentation see: https://docs.google.com/document/d/15pUD-JFxV4LIPJz6DFHOcJzxUQbwauO6dihMoJXmidE/edit?usp=sharing
