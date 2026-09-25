"use client";

import { useState } from "react";
import { FormFieldError } from "@/components/forms/form-field-error";
import type { FieldErrors } from "@/lib/form-errors";
import styles from "./comment-form.module.css";

type CommentFormProps = {
  body: string;
  onChange: (body: string) => void;
  onSubmit: () => void;
  fieldErrors: FieldErrors;
  submitting?: boolean;
  disabled?: boolean;
};

export function CommentForm({
  body,
  onChange,
  onSubmit,
  fieldErrors,
  submitting = false,
  disabled = false,
}: CommentFormProps) {
  const [clientError, setClientError] = useState<string | undefined>();

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!body.trim()) {
      setClientError("must not be blank");
      return;
    }
    setClientError(undefined);
    onSubmit();
  }

  const bodyError = fieldErrors.body ?? clientError;

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <div className={styles.field}>
        <label htmlFor="comment-body">Add comment</label>
        <textarea
          id="comment-body"
          rows={3}
          placeholder="Write a comment…"
          value={body}
          disabled={disabled || submitting}
          onChange={(e) => onChange(e.target.value)}
          aria-invalid={!!bodyError}
        />
        <FormFieldError message={bodyError} />
      </div>
      <button
        type="submit"
        className={styles.submitButton}
        disabled={disabled || submitting || !body.trim()}
      >
        {submitting ? "Posting…" : "Post"}
      </button>
    </form>
  );
}
