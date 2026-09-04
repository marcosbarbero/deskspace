package com.marcosbarbero.deskspace.shared.adapter.in.web;

import com.marcosbarbero.deskspace.api.model.ApiZone;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Binds a query parameter to the generated enum by the value in the spec.
 *
 * Spring's default enum binding uses the constant name, so `zone=quiet` fails against a
 * constant called `QUIET` and the caller gets a 400 for a value the spec says is valid.
 * The spec's value is the contract; the constant name is an accident of code generation.
 *
 * Unknown values throw here, which Spring reports as a type mismatch and
 * ValidationProblemAdvice turns into the problem shape the contract promises.
 */
@Component
public class ApiZoneConverter implements Converter<String, ApiZone> {

	@Override
	public ApiZone convert(String source) {
		return ApiZone.fromValue(source);
	}

}
