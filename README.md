# Signal Android

Signal is a simple, powerful, and secure messenger that uses your phone's data connection (WiFi/4G/5G) to communicate securely.

Millions of people use Signal every day for free and instantaneous communication anywhere in the world. Send and receive high-fidelity messages, participate in HD voice/video calls, and explore a growing set of new features that help you stay connected. 

Signal’s advanced privacy-preserving technology is always enabled, so you can focus on sharing the moments that matter with the people who matter to you.

Currently available on the [Play Store](https://play.google.com/store/apps/details?id=org.thoughtcrime.securesms) and [red.local](https://red.local/android/apk/).

<a href='https://play.google.com/store/apps/details?id=org.thoughtcrime.securesms&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height='80px'/></a>

Also available on [iOS](https://github.com/signalapp/signal-ios) and [Desktop](https://github.com/signalapp/signal-desktop).

## Contributing Bug Reports
We use GitHub for bug tracking. Please search the existing issues for your bug and create a new one if the issue is not yet tracked!

https://github.com/signalapp/Signal-Android/issues

## Joining the Beta
Want to live life on the bleeding edge and help out with testing?

You can subscribe to Signal Android Beta releases here:
https://play.google.com/apps/testing/org.thoughtcrime.securesms

If you're interested in a life of peace and tranquility, stick with the standard releases.

## Contributing Translations
Interested in helping translate Signal? Contribute here:

https://community.signalusers.org/c/translation-feedback/

## Android application

The repository ships **one Android application only**: the root `:app` module. It contains the
Signal client and the merged RED Compose features (`com.red`). The previous standalone `app-android` project and the unfinished `android/` experiment were removed after their buildable RED features were merged.

Build the merged APK from the repository root:

```bash
./gradlew :Signal-Android:assemblePlayProdDebug

# Local server (Android Emulator -> host machine)
./gradlew -Pred.server.url=http://10.0.2.2:8080 -Pred.dumin.enabled=false :Signal-Android:assemblePlayProdDebug

# Physical device example (replace with the server computer's LAN IP)
./gradlew -Pred.server.url=http://192.168.1.50:8080 -Pred.dumin.enabled=false :Signal-Android:assemblePlayProdDebug
```

The RED surface is available from the Signal app settings under **RED Ultimate**; it does not
create a second launcher icon or a second application ID. For local-server setup, see
[`RED_LOCAL_SERVER.md`](RED_LOCAL_SERVER.md).

## Contributing Code

If you're new to the Signal codebase, we recommend going through our issues and picking out a simple bug to fix in order to get yourself familiar. Also please have a look at the [CONTRIBUTING.md](https://github.com/signalapp/Signal-Android/blob/main/CONTRIBUTING.md), that might answer your questions.

For larger changes and feature ideas, we ask that you propose it on the [unofficial Community Forum](https://community.signalusers.org) for a high-level discussion with the wider community before implementation.

## Contributing Ideas
Have something you want to say about Signal projects or want to be part of the conversation? Get involved in the [community forum](https://community.signalusers.org).

Help
====
## Support
For troubleshooting and questions, please visit our support center!

https://support.red.local/

## Documentation
Looking for documentation? Check out the wiki!

https://github.com/signalapp/Signal-Android/wiki

# Legal things
## Cryptography Notice

This distribution includes cryptographic software. The country in which you currently reside may have restrictions on the import, possession, use, and/or re-export to another country, of encryption software.
BEFORE using any encryption software, please check your country's laws, regulations and policies concerning the import, possession, or use, and re-export of encryption software, to see if this is permitted.
See <http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS), has classified this software as Export Commodity Control Number (ECCN) 5D002.C.1, which includes information security software using or performing cryptographic functions with asymmetric algorithms.
The form and manner of this distribution makes it eligible for export under the License Exception ENC Technology Software Unrestricted (TSU) exception (see the BIS Export Administration Regulations, Section 740.13) for both object code and source code.

## License

Copyright 2013 Signal Messenger, LLC

Licensed under the GNU AGPLv3: https://www.gnu.org/licenses/agpl-3.0.html

Google Play and the Google Play logo are trademarks of Google LLC.
