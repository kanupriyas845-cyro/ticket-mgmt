import Link from "next/link";

export function AppHeader() {
  return (
    <header
      style={{
        borderBottom: "1px solid var(--color-border)",
        background: "var(--color-surface)",
      }}
    >
      <nav
        style={{
          maxWidth: "960px",
          margin: "0 auto",
          padding: "1rem 1.5rem",
          display: "flex",
          alignItems: "center",
          gap: "1.5rem",
        }}
      >
        <Link href="/" style={{ fontWeight: 600, color: "var(--color-text)" }}>
          Ticket Management
        </Link>
        <Link href="/tickets">Tickets</Link>
        <Link href="/tickets/new">New ticket</Link>
      </nav>
    </header>
  );
}
