# Kotlin Container

## Publishing to Maven Central

```bash
publish-maven --dry-run    # test locally (~/.m2)
publish-maven              # publish to Maven Central (manual release via sonatype)
publish-maven --release    # publish and release automatically
```

Coordinates: `com.cristianllanos:container:<version>`

Version is set in `build.gradle.kts`. Bump it and update the README installation snippet before publishing a new release.
