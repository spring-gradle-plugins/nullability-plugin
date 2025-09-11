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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TaskNameUtils}.
 *
 * @author Moritz Halbritter
 */
class TaskNameUtilsTests {

	@Test
	void shouldStripDigits() {
		assertThat(TaskNameUtils.stripDigits("")).isEqualTo("");
		assertThat(TaskNameUtils.stripDigits("abc")).isEqualTo("abc");
		assertThat(TaskNameUtils.stripDigits("abc123")).isEqualTo("abc");
		assertThat(TaskNameUtils.stripDigits("ab123c")).isEqualTo("ab123c");
		assertThat(TaskNameUtils.stripDigits("123abc")).isEqualTo("123abc");
		assertThat(TaskNameUtils.stripDigits("123abc456")).isEqualTo("123abc");
		assertThat(TaskNameUtils.stripDigits("123ab456c789")).isEqualTo("123ab456c");
	}

	@Test
	void shouldUncapitalize() {
		assertThat(TaskNameUtils.uncapitalize("")).isEqualTo("");
		assertThat(TaskNameUtils.uncapitalize("abc")).isEqualTo("abc");
		assertThat(TaskNameUtils.uncapitalize("Abc")).isEqualTo("abc");
		assertThat(TaskNameUtils.uncapitalize("ABC")).isEqualTo("aBC");
		assertThat(TaskNameUtils.uncapitalize("ABc")).isEqualTo("aBc");
	}

}
