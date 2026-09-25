"use client";

import { useCallback, useEffect, useState } from "react";
import { CommentForm } from "@/components/forms/comment-form";
import { createComment, listComments } from "@/lib/api";
import { apiErrorMessage, fieldErrorsFromApi } from "@/lib/form-errors";
import type { FieldErrors } from "@/lib/form-errors";
import { formatDateTime } from "@/lib/format";
import type { Comment } from "@/types/comment";
import styles from "./ticket-details.module.css";

type CommentSectionProps = {
  ticketId: number;
  canComment: boolean;
};

export function CommentSection({ ticketId, canComment }: CommentSectionProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [body, setBody] = useState("");
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const fetchComments = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const page = await listComments(ticketId, {
        page: 0,
        size: 50,
        sort: "createdAt,asc",
      });
      setComments(page.content);
    } catch (error) {
      setLoadError(
        apiErrorMessage(error, "Failed to load comments.")
      );
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  useEffect(() => {
    fetchComments();
  }, [fetchComments]);

  async function handleSubmit() {
    if (!body.trim()) {
      setFieldErrors({ body: "must not be blank" });
      return;
    }

    setSubmitting(true);
    setSubmitError(null);
    setFieldErrors({});

    try {
      await createComment(ticketId, { body: body.trim() });
      setBody("");
      await fetchComments();
    } catch (error) {
      setFieldErrors(fieldErrorsFromApi(error));
      setSubmitError(
        apiErrorMessage(error, "Failed to post comment.")
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className={styles.commentsSection}>
      <h2 className={styles.sectionHeading}>Comments</h2>

      {loading && <p className={styles.muted}>Loading comments…</p>}
      {loadError && (
        <div className={styles.errorBanner} role="alert">{loadError}</div>
      )}

      {!loading && !loadError && comments.length === 0 && (
        <p className={styles.muted}>No comments yet.</p>
      )}

      {!loading && comments.length > 0 && (
        <ul className={styles.commentList}>
          {comments.map((comment) => (
            <li key={comment.id} className={styles.commentItem}>
              <div className={styles.commentMeta}>
                <strong>{comment.author?.displayName ?? "Unknown"}</strong>
                <span className={styles.muted}>
                  {formatDateTime(comment.createdAt)}
                </span>
              </div>
              <p className={styles.commentBody}>{comment.body}</p>
            </li>
          ))}
        </ul>
      )}

      {canComment ? (
        <>
          {submitError && (
            <div className={styles.errorBanner} role="alert">{submitError}</div>
          )}
          <CommentForm
            body={body}
            onChange={setBody}
            onSubmit={handleSubmit}
            fieldErrors={fieldErrors}
            submitting={submitting}
          />
        </>
      ) : (
        <p className={styles.muted}>
          Comments cannot be added to closed or cancelled tickets.
        </p>
      )}
    </section>
  );
}
