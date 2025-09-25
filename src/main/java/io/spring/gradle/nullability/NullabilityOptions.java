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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Nullability configuration options for a {@link JavaCompile} task.
 *
 * @author Andy Wilkinson
 */
public abstract class NullabilityOptions {

	/**
	 * Internal use only.
	 * @param errorProne the ErrorProne options to which the nullability options are
	 * applied
	 */
	@Inject
	public NullabilityOptions(ErrorProneOptions errorProne) {
		errorProne.getEnabled().set(getChecking().map((checking) -> checking != Checking.DISABLED));
		errorProne.getDisableAllChecks().set(getChecking().map((checking) -> checking != Checking.DISABLED));
		errorProne.getCheckOptions().putAll(getChecking().map((checking) -> {
			Map<String, String> options = new LinkedHashMap<>();
			if (checking == Checking.MAIN) {
				options.put("NullAway:OnlyNullMarked", "true");
				options.put("NullAway:CustomContractAnnotations", "org.springframework.lang.Contract");
				options.put("NullAway:JSpecifyMode", "true");
			}
			return options;
		}));
		errorProne.getChecks().putAll(getChecking().map((checking) -> {
			if (checking == Checking.MAIN) {
				return Map.of("NullAway", CheckSeverity.ERROR);
			}
			return Collections.emptyMap();
		}));
	}

	/**
	 * Returns the type of checking to perform.
	 * @return the type of checking
	 */
	public abstract Property<Checking> getChecking();

	/**
	 * The type of checking to perform for the {@link JavaCompile} task.
	 */
	public enum Checking {

		/**
		 * Main code nullability checking is performed.
		 */
		MAIN,

		/**
		 * Nullability checking is disabled.
		 */
		DISABLED

	}

}
