import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import type { Desk } from '../../api/client';

import { DeskBoard } from './DeskBoard';

const DATE = '2026-03-02';

const A01: Desk = {
  id: '11111111-0000-0000-0000-000000000001',
  label: 'A-01',
  zone: 'quiet',
  available: true,
};
const B01: Desk = {
  id: '11111111-0000-0000-0000-000000000003',
  label: 'B-01',
  zone: 'collaboration',
  available: false,
};

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  });
}

describe('DeskBoard', () => {
  const fetchMock = vi.fn();

  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock);
    fetchMock.mockReset();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('lists desks and marks the taken ones', async () => {
    fetchMock.mockResolvedValueOnce(jsonResponse([A01, B01]));

    render(<DeskBoard date={DATE} bookedBy="ada@example.com" />);

    expect(await screen.findByText('A-01')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Book A-01' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Book B-01' })).not.toBeInTheDocument();
    expect(screen.getByText('· taken')).toBeInTheDocument();
  });

  it('asks the API for the date it was given', async () => {
    fetchMock.mockResolvedValueOnce(jsonResponse([]));

    render(<DeskBoard date={DATE} bookedBy="ada@example.com" />);

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    const request = fetchMock.mock.calls[0]?.[0] as Request;
    expect(request.url).toContain(`date=${DATE}`);
  });

  it('books a desk and reloads', async () => {
    fetchMock
      .mockResolvedValueOnce(jsonResponse([A01]))
      .mockResolvedValueOnce(
        jsonResponse(
          { id: 'b', deskId: A01.id, date: DATE, bookedBy: 'ada@example.com', status: 'confirmed' },
          201,
        ),
      )
      .mockResolvedValueOnce(jsonResponse([{ ...A01, available: false }]));

    render(<DeskBoard date={DATE} bookedBy="ada@example.com" />);
    await userEvent.click(await screen.findByRole('button', { name: 'Book A-01' }));

    expect(await screen.findByText('· taken')).toBeInTheDocument();
    const booking = fetchMock.mock.calls[1]?.[0] as Request;
    expect(booking.method).toBe('POST');
  });

  it('shows the problem title when a booking conflicts', async () => {
    fetchMock
      .mockResolvedValueOnce(jsonResponse([A01]))
      .mockResolvedValueOnce(
        jsonResponse({ title: 'Desk already booked', status: 409, detail: 'taken' }, 409),
      );

    render(<DeskBoard date={DATE} bookedBy="ada@example.com" />);
    await userEvent.click(await screen.findByRole('button', { name: 'Book A-01' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Desk already booked');
  });

  it('shows the problem title when the list fails', async () => {
    fetchMock.mockResolvedValueOnce(
      jsonResponse({ title: 'Invalid request', status: 400 }, 400),
    );

    render(<DeskBoard date={DATE} bookedBy="ada@example.com" />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid request');
  });
});
