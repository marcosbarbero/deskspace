import type { Zone } from '../../api/client';

const ZONES: Zone[] = ['quiet', 'collaboration', 'lab'];

type Props = {
  zone: Zone | undefined;
  onChange: (zone: Zone | undefined) => void;
};

/**
 * The empty option is "every zone", not "none". They are different answers and
 * the API distinguishes them: an absent parameter means the whole room.
 */
export function ZoneFilter({ zone, onChange }: Props) {
  return (
    <label>
      Zone{' '}
      <select
        value={zone ?? ''}
        onChange={(event) => onChange(event.target.value === '' ? undefined : (event.target.value as Zone))}
      >
        <option value="">Every zone</option>
        {ZONES.map((value) => (
          <option key={value} value={value}>
            {value}
          </option>
        ))}
      </select>
    </label>
  );
}
