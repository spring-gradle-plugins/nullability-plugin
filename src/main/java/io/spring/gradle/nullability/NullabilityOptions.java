/*
 * Copyright 2025-present the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Nullability configuration options.
 *
 * @author Andy Wilkinson
 */
public abstract class NullabilityOptions {

	private static final Pattern COMPILE_MAIN_SOURCES_TASK_NAME = Pattern.compile("compile(\\d+)?Java");

	private final JSpecifyOptions jspecify;

	/**
	 * Internal use only.
	 * @param objects object factory to create nested instances
	 * @param defaults default nullability configuration to use by convention applied
	 */
	@Inject
	public NullabilityOptions(ObjectFactory objects, NullabilityOptions defaults) {
		this.jspecify = objects.newInstance(JSpecifyOptions.class);
		if (defaults != null) {
			getRequireExplicitNullMarking().convention(defaults.getRequireExplicitNullMarking());
			this.jspecify.defaults(defaults.jspecify);
		}
		else {
			getRequireExplicitNullMarking().convention(true);
		}
	}

	public void jspecify(Action<JSpecifyOptions> configurer) {
		configurer.execute(this.jspecify);
	}

	/**
	 * Returns the type of checking to perform.
	 * @return the type of checking
	 */
	public abstract Property<String> getChecking();

	/**
	 * Whether explicit null marking is required.
	 * @return the property for whether explicit null marking is required
	 */
	public abstract Property<Boolean> getRequireExplicitNullMarking();

	void apply(JavaCompile javaCompile) {
		CompileOptions options = javaCompile.getOptions();
		ErrorProneOptions errorProneOptions = ((ExtensionAware) options).getExtensions()
			.getByType(ErrorProneOptions.class);
		getChecking().set(compilesMainSources(javaCompile) ? Checking.MAIN.name() : Checking.DISABLED.name());
		Provider<Checking> checkingAsEnum = getChecking()
			.map((string) -> Checking.valueOf(string.toUpperCase(Locale.ROOT)));
		errorProneOptions.getEnabled().set(checkingAsEnum.map((checking) -> checking != Checking.DISABLED));
		errorProneOptions.getDisableAllChecks().set(checkingAsEnum.map((checking) -> checking != Checking.DISABLED));
		errorProneOptions.getCheckOptions().putAll(checkingAsEnum.map(this::checkOptions));
		errorProneOptions.getChecks().putAll(checkingAsEnum.map(this::checks));
	}

	private boolean compilesMainSources(JavaCompile compileTask) {
		return COMPILE_MAIN_SOURCES_TASK_NAME.matcher(compileTask.getName()).matches();
	}

	private Map<String, String> checkOptions(Checking checking) {
		if (checking == Checking.DISABLED) {
			return Collections.emptyMap();
		}
		Map<String, String> options = new LinkedHashMap<>();
		options.put("NullAway:OnlyNullMarked", "true");
		List<String> customContractAnnotations = new ArrayList<>();
		customContractAnnotations.add("org.springframework.lang.Contract");
		if (checking == Checking.TESTS) {
			customContractAnnotations.add("org.assertj.core.internal.annotation.Contract");
		}
		options.put("NullAway:CheckContracts", "true");
		options.put("NullAway:CustomContractAnnotations", String.join(",", customContractAnnotations));
		if (checking == Checking.TESTS) {
			options.put("NullAway:HandleTestAssertionLibraries", "true");
		}
		this.jspecify.configureOptions(options);
		return options;
	}

	private Map<String, CheckSeverity> checks(Checking checking) {
		if (checking != Checking.DISABLED) {
			Map<String, CheckSeverity> checks = new HashMap<>();
			checks.put("NullAway", CheckSeverity.ERROR);
			if (Boolean.TRUE.equals(getRequireExplicitNullMarking().get())) {
				checks.put("RequireExplicitNullMarking", CheckSeverity.ERROR);
			}
			this.jspecify.configureChecks(checks);
			return checks;
		}
		return Collections.emptyMap();
	}

	public abstract static class JSpecifyOptions {

		public JSpecifyOptions() {
			getExperimental().convention(false);
			getUnrecognizedAnnotationLocation().convention(CheckSeverity.WARN);
		}

		/**
		 * Whether JSpecify Experimental mode is enabled.
		 * @return the property for whether JSpecify Experimental mode is enabled
		 */
		public abstract Property<Boolean> getExperimental();

		/**
		 * Severity of the JSpecify unrecognized annotation location check.
		 * @return the property for the severity of the unrecognized annotation location
		 * check
		 */
		public abstract Property<CheckSeverity> getUnrecognizedAnnotationLocation();

		private void defaults(JSpecifyOptions defaults) {
			getExperimental().convention(defaults.getExperimental());
			getUnrecognizedAnnotationLocation().convention(defaults.getUnrecognizedAnnotationLocation());
		}

		void configureOptions(Map<String, String> options) {
			options.put("NullAway:JSpecifyMode", "true");
			options.put("NullAway:JSpecifyExperimental", Boolean.toString(getExperimental().get()));
		}

		void configureChecks(Map<String, CheckSeverity> checks) {
			checks.put("JSpecifyUnrecognizedAnnotationLocation", getUnrecognizedAnnotationLocation().get());
		}

	}

	/**
	 * The type of null checking to perform for the {@link JavaCompile} task.
	 */
	enum Checking {

		/**
		 * Main code nullability checking is performed.
		 */
		MAIN,

		/**
		 * Test code nullability checking is performed.
		 */
		TESTS,

		/**
		 * Nullability checking is disabled.
		 */
		DISABLED

	}

}
