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

import javax.inject.Inject;

import net.ltgt.gradle.errorprone.CheckSeverity;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/**
 * Extension for configuring the {@link NullabilityPlugin}.
 *
 * @author Andy Wilkinson
 */
public abstract class NullabilityPluginExtension {

	static final String ERROR_PRONE_VERSION = "2.50.0";

	static final String NULL_AWAY_VERSION = "0.14.2";

	private final JSpecify jspecify;

	/**
	 * Internal use only.
	 * @param objects object factory to create nested instances
	 */
	@Inject
	public NullabilityPluginExtension(ObjectFactory objects) {
		getErrorProneVersion().convention(ERROR_PRONE_VERSION);
		getNullAwayVersion().convention(NULL_AWAY_VERSION);
		getRequireExplicitNullMarking().convention(true);
		this.jspecify = objects.newInstance(JSpecify.class);
	}

	/**
	 * The version of Error Prone to use.
	 * @return the Error Prone version
	 */
	public abstract Property<String> getErrorProneVersion();

	/**
	 * The version of NullAway to use.
	 * @return the NullAway version
	 */
	public abstract Property<String> getNullAwayVersion();

	/**
	 * Whether explicit null marking is required.
	 * @return the property for whether explicit null marking is required
	 */
	public abstract Property<Boolean> getRequireExplicitNullMarking();

	public void jspecify(Action<JSpecify> configurer) {
		configurer.execute(this.jspecify);
	}

	JSpecify jspecify() {
		return this.jspecify;
	}

	public abstract static class JSpecify {

		public JSpecify() {
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
		 * @return the property for whether JSpecify Experimental mode is enabled
		 */
		public abstract Property<CheckSeverity> getUnrecognizedAnnotationLocation();

	}

}
