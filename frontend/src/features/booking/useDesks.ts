import { useCallback, useEffect, useState } from 'react';

import { api, type Desk, type Zone } from '../../api/client';

type State = {
  desks: Desk[];
  loading: boolean;
  error: string | null;
};

const INITIAL: State = { desks: [], loading: true, error: null };

/**
 * Desks for one date, optionally limited to one zone.
 *
 * The zone goes to the API rather than filtering rows already on screen. The
 * server is the only thing that knows the whole room, and a client-side filter
 * would quietly become wrong the moment the list is paged or capped.
 *
 * The fetch lives inside the effect and nothing sets state synchronously while
 * the effect body runs, which is what stops the cascading re-render the React
 * lint rule warns about. Errors surface the API's own problem title, never one
 * invented here: see docs/ux/desk-board.md.
 */
export function useDesks(date: string, zone?: Zone) {
  const [state, setState] = useState<State>(INITIAL);
  const [reloads, setReloads] = useState(0);

  useEffect(() => {
    let cancelled = false;

    void (async () => {
      const { data, error } = await api.GET('/api/desks', {
        params: { query: zone ? { date, zone } : { date } },
      });
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
  }, [date, zone, reloads]);

  const reload = useCallback(() => setReloads((n) => n + 1), []);

  return { ...state, reload };
}
