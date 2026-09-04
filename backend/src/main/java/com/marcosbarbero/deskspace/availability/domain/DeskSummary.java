package com.marcosbarbero.deskspace.availability.domain;

import java.util.UUID;

/**
 * What availability needs to know about a desk: an id, something to print, and which part
 * of the room it is in.
 *
 * Deliberately not the desk slice's {@code Desk}. Reusing that type would look like
 * saving a class and would actually be a shared kernel: availability would then be
 * recompiled by any change to a concept it does not own. The zone is a string here
 * because availability reports zones and has no opinion about them.
 */
public record DeskSummary(UUID id, String label, String zone) {
}
