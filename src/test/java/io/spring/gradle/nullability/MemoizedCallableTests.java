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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MemoizedCallable}.
 *
 * @author Moritz Halbritter
 */
class MemoizedCallableTests {

	@Test
	void shouldMemoize() throws Exception {
		AtomicInteger count = new AtomicInteger();
		Callable<Integer> callable = count::incrementAndGet;
		MemoizedCallable<Integer> memoized = MemoizedCallable.of(callable);
		assertThat(memoized.call()).isEqualTo(1);
		assertThat(memoized.call()).isEqualTo(1);
		assertThat(memoized.call()).isEqualTo(1);
		assertThat(count.get()).isEqualTo(1);
	}

}
