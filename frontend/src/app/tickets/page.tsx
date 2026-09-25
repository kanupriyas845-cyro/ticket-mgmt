import { Suspense } from "react";
import { TicketListPage } from "@/components/tickets/ticket-list-page";

function TicketListFallback() {
  return (
    <section>
      <h1>Tickets</h1>
      <p>Loading…</p>
    </section>
  );
}

export default function TicketsPage() {
  return (
    <Suspense fallback={<TicketListFallback />}>
      <TicketListPage />
    </Suspense>
  );
}
