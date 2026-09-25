"use client";

import { useState } from "react";
import { FormFieldError } from "@/components/forms/form-field-error";
import type { FieldErrors } from "@/lib/form-errors";
import { PRIORITY_LABELS } from "@/lib/ticket-constants";
import type { TicketPriority } from "@/types/ticket";
import styles from "./ticket-form.module.css";

const PRIORITIES: TicketPriority[] = ["LOW", "MEDIUM", "HIGH"];
const TITLE_MAX_LENGTH = 255;

export type TicketFormValues = {
  title: string;
  description: string;
  priority: TicketPriority;
};

type TicketFormProps = {
  values: TicketFormValues;
  onChange: (values: TicketFormValues) => void;
  onSubmit: () => void;
  onCancel: () => void;
  fieldErrors: FieldErrors;
  submitLabel: string;
  submitting?: boolean;
  disabled?: boolean;
};

function validateClient(values: TicketFormValues): FieldErrors {
  const errors: FieldErrors = {};
  if (!values.title.trim()) {
    errors.title = "Title is required";
  } else if (values.title.length > TITLE_MAX_LENGTH) {
    errors.title = "Title must be 255 characters or less";
  }
  return errors;
}

export function TicketForm({
  values,
  onChange,
  onSubmit,
  onCancel,
  fieldErrors,
  submitLabel,
  submitting = false,
  disabled = false,
}: TicketFormProps) {
  const [clientErrors, setClientErrors] = useState<FieldErrors>({});

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const nextClientErrors = validateClient(values);
    setClientErrors(nextClientErrors);
    if (Object.keys(nextClientErrors).length > 0) {
      return;
    }
    onSubmit();
  }

  const titleError = fieldErrors.title ?? clientErrors.title;
  const priorityError = fieldErrors.priority ?? clientErrors.priority;

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <div className={styles.field}>
        <label htmlFor="ticket-title">
          Title <span className={styles.required}>*</span>
        </label>
        <input
          id="ticket-title"
          type="text"
          maxLength={TITLE_MAX_LENGTH}
          value={values.title}
          disabled={disabled || submitting}
          onChange={(e) => onChange({ ...values, title: e.target.value })}
          aria-invalid={!!titleError}
        />
        <FormFieldError message={titleError} />
      </div>

      <div className={styles.field}>
        <label htmlFor="ticket-description">Description</label>
        <textarea
          id="ticket-description"
          rows={5}
          value={values.description}
          disabled={disabled || submitting}
          onChange={(e) => onChange({ ...values, description: e.target.value })}
        />
        <FormFieldError message={fieldErrors.description} />
      </div>

      <div className={styles.field}>
        <label htmlFor="ticket-priority">Priority</label>
        <select
          id="ticket-priority"
          value={values.priority}
          disabled={disabled || submitting}
          onChange={(e) =>
            onChange({ ...values, priority: e.target.value as TicketPriority })
          }
          aria-invalid={!!priorityError}
        >
          {PRIORITIES.map((code) => (
            <option key={code} value={code}>
              {PRIORITY_LABELS[code]}
            </option>
          ))}
        </select>
        <FormFieldError message={priorityError} />
      </div>

      <div className={styles.actions}>
        <button type="submit" className={styles.primaryButton} disabled={disabled || submitting}>
          {submitting ? "Saving…" : submitLabel}
        </button>
        <button
          type="button"
          className={styles.secondaryButton}
          onClick={onCancel}
          disabled={submitting}
        >
          Cancel
        </button>
      </div>
    </form>
  );
}

export function validateTicketForm(values: TicketFormValues): FieldErrors {
  return validateClient(values);
}
