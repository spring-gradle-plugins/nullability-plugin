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

package io.spring.gradle.nullability.testkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

/**
 * A {@code GradleBuild} is used to run a Gradle build using {@link GradleRunner}.
 *
 * @author Andy Wilkinson
 */
public class GradleBuild {

	private File projectDir;

	private String script;

	void before() throws IOException {
		this.projectDir = Files.createTempDirectory("gradle-").toFile();
	}

	void after() {
		this.script = null;
		try {
			Files.walk(this.projectDir.toPath())
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public GradleBuild script(String script) {
		this.script = script;
		return this;
	}

	public BuildResult build(String... arguments) {
		try {
			return prepareRunner(arguments).build();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public GradleRunner prepareRunner(String... arguments) throws IOException {
		Files.copy(new File(this.script).toPath(), new File(this.projectDir, "build.gradle").toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		GradleRunner gradleRunner = GradleRunner.create().withProjectDir(this.projectDir);
		gradleRunner.withDebug(true).withPluginClasspath();
		List<String> allArguments = new ArrayList<>();
		allArguments.add("--stacktrace");
		allArguments.addAll(Arrays.asList(arguments));
		allArguments.add("--warning-mode");
		allArguments.add("all");
		return gradleRunner.withArguments(allArguments);
	}

	public File getProjectDir() {
		return this.projectDir;
	}

	public void setProjectDir(File projectDir) {
		this.projectDir = projectDir;
	}

}
