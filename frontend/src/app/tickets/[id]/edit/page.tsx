import { TicketDetailsPage } from "@/components/tickets/ticket-details-page";

type EditTicketRouteProps = {
  params: Promise<{ id: string }>;
};

export default async function EditTicketRoute({ params }: EditTicketRouteProps) {
  const { id } = await params;
  const ticketId = Number(id);

  if (!Number.isFinite(ticketId) || ticketId < 1) {
    return (
      <section>
        <h1>Invalid ticket</h1>
        <p>The ticket ID is not valid.</p>
      </section>
    );
  }

  return <TicketDetailsPage ticketId={ticketId} startInEditMode />;
}
