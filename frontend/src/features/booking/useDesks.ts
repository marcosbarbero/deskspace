import { useCallback, useEffect, useState } from 'react';

import { api, type Desk } from '../../api/client';

type State = {
  desks: Desk[];
  loading: boolean;
  error: string | null;
};

const INITIAL: State = { desks: [], loading: true, error: null };

/**
 * Desks for one date.
 *
 * The fetch lives inside the effect and nothing sets state synchronously while
 * the effect body runs, which is what stops the cascading re-render the React
 * lint rule warns about. Reloading bumps a counter rather than calling the
 * fetch directly, so there is exactly one place that talks to the API.
 *
 * Errors surface the API's own problem title, never a message invented here:
 * see docs/ux/desk-board.md.
 */
export function useDesks(date: string) {
  const [state, setState] = useState<State>(INITIAL);
  const [reloads, setReloads] = useState(0);

  useEffect(() => {
    let cancelled = false;

    void (async () => {
      const { data, error } = await api.GET('/api/desks', { params: { query: { date } } });
      if (cancelled) {
        return;
      }
      setState(
        error ? { desks: [], loading: false, error: error.title } : { desks: data ?? [], loading: false, error: null },
      );
    })();

    return () => {
      cancelled = true;
    };
  }, [date, reloads]);

  const reload = useCallback(() => setReloads((n) => n + 1), []);

  return { ...state, reload };
}
