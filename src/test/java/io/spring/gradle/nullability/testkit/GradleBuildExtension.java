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

import java.lang.reflect.Field;
import java.net.URL;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * An {@link Extension} for managing the lifecycle of a {@link GradleBuild} stored in a
 * field named {@code gradleBuild}.
 *
 * @author Andy Wilkinson
 */
public class GradleBuildExtension implements BeforeEachCallback, AfterEachCallback {

	private static final Pattern GRADLE_VERSION_PATTERN = Pattern.compile("\\[Gradle .+\\]");

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		GradleBuild gradleBuild = extractGradleBuild(context);
		URL scriptUrl = findDefaultScript(context);
		if (scriptUrl != null) {
			gradleBuild.script(scriptUrl.getFile());
		}
		gradleBuild.before();
	}

	private GradleBuild extractGradleBuild(ExtensionContext context) throws Exception {
		Object testInstance = context.getRequiredTestInstance();
		Field gradleBuildField = testInstance.getClass().getDeclaredField("gradleBuild");
		gradleBuildField.setAccessible(true);
		GradleBuild gradleBuild = (GradleBuild) gradleBuildField.get(testInstance);
		return gradleBuild;
	}

	private URL findDefaultScript(ExtensionContext context) {
		URL scriptUrl = getScriptForTestMethod(context);
		if (scriptUrl != null) {
			return scriptUrl;
		}
		return getScriptForTestClass(context.getRequiredTestClass());
	}

	private URL getScriptForTestMethod(ExtensionContext context) {
		Class<?> testClass = context.getRequiredTestClass();
		String name = testClass.getSimpleName() + "-" + removeGradleVersion(context.getRequiredTestMethod().getName())
				+ ".gradle";
		return testClass.getResource(name);
	}

	private String removeGradleVersion(String methodName) {
		return GRADLE_VERSION_PATTERN.matcher(methodName).replaceAll("").trim();
	}

	private URL getScriptForTestClass(Class<?> testClass) {
		return testClass.getResource(testClass.getSimpleName() + ".gradle");
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		extractGradleBuild(context).after();
	}

}
