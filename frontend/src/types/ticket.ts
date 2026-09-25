import type { UserSummary } from "@/types/api";

export type TicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED"
  | "CANCELLED";

export type TicketPriority = "LOW" | "MEDIUM" | "HIGH";

export type Ticket = {
  id: number;
  title: string;
  description: string | null;
  status: TicketStatus;
  priority: TicketPriority;
  assignee: UserSummary | null;
  createdAt: string;
  updatedAt: string;
};

export type TicketSummary = {
  id: number;
  title: string;
  status: TicketStatus;
  priority: TicketPriority;
  assignee: UserSummary | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateTicketInput = {
  title: string;
  description?: string;
  priority?: TicketPriority;
};

export type UpdateTicketInput = {
  title?: string;
  description?: string;
  priority?: TicketPriority;
};

export type ChangeTicketStatusInput = {
  status: TicketStatus;
};

export type TicketListParams = {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
  status?: string[];
  priority?: TicketPriority;
};
