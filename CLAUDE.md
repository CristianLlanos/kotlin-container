# Kotlin Container

## Publishing to Maven Central

```bash
publish-maven --dry-run    # test locally (~/.m2)
publish-maven              # publish to Maven Central
```

Coordinates: `com.cristianllanos:container:<version>`

Version is set in `build.gradle.kts`. Bump it before publishing a new release.

After publishing, check status at https://central.sonatype.com
