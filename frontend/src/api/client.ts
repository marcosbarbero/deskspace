import createClient from 'openapi-fetch';

import type { components, paths } from './schema';

/**
 * The only place in this app that knows how to reach the API. Every type below
 * comes from api/openapi.yaml via `npm run gen`, so a change to the contract
 * that this code has not caught up with is a type error, not a runtime surprise.
 */
/**
 * Paths in the spec are absolute (`/api/...`), and both `fetch` in Node and the
 * dev-server proxy want a base to resolve them against. In the browser that is
 * the page's own origin, so the Vite proxy still handles `/api`.
 */
const baseUrl = typeof window === 'undefined' ? 'http://localhost:8080' : window.location.origin;

export const api = createClient<paths>({
  baseUrl,
  // Resolved at call time rather than captured at module load, so a test can
  // substitute the global without having to rebuild the client.
  fetch: (request) => globalThis.fetch(request),
});

export type Desk = components['schemas']['Desk'];
export type Booking = components['schemas']['Booking'];
export type Problem = components['schemas']['Problem'];
export type Zone = components['schemas']['Zone'];
