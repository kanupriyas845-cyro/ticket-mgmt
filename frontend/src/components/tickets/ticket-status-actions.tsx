"use client";

import { getAvailableStatusActions } from "@/lib/status-transitions";
import type { TicketStatus } from "@/types/ticket";
import styles from "./ticket-details.module.css";

type TicketStatusActionsProps = {
  currentStatus: TicketStatus;
  onTransition: (targetStatus: TicketStatus) => void;
  disabled?: boolean;
};

export function TicketStatusActions({
  currentStatus,
  onTransition,
  disabled = false,
}: TicketStatusActionsProps) {
  const actions = getAvailableStatusActions(currentStatus);

  if (actions.length === 0) {
    return null;
  }

  return (
    <div className={styles.statusActions}>
      <h2 className={styles.sectionHeading}>Actions</h2>
      <div className={styles.actionButtons}>
        {actions.map((action) => (
          <button
            key={action.targetStatus}
            type="button"
            className={
              action.targetStatus === "CANCELLED"
                ? styles.dangerButton
                : styles.primaryButton
            }
            disabled={disabled}
            onClick={() => onTransition(action.targetStatus)}
          >
            {action.label}
          </button>
        ))}
      </div>
    </div>
  );
}
