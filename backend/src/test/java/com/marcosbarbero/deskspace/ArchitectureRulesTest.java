package com.marcosbarbero.deskspace;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The slice rules from docs/adr/0001-package-by-feature.md, as a build failure.
 *
 * These were prose in CLAUDE.md first. Prose gets read once; a red test gets read every
 * time it goes red.
 */
@AnalyzeClasses(packages = "com.marcosbarbero.deskspace", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

	@ArchTest
	static final ArchRule desks_know_nothing_about_bookings = noClasses().that()
		.resideInAPackage("..desk..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..booking..", "..availability..")
		.because("a desk exists whether or not anyone booked it; availability composes the two");

	@ArchTest
	static final ArchRule bookings_do_not_reach_into_availability = noClasses().that()
		.resideInAPackage("..booking..")
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..availability..")
		.because("availability depends on booking, so the reverse edge would be a cycle");

	@ArchTest
	static final ArchRule no_cycles_between_slices = slices().matching("com.marcosbarbero.deskspace.(*)..")
		.should()
		.beFreeOfCycles();

	@ArchTest
	static final ArchRule no_field_injection = fields().should()
		.notBeAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
		.because("constructor injection makes a class testable without a container");

	@ArchTest
	static final ArchRule domain_does_not_depend_on_the_wire_format = noClasses().that()
		.resideInAnyPackage("..desk..", "..booking..")
		.and()
		.haveSimpleNameNotEndingWith("Controller")
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..api.model..")
		.because("generated wire types change when the spec changes; the domain should not");

}
