import path from 'node:path';

import { MatchersV3, PactV3 } from '@pact-foundation/pact';
import createClient from 'openapi-fetch';
import { afterAll, describe, expect, it } from 'vitest';

import type { paths } from './schema';

const { uuid, like, eachLike, integer } = MatchersV3;

/**
 * The consumer contract.
 *
 * api/openapi.yaml says what the API *can* do. This file says what this client
 * actually *depends on*, which is a much smaller set, and it is the set the
 * backend has to keep working. A field the spec allows but nobody reads can be
 * removed; a field named here cannot.
 *
 * The pact written here is verified against the real backend by
 * BookingContractVerificationTest on the provider side.
 */
const pact = new PactV3({
  consumer: 'deskspace-frontend',
  provider: 'deskspace-backend',
  dir: path.resolve(process.cwd(), '..', 'pacts'),
  logLevel: 'warn',
});

const DESK_ID = '11111111-0000-0000-0000-000000000001';
const DATE = '2026-03-02';

function clientFor(baseUrl: string) {
  return createClient<paths>({ baseUrl, fetch: (request) => globalThis.fetch(request) });
}

describe('deskspace-backend contract', () => {
  afterAll(() => {
    // PactV3 writes the file when the last interaction completes.
  });

  it('returns desks with availability for a date', async () => {
    await pact
      .given('desk A-01 exists and is free on 2026-03-02')
      .uponReceiving('a request for desks on a date')
      .withRequest({
        method: 'GET',
        path: '/api/desks',
        query: { date: DATE },
      })
      .willRespondWith({
        status: 200,
        headers: { 'Content-Type': 'application/json' },
        body: eachLike({
          id: uuid(DESK_ID),
          label: like('A-01'),
          zone: like('quiet'),
          available: like(true),
        }),
      })
      .executeTest(async (server) => {
        const { data, error } = await clientFor(server.url).GET('/api/desks', {
          params: { query: { date: DATE } },
        });

        expect(error).toBeUndefined();
        expect(data?.[0]?.label).toBe('A-01');
        expect(data?.[0]?.available).toBe(true);
      });
  });

  it('refuses a desk that is already booked, with a problem the UI can show', async () => {
    await pact
      .given('desk A-01 is already booked on 2026-03-02')
      .uponReceiving('a booking for a desk that is taken')
      .withRequest({
        method: 'POST',
        path: '/api/bookings',
        headers: { 'Content-Type': 'application/json' },
        body: { deskId: uuid(DESK_ID), date: DATE, bookedBy: like('ada@example.com') },
      })
      .willRespondWith({
        status: 409,
        headers: { 'Content-Type': 'application/json' },
        // The board shows this title verbatim: see docs/ux/desk-board.md.
        body: { title: like('Desk already booked'), status: integer(409) },
      })
      .executeTest(async (server) => {
        const { error } = await clientFor(server.url).POST('/api/bookings', {
          body: { deskId: DESK_ID, date: DATE, bookedBy: 'ada@example.com' },
        });

        expect(error?.title).toBe('Desk already booked');
      });
  });
});
