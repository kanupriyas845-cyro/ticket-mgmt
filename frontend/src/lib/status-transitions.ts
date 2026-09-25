import type { TicketStatus } from "@/types/ticket";

/** Mirrors spec/state-machine.md §2 — UI hints only; backend enforces. */
const ALLOWED_TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["RESOLVED", "CANCELLED"],
  RESOLVED: ["CLOSED"],
  CLOSED: [],
  CANCELLED: [],
};

export type StatusAction = {
  targetStatus: TicketStatus;
  label: string;
};

const ACTION_LABELS: Record<TicketStatus, string> = {
  IN_PROGRESS: "Start work",
  CANCELLED: "Cancel ticket",
  RESOLVED: "Resolve",
  CLOSED: "Close",
  OPEN: "Reopen",
};

export function getAvailableStatusActions(
  currentStatus: TicketStatus
): StatusAction[] {
  return ALLOWED_TRANSITIONS[currentStatus].map((targetStatus) => ({
    targetStatus,
    label: ACTION_LABELS[targetStatus],
  }));
}

export function isTerminalStatus(status: TicketStatus): boolean {
  return status === "CLOSED" || status === "CANCELLED";
}

export function isEditableStatus(status: TicketStatus): boolean {
  return !isTerminalStatus(status);
}
