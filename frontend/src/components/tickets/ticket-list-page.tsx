"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { ApiError, listTickets } from "@/lib/api";
import { useDebouncedValue } from "@/lib/hooks/use-debounced-value";
import { formatDateTime } from "@/lib/format";
import {
  PRIORITY_LABELS,
  STATUS_LABELS,
  TICKET_STATUSES,
} from "@/lib/ticket-constants";
import type { PageResponse } from "@/types/api";
import type { TicketSummary } from "@/types/ticket";
import styles from "./ticket-list.module.css";

const PAGE_SIZE = 20;
const SEARCH_DEBOUNCE_MS = 300;

function buildTicketsUrl(params: {
  page: number;
  search?: string;
  status?: string | null;
}): string {
  const query = new URLSearchParams();
  if (params.page > 0) query.set("page", String(params.page));
  if (params.search) query.set("search", params.search);
  if (params.status) query.set("status", params.status);
  const qs = query.toString();
  return qs ? `/tickets?${qs}` : "/tickets";
}

export function TicketListPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const page = Number(searchParams.get("page") ?? "0");
  const urlSearch = searchParams.get("search") ?? "";
  const urlStatus = searchParams.get("status") ?? "";

  const [searchInput, setSearchInput] = useState(urlSearch);
  const debouncedSearch = useDebouncedValue(searchInput, SEARCH_DEBOUNCE_MS);

  const [data, setData] = useState<PageResponse<TicketSummary> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Sync debounced search to URL (spec/ui-flow.md: debounce 300ms)
  useEffect(() => {
    if (debouncedSearch === urlSearch) return;
    router.replace(
      buildTicketsUrl({ page: 0, search: debouncedSearch, status: urlStatus || null })
    );
  }, [debouncedSearch, urlSearch, urlStatus, router]);

  // Keep input in sync when URL changes externally (e.g. back button)
  useEffect(() => {
    setSearchInput(urlSearch);
  }, [urlSearch]);

  const fetchTickets = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await listTickets({
        page,
        size: PAGE_SIZE,
        sort: "createdAt,desc",
        search: urlSearch || undefined,
        status: urlStatus ? [urlStatus] : undefined,
      });
      setData(result);
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : "Failed to load tickets. Is the backend running?";
      setError(message);
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [page, urlSearch, urlStatus]);

  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  function handleStatusChange(status: string) {
    router.push(
      buildTicketsUrl({
        page: 0,
        search: urlSearch,
        status: status || null,
      })
    );
  }

  function handlePageChange(nextPage: number) {
    router.push(
      buildTicketsUrl({
        page: nextPage,
        search: urlSearch,
        status: urlStatus || null,
      })
    );
  }

  function clearSearch() {
    setSearchInput("");
    router.push(
      buildTicketsUrl({ page: 0, search: "", status: urlStatus || null })
    );
  }

  const tickets = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const currentPage = data?.page ?? 0;

  return (
    <section className={styles.section}>
      <div className={styles.header}>
        <h1 className={styles.title}>Tickets</h1>
        <Link href="/tickets/new" className={styles.newButton}>
          + New ticket
        </Link>
      </div>

      <div className={styles.filters}>
        <div className={styles.searchGroup}>
          <label htmlFor="ticket-search" className={styles.label}>
            Search
          </label>
          <div className={styles.searchRow}>
            <input
              id="ticket-search"
              type="search"
              className={styles.input}
              placeholder="Search by title or description"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              disabled={loading}
            />
            {searchInput && (
              <button
                type="button"
                className={styles.clearButton}
                onClick={clearSearch}
                disabled={loading}
              >
                Clear
              </button>
            )}
          </div>
        </div>

        <div className={styles.statusGroup}>
          <label htmlFor="ticket-status" className={styles.label}>
            Status
          </label>
          <select
            id="ticket-status"
            className={styles.select}
            value={urlStatus}
            onChange={(e) => handleStatusChange(e.target.value)}
            disabled={loading}
          >
            <option value="">All</option>
            {TICKET_STATUSES.map((code) => (
              <option key={code} value={code}>
                {STATUS_LABELS[code]}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error && (
        <div className={styles.errorBanner} role="alert">
          {error}
        </div>
      )}

      {loading && <p className={styles.muted}>Loading tickets…</p>}

      {!loading && !error && tickets.length === 0 && (
        <div className={styles.empty}>
          <p>No tickets yet. Create your first ticket.</p>
          <Link href="/tickets/new">Create ticket</Link>
        </div>
      )}

      {!loading && !error && tickets.length > 0 && (
        <>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Title</th>
                <th>Status</th>
                <th>Priority</th>
                <th>Assignee</th>
                <th>Updated</th>
              </tr>
            </thead>
            <tbody>
              {tickets.map((ticket) => (
                <tr key={ticket.id}>
                  <td>
                    <Link
                      href={`/tickets/${ticket.id}`}
                      className={styles.titleLink}
                    >
                      {ticket.title}
                    </Link>
                  </td>
                  <td>
                    <span className={styles.badge}>{ticket.status}</span>
                  </td>
                  <td>{PRIORITY_LABELS[ticket.priority] ?? ticket.priority}</td>
                  <td>{ticket.assignee?.displayName ?? "—"}</td>
                  <td className={styles.muted}>
                    {formatDateTime(ticket.updatedAt)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className={styles.pagination}>
              <button
                type="button"
                className={styles.pageButton}
                disabled={currentPage <= 0}
                onClick={() => handlePageChange(currentPage - 1)}
              >
                ← Prev
              </button>
              <span className={styles.pageInfo}>
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                type="button"
                className={styles.pageButton}
                disabled={currentPage >= totalPages - 1}
                onClick={() => handlePageChange(currentPage + 1)}
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </section>
  );
}
