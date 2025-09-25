/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.gradle.nullability;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.spring.gradle.nullability.testkit.GradleBuild;
import io.spring.gradle.nullability.testkit.GradleBuildExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link NullabilityPlugin}.
 *
 * @author Andy Wilkinson
 */
@ExtendWith(GradleBuildExtension.class)
class NullabilityPluginIntegrationTests {

	private final GradleBuild gradleBuild = new GradleBuild();

	@Test
	void appliesErrorPronePlugin() {
		BuildResult result = this.gradleBuild.build("dependencies", "--configuration", "errorprone");
		assertThat(result.getOutput())
			.contains("com.google.errorprone:error_prone_core:" + NullabilityPluginExtension.ERROR_PRONE_VERSION)
			.contains("com.uber.nullaway:nullaway:" + NullabilityPluginExtension.NULL_AWAY_VERSION);
	}

	@Test
	void usesCustomErrorProneVersion() {
		BuildResult result = this.gradleBuild.build("dependencies", "--configuration", "errorprone");
		assertThat(result.getOutput()).contains("com.google.errorprone:error_prone_core:2.37.0")
			.contains("com.uber.nullaway:nullaway:" + NullabilityPluginExtension.NULL_AWAY_VERSION);
	}

	@Test
	void usesCustomNullAwayVersion() {
		BuildResult result = this.gradleBuild.build("dependencies", "--configuration", "errorprone");
		assertThat(result.getOutput())
			.contains("com.google.errorprone:error_prone_core:" + NullabilityPluginExtension.ERROR_PRONE_VERSION)
			.contains("com.uber.nullaway:nullaway:0.12.6");
	}

	@Test
	void configuresErrorProneOnCompileJava() {
		BuildResult result = this.gradleBuild.build("checkCompileJava");
		assertThat(result.getOutput())
			.contains("-XepDisableAllChecks -Xep:NullAway:ERROR -XepOpt:NullAway:OnlyNullMarked=true "
					+ "-XepOpt:NullAway:CustomContractAnnotations=org.springframework.lang.Contract "
					+ "-XepOpt:NullAway:JSpecifyMode=true");
	}

	@Test
	void disablesErrorProneOnCompileTestJavaByDefault() {
		BuildResult result = this.gradleBuild.build("checkCompileTestJava");
		assertThat(result.getOutput()).contains("-XepCompilingTestOnlyCode");
	}

	@Test
	void configuresErrorProneOnCompileTestJavaWhenEnabled() {
		BuildResult result = this.gradleBuild.build("checkCompileTestJava");
		assertThat(result.getOutput()).contains(
				"-XepDisableAllChecks -XepCompilingTestOnlyCode -Xep:NullAway:ERROR -XepOpt:NullAway:OnlyNullMarked=true "
						+ "-XepOpt:NullAway:CustomContractAnnotations=org.springframework.lang.Contract,org.assertj.core.internal.annotation.Contract "
						+ "-XepOpt:NullAway:JSpecifyMode=true -XepOpt:NullAway:HandleTestAssertionLibraries=true");
	}

	@Test
	void compileFailsForNullabilityViolationInMainCode() throws IOException {
		writeSource("main");
		BuildResult result = this.gradleBuild.prepareRunner("compileJava").buildAndFail();
		assertThat(result.getOutput()).contains("[NullAway] assigning @Nullable expression to @NonNull field");
	}

	@Test
	void compileSucceedsForNullabilityViolationInTestCode() {
		writeSource("test");
		BuildResult result = this.gradleBuild.build("compileTestJava");
		assertThat(result.task(":compileTestJava").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	void compileFailsForNullabilityViolationInTestCodeWhenCheckingIsEnabled() throws IOException {
		writeSource("test");
		BuildResult result = this.gradleBuild.prepareRunner("compileTestJava").buildAndFail();
		assertThat(result.getOutput()).contains("[NullAway] assigning @Nullable expression to @NonNull field");
	}

	private void writeSource(String sourceSetName) {
		Path projectDir = this.gradleBuild.getProjectDir().toPath();
		Path pkg = projectDir.resolve("src/%s/java/com/example".formatted(sourceSetName));
		try {
			Files.createDirectories(pkg);
			Files.writeString(pkg.resolve("package-info.java"), """
					@NullMarked
					package com.example;

					import org.jspecify.annotations.NullMarked;
					""");
			Files.writeString(pkg.resolve("Example.java"), """
					package com.example;

					public class Example {

						private Object field = null;

					}
					""");
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
