# Nullability Plugin

A Gradle plugin for verifying a Spring project's nullability at compile time. Gradle 8.x is supported.



## Applying the Plugin

The plugin can then be applied to a project in the usual manner, as shown in the following example:

```
plugins {
	id "io.spring.nullability" version "<<version>>"
}
```


## Configuring the Plugin

The plugin can be used without configuration.
It will verify the nullability of all `JavaCompile` tasks whose name matches `compile(\\d+)?Java`.

A `nullability` extension is added by the plugin.
The extension can be used to customize the versions of the dependencies that are used during verification.

```
nullability {
	errorProneVersion = <<custom-version>>
	nullAwayVersion = <<custom-version>>
}
```
