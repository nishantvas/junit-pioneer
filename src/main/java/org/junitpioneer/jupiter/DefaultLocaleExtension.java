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

import java.util.Locale;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

class DefaultLocaleExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(DefaultLocaleExtension.class);

	private static final String KEY = "DefaultLocale";

	@Override
	public void beforeAll(ExtensionContext context) {
		PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DefaultLocale.class)
				.ifPresent(annotation -> setDefaultLocale(context, annotation));
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DefaultLocale.class)
				.ifPresent(annotation -> setDefaultLocale(context, annotation));
	}

	private void setDefaultLocale(ExtensionContext context, DefaultLocale annotation) {
		storeDefaultLocale(context);
		Locale configuredLocale = createLocale(annotation);
		Locale.setDefault(configuredLocale);
	}

	private void storeDefaultLocale(ExtensionContext context) {
		context.getStore(NAMESPACE).put(KEY, Locale.getDefault());
	}

	private static Locale createLocale(DefaultLocale annotation) {
		if (!annotation.value().isEmpty()) {
			return createFromLanguageTag(annotation);
		} else {
			return createFromParts(annotation);
		}
	}

	private static Locale createFromLanguageTag(DefaultLocale annotation) {
		if (!annotation.language().isEmpty() || !annotation.country().isEmpty() || !annotation.variant().isEmpty()) {
			throw new ExtensionConfigurationException(
				"@DefaultLocale can only be used with language tag if language, country, and variant are not set");
		}
		return Locale.forLanguageTag(annotation.value());
	}

	private static Locale createFromParts(DefaultLocale annotation) {
		String language = annotation.language();
		String country = annotation.country();
		String variant = annotation.variant();
		if (!language.isEmpty() && !country.isEmpty() && !variant.isEmpty()) {
			return new Locale(language, country, variant);
		} else if (!language.isEmpty() && !country.isEmpty()) {
			return new Locale(language, country);
		} else if (!language.isEmpty() && variant.isEmpty()) {
			return new Locale(language);
		} else {
			throw new ExtensionConfigurationException(
				"@DefaultLocale not configured correctly. When not using a language tag, specify either"
						+ "language, or language and country, or language and country and variant.");
		}
	}

	@Override
	public void afterEach(ExtensionContext context) {
		if (PioneerAnnotationUtils.isAnyAnnotationPresent(context, DefaultLocale.class)) {
			resetDefaultLocale(context);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		resetDefaultLocale(context);
	}

	private void resetDefaultLocale(ExtensionContext context) {
		Locale.setDefault(context.getStore(NAMESPACE).get(KEY, Locale.class));
	}

}
