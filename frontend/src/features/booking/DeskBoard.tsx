import { useState } from 'react';

import { api } from '../../api/client';

import { useDesks } from './useDesks';

type Props = {
  date: string;
  bookedBy: string;
};

export function DeskBoard({ date, bookedBy }: Props) {
  const { desks, loading, error, reload } = useDesks(date);
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

  if (loading) {
    return <p role="status">Loading desks</p>;
  }
  if (error) {
    return <p role="alert">{error}</p>;
  }

  return (
    <section aria-label={`Desks on ${date}`}>
      {conflict && <p role="alert">{conflict}</p>}
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
