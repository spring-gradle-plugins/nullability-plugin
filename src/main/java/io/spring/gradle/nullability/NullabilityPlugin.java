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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Gradle plugin for compile-time verification of nullability.
 *
 * @author Brian Clozel
 * @author Andy Wilkinson
 * @author Moritz Halbritter
 */
public class NullabilityPlugin implements Plugin<Project> {

	private static final Set<String> CUSTOM_CONTRACT_ANNOTATIONS = Set.of("org.springframework.lang.Contract");

	private static final Set<String> CUSTOM_CONTRACT_TEST_ANNOTATIONS = Set
		.of("org.assertj.core.internal.annotation.Contract");

	private static final Pattern COMPILE_TASK_NAME = Pattern.compile("compile(\\w*)Java");

	@Override
	public void apply(Project project) {
		NullabilityPluginExtension nullability = project.getExtensions()
			.create("nullability", NullabilityPluginExtension.class);
		project.getPlugins().apply(ErrorPronePlugin.class);
		configureDependencies(project, nullability);
		configureJavaCompilation(project, nullability);
	}

	private void configureDependencies(Project project, NullabilityPluginExtension nullability) {
		project.getConfigurations().getByName(ErrorPronePlugin.CONFIGURATION_NAME);
		project.getDependencies()
			.add(ErrorPronePlugin.CONFIGURATION_NAME, nullability.getErrorProneVersion()
				.map((version) -> "com.google.errorprone:error_prone_core:" + version));
		project.getDependencies()
			.add(ErrorPronePlugin.CONFIGURATION_NAME,
					nullability.getNullAwayVersion().map((version) -> "com.uber.nullaway:nullaway:" + version));
	}

	private void configureJavaCompilation(Project project, NullabilityPluginExtension nullability) {
		project.getTasks().withType(JavaCompile.class).configureEach((javaCompile) -> {
			Provider<TaskType> taskType = project.provider(() -> getTaskType(nullability, javaCompile));
			Provider<Boolean> enabled = taskType.map((type) -> type != TaskType.DISABLED);
			configureErrorProne(javaCompile, enabled, taskType);
			setErrorProneEnabled(javaCompile, enabled);
		});
	}

	private TaskType getTaskType(NullabilityPluginExtension nullability, JavaCompile compileTask) {
		Matcher matcher = COMPILE_TASK_NAME.matcher(compileTask.getName());
		if (!matcher.matches()) {
			return TaskType.DISABLED;
		}
		String sourceSetName = TaskNameUtils.uncapitalize(TaskNameUtils.stripDigits(matcher.group(1)));
		if (sourceSetName.isEmpty()) {
			return TaskType.MAIN;
		}
		NullabilityPluginExtension.Check.SourceSet sourceSet = nullability.getCheck()
			.getSourceSet()
			.findByName(sourceSetName);
		if (sourceSet == null) {
			return TaskType.DISABLED;
		}
		String sourceSetType = sourceSet.getType().get();
		return switch (sourceSetType) {
			case "main" -> TaskType.MAIN;
			case "test" -> TaskType.TEST;
			default -> throw new IllegalStateException(
					"Unknown source set type '%s'. Supported types are: 'main', 'test'".formatted(sourceSetType));
		};
	}

	private void configureErrorProne(JavaCompile compileTask, Provider<Boolean> enabled, Provider<TaskType> taskType) {
		errorProneOptions(compileTask, (options) -> {
			options.getDisableAllChecks().set(ifEnabled(enabled, () -> true));
			options.getCheckOptions().putAll(ifEnabled(enabled, () -> getCheckOptions(taskType.get())));
			options.getChecks().putAll(ifEnabled(enabled, () -> Map.of("NullAway", CheckSeverity.ERROR)));
		});
	}

	private <T> Provider<T> ifEnabled(Provider<Boolean> enabled, Supplier<T> supplier) {
		return enabled.map((e) -> e ? supplier.get() : null);
	}

	private Map<String, String> getCheckOptions(TaskType taskType) {
		Map<String, String> result = new HashMap<>();
		result.put("NullAway:OnlyNullMarked", "true");
		result.put("NullAway:CustomContractAnnotations", getCustomContractAnnotationsOption(taskType));
		result.put("NullAway:JSpecifyMode", "true");
		if (taskType == TaskType.TEST) {
			result.put("NullAway:HandleTestAssertionLibraries", "true");
		}
		return Collections.unmodifiableMap(result);
	}

	private String getCustomContractAnnotationsOption(TaskType taskType) {
		StringJoiner result = new StringJoiner(",");
		for (String annotation : new TreeSet<>(CUSTOM_CONTRACT_ANNOTATIONS)) {
			result.add(annotation);
		}
		if (taskType == TaskType.TEST) {
			for (String annotation : new TreeSet<>(CUSTOM_CONTRACT_TEST_ANNOTATIONS)) {
				result.add(annotation);
			}
		}
		return result.toString();
	}

	private void setErrorProneEnabled(JavaCompile compileTask, Provider<Boolean> enabled) {
		errorProneOptions(compileTask, (errorProneOptions) -> errorProneOptions.getEnabled().set(enabled));
	}

	private void errorProneOptions(JavaCompile compileTask, Consumer<ErrorProneOptions> optionsConsumer) {
		CompileOptions options = compileTask.getOptions();
		ErrorProneOptions errorProneOptions = ((ExtensionAware) options).getExtensions()
			.getByType(ErrorProneOptions.class);
		optionsConsumer.accept(errorProneOptions);
	}

	private enum TaskType {

		/**
		 * Main code.
		 */
		MAIN,
		/**
		 * Test code.
		 */
		TEST,
		/**
		 * Disabled.
		 */
		DISABLED

	}

}
