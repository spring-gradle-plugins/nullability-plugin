# Nullability Plugin

A Gradle plugin for verifying a Spring project's nullability at compile time. Gradle 8.x is supported.



## Applying the Plugin

The plugin can then be applied to a project in the usual manner, as shown in the following example:

```gradle
plugins {
    id "io.spring.nullability" version "<<version>>"
}
```


## Configuring the Plugin

The plugin can be used without configuration.
It will verify the nullability of all `JavaCompile` tasks whose name matches `compile(\\d+)?Java`.

A `nullability` extension is added by the plugin.
The extension can be used to customize the versions of the dependencies that are used during verification.

```gradle
nullability {
    errorProneVersion = <<custom-version>>
    nullAwayVersion = <<custom-version>>
}
```

### Configuring additional source sets

By default, only the "main" source set is checked.
To enable nullability checks for the source set "additional":

```gradle
nullability {
    check {
        sourceSet {
            "additional" {
                type = "main"
            }
        }
    }
}
```

### Checking Nullability in Tests

By default, the plugin only checks nullability on production code.
To enable nullability checking in tests, set the type of the "test" source set.

```gradle
nullability {
    check {
        sourceSet {
            "test" {
                type = "test"
            }
        }
    }
}
```

It will now additionally verify the nullability of all `JavaCompile` tasks whose name matches `compileTest(\\d+)?Java`.
Additionally, it enables the AssertJ contract annotation and special null handling for AssertJ calls.
