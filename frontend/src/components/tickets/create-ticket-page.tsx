"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { TicketForm, validateTicketForm } from "@/components/forms/ticket-form";
import type { TicketFormValues } from "@/components/forms/ticket-form";
import { createTicket } from "@/lib/api";
import { apiErrorMessage, fieldErrorsFromApi } from "@/lib/form-errors";
import type { FieldErrors } from "@/lib/form-errors";
import styles from "./ticket-details.module.css";

const initialValues: TicketFormValues = {
  title: "",
  description: "",
  priority: "MEDIUM",
};

export function CreateTicketPage() {
  const router = useRouter();
  const [values, setValues] = useState<TicketFormValues>(initialValues);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit() {
    const clientErrors = validateTicketForm(values);
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors);
      return;
    }

    setSubmitting(true);
    setFormError(null);
    setFieldErrors({});

    try {
      const ticket = await createTicket({
        title: values.title.trim(),
        description: values.description.trim() || undefined,
        priority: values.priority,
      });
      router.push(`/tickets/${ticket.id}`);
    } catch (error) {
      setFieldErrors(fieldErrorsFromApi(error));
      setFormError(
        apiErrorMessage(error, "Failed to create ticket. Please try again.")
      );
      setSubmitting(false);
    }
  }

  return (
    <section className={styles.section}>
      <div className={styles.headerRow}>
        <Link href="/tickets" className={styles.backLink}>← Back to tickets</Link>
      </div>
      <h1 className={styles.title}>New ticket</h1>

      {formError && (
        <div className={styles.errorBanner} role="alert">{formError}</div>
      )}

      <TicketForm
        values={values}
        onChange={setValues}
        onSubmit={handleSubmit}
        onCancel={() => router.push("/tickets")}
        fieldErrors={fieldErrors}
        submitLabel="Create"
        submitting={submitting}
      />
    </section>
  );
}
