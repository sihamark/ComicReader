# Cyclone

Manga and Comic Reading app, planned to be a multiplatform app.
Currently only Android is supported.

## Kotlin Multiplatform

- remove hilt in favor for kotlin-inject (easier to migrate from hilt),
  see [pr](https://github.com/tfcporciuncula/kotlin-inject-greeter/pull/2/files)
  and [article](https://proandroiddev.com/from-dagger-hilt-into-the-multiplatform-world-with-kotlin-inject-647d8e3bddd5)
- image loading with coil maybe for multiplatform;
  alternative: https://qdsfdhvh.github.io/compose-imageloader/