To use the aar file you'll need to import it. In android studio:

- Click New -> New Module
- Select Import aar/jar
- Select the aar file
- in the app gradle file put: compile project(':sdk-debug')
- needed dependecies are:
  compile 'no.nordicsemi.android:dfu:0.6.2'
  compile 'com.fasterxml.jackson.core:jackson-databind:2.7.0'

In your AndroidManifest file:
< service android:name="com.theshopatvsp.levelandroidsdk.ble.BleManager"
            android:enabled="true"
            android:exported="true"></service >
            
Some where, you need to start the BleManager service, like: startService(new Intent(this, BleManager.class));
Be sure to only start it once, put it in the Application class or somewhere that gets run on app startup.
