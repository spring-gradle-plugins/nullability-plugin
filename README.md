# Nullability Plugin

A Gradle plugin for verifying a Spring project's nullability at compile time. Gradle 8.x is supported.



## Applying the Plugin

The plugin can then be applied to a project in the usual manner, as shown in the following example:

```groovy
plugins {
	id "io.spring.nullability" version "<<version>>"
}
```


## Configuring the Plugin

The plugin can be used without configuration.
It will verify the nullability of all `JavaCompile` tasks whose name matches `compile(\\d+)?Java`.

A `nullability` extension is added by the plugin.
The extension can be used to customize the versions of the dependencies that are used during verification.

```groovy
nullability {
	errorProneVersion = <<custom-version>>
	nullAwayVersion = <<custom-version>>
}
```

## Types of Nullability Checking

The plugin supports two types of nullability checking, `main` and `tests`.
The former is applied by default to tasks whose name matches `compile(\\d+)?Java`.
The latter is opt-in.

Checking using `tests` differs from `main`. It adds support for AssertJ's custom `Contract` annotation and enables NullAway's `HandleTestAssertionLibraries` option.

## Configuring Nullability Checking

The plugin adds a `nullability` extension to the `options` of all `JavaCompile` tasks.
The extension provides a single property, `checking`, that can be used to configure the type of null checking that is performed.
It defaults to `main` for tasks whose name matches `compile(\\d+)?Java`.
For all other `JavaCompile` tasks it defaults to `disabled`.

The following enables `tests` nullability checking for `compileTestJava`:

```groovy
tasks.named("compileTestJava") {
	options.nullability.checking = "tests"
}
