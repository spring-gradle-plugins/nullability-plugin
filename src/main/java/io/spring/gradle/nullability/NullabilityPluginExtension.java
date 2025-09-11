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

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/**
 * Extension for configuring the {@link NullabilityPlugin}.
 *
 * @author Andy Wilkinson
 * @author Moritz Halbritter
 */
public abstract class NullabilityPluginExtension {

	static final String ERROR_PRONE_VERSION = "2.41.0";

	static final String NULL_AWAY_VERSION = "0.12.9";

	private final Check check;

	/**
	 * Internal use only.
	 * @param objectFactory the object factory
	 */
	@Inject
	public NullabilityPluginExtension(ObjectFactory objectFactory) {
		this.check = objectFactory.newInstance(Check.class);
		getErrorProneVersion().convention(ERROR_PRONE_VERSION);
		getNullAwayVersion().convention(NULL_AWAY_VERSION);
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
	 * Returns the configured checking.
	 * @return the configured checking
	 */
	public Check getCheck() {
		return this.check;
	}

	/**
	 * Configures checking.
	 * @param action the action to execute
	 */
	public void check(Action<Check> action) {
		action.execute(this.check);
	}

	/**
	 * Configures checking.
	 */
	public abstract static class Check {

		private final NamedDomainObjectContainer<SourceSet> sourceSet;

		/**
		 * Internal use only.
		 * @param objectFactory the object factory
		 */
		@Inject
		public Check(ObjectFactory objectFactory) {
			this.sourceSet = objectFactory.domainObjectContainer(SourceSet.class,
					(name) -> objectFactory.newInstance(SourceSet.class, name));
		}

		/**
		 * Configures checking on source sets.
		 * @return the source sets
		 */
		public NamedDomainObjectContainer<SourceSet> getSourceSet() {
			return this.sourceSet;
		}

		/**
		 * A source set.
		 */
		public abstract static class SourceSet implements Named {

			private final String name;

			/**
			 * Internal use only.
			 * @param name the name of the source set
			 */
			@Inject
			public SourceSet(String name) {
				this.name = name;
				getType().convention("main");
			}

			@Override
			public String getName() {
				return this.name;
			}

			/**
			 * The type of the source set. Valid values are 'main' and 'test'.
			 * @return the type of the source set
			 */
			public abstract Property<String> getType();

		}

	}

}
