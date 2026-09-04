package com.marcosbarbero.deskspace.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * The architecture, as a build failure.
 *
 * These were prose in an ADR first. Prose gets read once; a red test gets read every time
 * it goes red, which is the only difference that matters.
 *
 * See docs/adr/0008-ports-and-adapters-within-each-slice.md and
 * docs/adr/0009-events-between-slices.md for why each rule exists rather than what it
 * says.
 */
@AnalyzeClasses(packages = "com.marcosbarbero.deskspace", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

	private static final String ROOT = "com.marcosbarbero.deskspace.";

	private static final String DESK = ROOT + "desk..";

	private static final String BOOKING = ROOT + "booking..";

	private static final String AVAILABILITY = ROOT + "availability..";

	private static final String PUBLISHED_EVENTS = ROOT + "booking.domain.event..";

	// --- the hexagon -------------------------------------------------------

	@ArchTest
	static final ArchRule domain_depends_on_nothing_of_ours = noClasses().that()
		.resideInAPackage("..domain..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..application..", "..adapter..", "..api..")
		.because("the innermost layer is the one that must never be dragged along by a change "
				+ "of framework, storage or wire format");

	@ArchTest
	static final ArchRule application_never_reaches_into_an_adapter = noClasses().that()
		.resideInAPackage("..application..")
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..adapter..")
		.because("a use case that names its adapter has stopped being testable without one, "
				+ "and the port it declared has stopped meaning anything");

	@ArchTest
	static final ArchRule application_is_free_of_spring_web = noClasses().that()
		.resideInAPackage("..application..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("org.springframework.web..", "org.springframework.http..",
				"org.springframework.context.event..")
		.because("HTTP and the event transport are delivery details; they belong in adapters, "
				+ "which is what lets a use case be called from a queue tomorrow");

	@ArchTest
	static final ArchRule wire_types_stop_at_the_web_adapter = noClasses().that()
		.resideOutsideOfPackages("..adapter.in.web..", "..api..")
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..api.model..")
		.because("generated wire types change whenever the spec does, and the domain should not");

	// --- the slices --------------------------------------------------------

	@ArchTest
	static final ArchRule desks_know_nothing_about_anyone = noClasses().that()
		.resideInAPackage(DESK)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(BOOKING, AVAILABILITY)
		.because("a desk exists whether or not anybody booked it");

	@ArchTest
	static final ArchRule availability_knows_booking_only_through_its_events = noClasses().that()
		.resideInAPackage(AVAILABILITY)
		.should()
		.dependOnClassesThat(resideInAPackage(BOOKING).and(resideOutsideOfPackage(PUBLISHED_EVENTS)))
		.because("events are the published language between these two; a call in either "
				+ "direction would make them one deployable forever");

	@ArchTest
	static final ArchRule booking_never_hears_about_availability = noClasses().that()
		.resideInAPackage(BOOKING)
		.should()
		.dependOnClassesThat()
		.resideInAPackage(AVAILABILITY)
		.because("the write side must not know that a read model exists, or it will start " + "maintaining it");

	@ArchTest
	static final ArchRule only_an_adapter_may_cross_a_slice_boundary = noClasses().that()
		.resideInAPackage("com.marcosbarbero.deskspace.booking.application..")
		.or()
		.resideInAPackage("com.marcosbarbero.deskspace.availability.application..")
		.should()
		.dependOnClassesThat()
		.resideInAPackage(DESK)
		.because("crossing a boundary is allowed, and it belongs in one named adapter class "
				+ "so a diff shows it and this rule can find it");

	@ArchTest
	static final ArchRule no_cycles_between_slices = slices().matching("com.marcosbarbero.deskspace.(*)..")
		.should()
		.beFreeOfCycles();

	// --- conventions -------------------------------------------------------

	@ArchTest
	static final ArchRule no_field_injection = fields().should()
		.notBeAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
		.because("constructor injection is what makes a class testable without a container");

	@ArchTest
	static final ArchRule events_are_records_in_the_published_package = classes().that()
		.implement("com.marcosbarbero.deskspace.shared.event.DomainEvent")
		.should()
		.resideInAPackage("..domain.event..")
		.andShould()
		.beRecords()
		.because("an event is an immutable fact in a package other slices are allowed to read");

}
