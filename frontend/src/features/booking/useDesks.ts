import { useCallback, useEffect, useState } from 'react';

import { api, type Desk } from '../../api/client';

type State = {
  desks: Desk[];
  loading: boolean;
  error: string | null;
};

/**
 * Desks for one date. Errors are surfaced as the API's own problem title, never
 * as a generic message, because the title is the thing the API promised to send.
 */
export function useDesks(date: string) {
  const [state, setState] = useState<State>({ desks: [], loading: true, error: null });

  const load = useCallback(async () => {
    setState((s) => ({ ...s, loading: true, error: null }));
    const { data, error } = await api.GET('/api/desks', { params: { query: { date } } });
    if (error) {
      setState({ desks: [], loading: false, error: error.title });
      return;
    }
    setState({ desks: data ?? [], loading: false, error: null });
  }, [date]);

  useEffect(() => {
    void load();
  }, [load]);

  return { ...state, reload: load };
}
