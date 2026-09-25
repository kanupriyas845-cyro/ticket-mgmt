"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { TicketForm } from "@/components/forms/ticket-form";
import type { TicketFormValues } from "@/components/forms/ticket-form";
import { CommentSection } from "@/components/tickets/comment-section";
import { TicketStatusActions } from "@/components/tickets/ticket-status-actions";
import {
  ApiError,
  changeTicketStatus,
  getTicket,
  updateTicket,
} from "@/lib/api";
import { apiErrorMessage, fieldErrorsFromApi } from "@/lib/form-errors";
import type { FieldErrors } from "@/lib/form-errors";
import { formatDateTime } from "@/lib/format";
import {
  isEditableStatus,
  isTerminalStatus,
} from "@/lib/status-transitions";
import { PRIORITY_LABELS, STATUS_LABELS } from "@/lib/ticket-constants";
import type { Ticket, TicketStatus } from "@/types/ticket";
import styles from "./ticket-details.module.css";

type TicketDetailsPageProps = {
  ticketId: number;
  startInEditMode?: boolean;
};

function toFormValues(ticket: Ticket): TicketFormValues {
  return {
    title: ticket.title,
    description: ticket.description ?? "",
    priority: ticket.priority,
  };
}

export function TicketDetailsPage({
  ticketId,
  startInEditMode = false,
}: TicketDetailsPageProps) {
  const router = useRouter();
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [editing, setEditing] = useState(startInEditMode);
  const [formValues, setFormValues] = useState<TicketFormValues | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const [statusError, setStatusError] = useState<string | null>(null);
  const [statusChanging, setStatusChanging] = useState(false);

  const loadTicket = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    setNotFound(false);
    try {
      const data = await getTicket(ticketId);
      setTicket(data);
      setFormValues(toFormValues(data));
      if (startInEditMode && !isEditableStatus(data.status)) {
        setEditing(false);
        setFormError("This ticket cannot be edited.");
      }
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        setNotFound(true);
      } else {
        setLoadError(apiErrorMessage(error, "Failed to load ticket."));
      }
    } finally {
      setLoading(false);
    }
  }, [ticketId, startInEditMode]);

  useEffect(() => {
    loadTicket();
  }, [loadTicket]);

  async function handleSave() {
    if (!ticket || !formValues) return;

    setSaving(true);
    setFormError(null);
    setFieldErrors({});

    try {
      const updated = await updateTicket(ticketId, {
        title: formValues.title.trim(),
        description: formValues.description.trim() || undefined,
        priority: formValues.priority,
      });
      setTicket(updated);
      setFormValues(toFormValues(updated));
      setEditing(false);
      router.replace(`/tickets/${ticketId}`);
    } catch (error) {
      setFieldErrors(fieldErrorsFromApi(error));
      setFormError(apiErrorMessage(error, "Failed to save ticket."));
    } finally {
      setSaving(false);
    }
  }

  async function handleStatusChange(targetStatus: TicketStatus) {
    setStatusChanging(true);
    setStatusError(null);

    try {
      const updated = await changeTicketStatus(ticketId, { status: targetStatus });
      setTicket(updated);
      setFormValues(toFormValues(updated));
      if (!isEditableStatus(updated.status)) {
        setEditing(false);
      }
    } catch (error) {
      setStatusError(
        apiErrorMessage(
          error,
          "Cannot change status. Please try again."
        )
      );
    } finally {
      setStatusChanging(false);
    }
  }

  function handleStartEdit() {
    if (!ticket || !isEditableStatus(ticket.status)) {
      setFormError("This ticket cannot be edited.");
      return;
    }
    setEditing(true);
    setFormError(null);
    setFieldErrors({});
  }

  function handleCancelEdit() {
    if (ticket) {
      setFormValues(toFormValues(ticket));
    }
    setEditing(false);
    setFieldErrors({});
    setFormError(null);
    router.replace(`/tickets/${ticketId}`);
  }

  if (loading) {
    return (
      <section className={styles.section}>
        <p className={styles.muted}>Loading ticket…</p>
      </section>
    );
  }

  if (notFound) {
    return (
      <section className={styles.section}>
        <Link href="/tickets" className={styles.backLink}>← Back to tickets</Link>
        <h1 className={styles.title}>Ticket not found</h1>
        <p className={styles.muted}>Ticket #{ticketId} does not exist.</p>
      </section>
    );
  }

  if (loadError || !ticket || !formValues) {
    return (
      <section className={styles.section}>
        <Link href="/tickets" className={styles.backLink}>← Back to tickets</Link>
        <div className={styles.errorBanner} role="alert">
          {loadError ?? "Something went wrong."}
        </div>
      </section>
    );
  }

  const editable = isEditableStatus(ticket.status);
  const canComment = !isTerminalStatus(ticket.status);

  return (
    <section className={styles.section}>
      <div className={styles.headerRow}>
        <Link href="/tickets" className={styles.backLink}>← Back to tickets</Link>
        {editable && !editing && (
          <button type="button" className={styles.secondaryButton} onClick={handleStartEdit}>
            Edit
          </button>
        )}
      </div>

      {formError && (
        <div className={styles.errorBanner} role="alert">{formError}</div>
      )}
      {statusError && (
        <div className={styles.errorBanner} role="alert">
          Cannot change status: {statusError}
        </div>
      )}

      {editing ? (
        <>
          <h1 className={styles.title}>Edit ticket</h1>
          <TicketForm
            values={formValues}
            onChange={setFormValues}
            onSubmit={handleSave}
            onCancel={handleCancelEdit}
            fieldErrors={fieldErrors}
            submitLabel="Save"
            submitting={saving}
          />
        </>
      ) : (
        <>
          <h1 className={styles.title}>{ticket.title}</h1>

          <dl className={styles.metaGrid}>
            <div>
              <dt>Status</dt>
              <dd>{STATUS_LABELS[ticket.status]}</dd>
            </div>
            <div>
              <dt>Priority</dt>
              <dd>{PRIORITY_LABELS[ticket.priority]}</dd>
            </div>
            <div>
              <dt>Assignee</dt>
              <dd>{ticket.assignee?.displayName ?? "Unassigned"}</dd>
            </div>
            <div>
              <dt>Created</dt>
              <dd>{formatDateTime(ticket.createdAt)}</dd>
            </div>
            <div>
              <dt>Updated</dt>
              <dd>{formatDateTime(ticket.updatedAt)}</dd>
            </div>
          </dl>

          <div className={styles.descriptionBlock}>
            <h2 className={styles.sectionHeading}>Description</h2>
            <p>{ticket.description || "—"}</p>
          </div>

          <TicketStatusActions
            currentStatus={ticket.status}
            onTransition={handleStatusChange}
            disabled={statusChanging}
          />
        </>
      )}

      <CommentSection ticketId={ticketId} canComment={canComment} />
    </section>
  );
}
