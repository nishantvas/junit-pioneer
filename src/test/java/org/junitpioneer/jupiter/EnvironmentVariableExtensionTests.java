/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

@DisplayName("EnvironmentVariable extension")
class EnvironmentVariableExtensionTests extends AbstractPioneerTestEngineTests {

	@BeforeAll
	static void globalSetUp() {
		EnvironmentVariableUtils.set("set prop A", "old A");
		EnvironmentVariableUtils.set("set prop B", "old B");
		EnvironmentVariableUtils.set("set prop C", "old C");

		EnvironmentVariableUtils.clear("clear prop D");
		EnvironmentVariableUtils.clear("clear prop E");
		EnvironmentVariableUtils.clear("clear prop F");
	}

	@AfterAll
	static void globalTearDown() {
		assertThat(System.getenv("set prop A")).isEqualTo("old A");
		assertThat(System.getenv("set prop B")).isEqualTo("old B");
		assertThat(System.getenv("set prop C")).isEqualTo("old C");

		assertThat(System.getenv("clear prop D")).isNull();
		assertThat(System.getenv("clear prop E")).isNull();
		assertThat(System.getenv("clear prop F")).isNull();
	}

	@Nested
	@DisplayName("used with ClearEnvironmentVariable")
	@ClearEnvironmentVariable(key = "set prop A")
	class ClearEnvironmentVariableTests {

		@Test
		@DisplayName("should clear environment variable")
		@ClearEnvironmentVariable(key = "set prop B")
		void shouldClearEnvironmentVariable() {
			assertThat(System.getenv("set prop A")).isNull();
			assertThat(System.getenv("set prop B")).isNull();
			assertThat(System.getenv("set prop C")).isEqualTo("old C");

			assertThat(System.getenv("clear prop D")).isNull();
			assertThat(System.getenv("clear prop E")).isNull();
			assertThat(System.getenv("clear prop F")).isNull();
		}

		@Test
		@DisplayName("should be repeatable")
		@ClearEnvironmentVariable(key = "set prop B")
		@ClearEnvironmentVariable(key = "set prop C")
		void shouldBeRepeatable() {
			assertThat(System.getenv("set prop A")).isNull();
			assertThat(System.getenv("set prop B")).isNull();
			assertThat(System.getenv("set prop C")).isNull();

			assertThat(System.getenv("clear prop D")).isNull();
			assertThat(System.getenv("clear prop E")).isNull();
			assertThat(System.getenv("clear prop F")).isNull();
		}

	}

	@Nested
	@DisplayName("used with SetEnvironmentVariable")
	@SetEnvironmentVariable(key = "set prop A", value = "new A")
	class SetEnvironmentVariableTests {

		@Test
		@DisplayName("should set environment variable to value")
		@SetEnvironmentVariable(key = "set prop B", value = "new B")
		void shouldSetEnvironmentVariableToValue() {
			assertThat(System.getenv("set prop A")).isEqualTo("new A");
			assertThat(System.getenv("set prop B")).isEqualTo("new B");
			assertThat(System.getenv("set prop C")).isEqualTo("old C");

			assertThat(System.getenv("clear prop D")).isNull();
			assertThat(System.getenv("clear prop E")).isNull();
			assertThat(System.getenv("clear prop F")).isNull();
		}

		@Test
		@DisplayName("should be repeatable")
		@SetEnvironmentVariable(key = "set prop B", value = "new B")
		@SetEnvironmentVariable(key = "clear prop D", value = "new D")
		void shouldBeRepeatable() {
			assertThat(System.getenv("set prop A")).isEqualTo("new A");
			assertThat(System.getenv("set prop B")).isEqualTo("new B");
			assertThat(System.getenv("set prop C")).isEqualTo("old C");

			assertThat(System.getenv("clear prop D")).isEqualTo("new D");
			assertThat(System.getenv("clear prop E")).isNull();
			assertThat(System.getenv("clear prop F")).isNull();
		}

	}

	@Nested
	@DisplayName("used with both ClearEnvironmentVariable and SetEnvironmentVariable")
	@ClearEnvironmentVariable(key = "set prop A")
	@SetEnvironmentVariable(key = "clear prop D", value = "new D")
	class CombinedEnvironmentVariableTests {

		@Test
		@DisplayName("should be combinable")
		@ClearEnvironmentVariable(key = "set prop B")
		@SetEnvironmentVariable(key = "clear prop E", value = "new E")
		void clearAndSetEnvironmentVariableShouldBeCombinable() {
			assertThat(System.getenv("set prop A")).isNull();
			assertThat(System.getenv("set prop B")).isNull();
			assertThat(System.getenv("set prop C")).isEqualTo("old C");

			assertThat(System.getenv("clear prop D")).isEqualTo("new D");
			assertThat(System.getenv("clear prop E")).isEqualTo("new E");
			assertThat(System.getenv("clear prop F")).isNull();
		}

		@Test
		@DisplayName("method level should overwrite class level")
		@ClearEnvironmentVariable(key = "clear prop D")
		@SetEnvironmentVariable(key = "set prop A", value = "new A")
		void methodLevelShouldOverwriteClassLevel() {
			assertThat(System.getenv("set prop A")).isEqualTo("new A");
			assertThat(System.getenv("set prop B")).isEqualTo("old B");
			assertThat(System.getenv("set prop C")).isEqualTo("old C");

			assertThat(System.getenv("clear prop D")).isNull();
			assertThat(System.getenv("clear prop E")).isNull();
			assertThat(System.getenv("clear prop F")).isNull();
		}

	}

	@DisplayName("with nested classes")
	@ClearEnvironmentVariable(key = "set prop A")
	@SetEnvironmentVariable(key = "set prop B", value = "new B")
	@Nested
	class NestedEnvironmentVariableTests extends AbstractPioneerTestEngineTests {

		@Nested
		@DisplayName("without EnvironmentVariable annotations")
		class NestedClass {

			@Test
			@DisplayName("environment variables should be set from enclosed class when they are not provided in nested")
			public void shouldSetEnvironmentVariableFromEnclosedClass() {
				assertThat(System.getenv("set prop A")).isNull();
				assertThat(System.getenv("set prop B")).isEqualTo("new B");
			}

		}

		@Nested
		@SetEnvironmentVariable(key = "set prop B", value = "newer B")
		@DisplayName("with SetEnvironmentVariable annotation")
		class AnnotatedNestedClass {

			@Test
			@DisplayName("environment variable should be set from nested class when it is provided")
			public void shouldSetEnvironmentVariableFromNestedClass() {
				assertThat(System.getenv("set prop B")).isEqualTo("newer B");
			}

			@Test
			@SetEnvironmentVariable(key = "set prop B", value = "newest B")
			@DisplayName("environment variable should be set from method when it is provided")
			public void shouldSetEnvironmentVariableFromMethodOfNestedClass() {
				assertThat(System.getenv("set prop B")).isEqualTo("newest B");
			}

		}

	}

	@Nested
	@DisplayName("used with incorrect configuration")
	class ConfigurationFailureTests {

		@Test
		@DisplayName("should fail when clear and set same environment variable")
		void shouldFailWhenClearAndSetSameEnvironmentVariable() {
			ExecutionEventRecorder eventRecorder = executeTests(MethodLevelInitializationFailureTestCase.class,
				"shouldFailWhenClearAndSetSameEnvironmentVariable");

			assertExtensionConfigurationFailure(eventRecorder.getFailedTestFinishedEvents());
		}

		@Test
		@DisplayName("should fail when clear same environment variable twice")
		@Disabled("This can't happen at the moment, because Jupiter's annotation tooling "
				+ "deduplicates identical annotations like the ones required for this test: "
				+ "https://github.com/junit-team/junit5/issues/2131")
		void shouldFailWhenClearSameEnvironmentVariableTwice() {
			ExecutionEventRecorder eventRecorder = executeTests(MethodLevelInitializationFailureTestCase.class,
				"shouldFailWhenClearSameEnvironmentVariableTwice");

			assertExtensionConfigurationFailure(eventRecorder.getFailedTestFinishedEvents());
		}

		@Test
		@DisplayName("should fail when set same environment variable twice")
		void shouldFailWhenSetSameEnvironmentVariableTwice() {
			ExecutionEventRecorder eventRecorder = executeTests(MethodLevelInitializationFailureTestCase.class,
				"shouldFailWhenSetSameEnvironmentVariableTwice");

			assertExtensionConfigurationFailure(eventRecorder.getFailedTestFinishedEvents());
		}

	}

	static class MethodLevelInitializationFailureTestCase {

		@Test
		@DisplayName("clearing and setting the same variable")
		@ClearEnvironmentVariable(key = "set prop A")
		@SetEnvironmentVariable(key = "set prop A", value = "new A")
		void shouldFailWhenClearAndSetSameEnvironmentVariable() {
		}

		@Test
		@ClearEnvironmentVariable(key = "set prop A")
		@ClearEnvironmentVariable(key = "set prop A")
		void shouldFailWhenClearSameEnvironmentVariableTwice() {
		}

		@Test
		@SetEnvironmentVariable(key = "set prop A", value = "new A")
		@SetEnvironmentVariable(key = "set prop A", value = "new B")
		void shouldFailWhenSetSameEnvironmentVariableTwice() {
		}

	}

	private static void assertExtensionConfigurationFailure(List<ExecutionEvent> failedTestFinishedEvents) {
		assertThat(failedTestFinishedEvents.size()).isEqualTo(1);
		//@formatter:off
		Throwable thrown = failedTestFinishedEvents.get(0)
				.getPayload(TestExecutionResult.class)
				.flatMap(TestExecutionResult::getThrowable)
				.orElseThrow(AssertionError::new);
		//@formatter:on
		assertThat(thrown).isInstanceOf(ExtensionConfigurationException.class);
	}

}
