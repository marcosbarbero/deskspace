import { useState } from 'react';

import { api, type Zone } from '../../api/client';

import { ZoneFilter } from './ZoneFilter';
import { useDesks } from './useDesks';

type Props = {
  date: string;
  bookedBy: string;
};

export function DeskBoard({ date, bookedBy }: Props) {
  const [zone, setZone] = useState<Zone | undefined>(undefined);
  const { desks, loading, error, reload } = useDesks(date, zone);
  const [conflict, setConflict] = useState<string | null>(null);

  async function book(deskId: string) {
    setConflict(null);
    const { error: bookingError } = await api.POST('/api/bookings', {
      body: { deskId, date, bookedBy },
    });
    if (bookingError) {
      setConflict(bookingError.title);
      return;
    }
    reload();
  }

  return (
    <section aria-label={`Desks on ${date}`}>
      <ZoneFilter zone={zone} onChange={setZone} />
      {conflict && <p role="alert">{conflict}</p>}
      {loading && <p role="status">Loading desks</p>}
      {error && <p role="alert">{error}</p>}
      <ul>
        {desks.map((desk) => (
          <li key={desk.id}>
            <span>{desk.label}</span>
            <span> · {desk.zone}</span>
            {desk.available ? (
              <button type="button" onClick={() => void book(desk.id)}>
                Book {desk.label}
              </button>
            ) : (
              <span> · taken</span>
            )}
          </li>
        ))}
      </ul>
    </section>
  );
}
