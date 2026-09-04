import { DeskBoard } from './features/booking/DeskBoard';

export function App() {
  const today = new Date().toISOString().slice(0, 10);
  return (
    <main>
      <h1>Deskspace</h1>
      <DeskBoard date={today} bookedBy="you@example.com" />
    </main>
  );
}
