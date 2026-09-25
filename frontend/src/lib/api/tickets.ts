import { apiRequest } from "@/lib/api/client";
import type { PageResponse } from "@/types/api";
import type {
  ChangeTicketStatusInput,
  CreateTicketInput,
  Ticket,
  TicketListParams,
  TicketSummary,
  UpdateTicketInput,
} from "@/types/ticket";

function buildQuery(params: TicketListParams): string {
  const searchParams = new URLSearchParams();

  if (params.page !== undefined) searchParams.set("page", String(params.page));
  if (params.size !== undefined) searchParams.set("size", String(params.size));
  if (params.sort) searchParams.set("sort", params.sort);
  if (params.search) searchParams.set("search", params.search);
  if (params.priority) searchParams.set("priority", params.priority);
  params.status?.forEach((code) => searchParams.append("status", code));

  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

export async function listTickets(
  params: TicketListParams = {}
): Promise<PageResponse<TicketSummary>> {
  return apiRequest<PageResponse<TicketSummary>>(
    `/api/v1/tickets${buildQuery(params)}`
  );
}

export async function getTicket(ticketId: number): Promise<Ticket> {
  return apiRequest<Ticket>(`/api/v1/tickets/${ticketId}`);
}

export async function createTicket(input: CreateTicketInput): Promise<Ticket> {
  return apiRequest<Ticket>("/api/v1/tickets", {
    method: "POST",
    body: input,
  });
}

export async function updateTicket(
  ticketId: number,
  input: UpdateTicketInput
): Promise<Ticket> {
  return apiRequest<Ticket>(`/api/v1/tickets/${ticketId}`, {
    method: "PATCH",
    body: input,
  });
}

export async function changeTicketStatus(
  ticketId: number,
  input: ChangeTicketStatusInput
): Promise<Ticket> {
  return apiRequest<Ticket>(`/api/v1/tickets/${ticketId}/status`, {
    method: "PATCH",
    body: input,
  });
}
