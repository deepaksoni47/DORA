const sampleCategories = [
  "Research Papers",
  "Tutorials",
  "Videos",
  "Articles",
  "Source Code",
];

export function SearchShell() {
  return (
    <main
      style={{
        minHeight: "100vh",
        padding: "48px 20px",
        display: "grid",
        placeItems: "center",
      }}
    >
      <section
        style={{
          width: "min(1100px, 100%)",
          background: "var(--surface)",
          border: "1px solid var(--surface-border)",
          borderRadius: "28px",
          padding: "32px",
          backdropFilter: "blur(18px)",
          boxShadow: "0 18px 50px rgba(61, 42, 21, 0.08)",
        }}
      >
        <p
          style={{
            margin: 0,
            color: "var(--accent-strong)",
            letterSpacing: "0.08em",
            textTransform: "uppercase",
            fontSize: "0.8rem",
          }}
        >
          Academic Meta Search
        </p>
        <h1
          style={{
            margin: "12px 0 16px",
            fontSize: "clamp(2.8rem, 7vw, 5.2rem)",
            lineHeight: 0.95,
          }}
        >
          Explore learning resources from one place.
        </h1>
        <p
          style={{
            maxWidth: "700px",
            fontSize: "1.05rem",
            lineHeight: 1.7,
            color: "var(--muted)",
          }}
        >
          DORA will combine educational APIs, curated crawling, and local
          indexing into one focused search experience for students and
          researchers.
        </p>

        <div
          style={{
            marginTop: "28px",
            padding: "12px",
            borderRadius: "20px",
            background: "rgba(255, 255, 255, 0.85)",
            border: "1px solid rgba(72, 52, 34, 0.08)",
            display: "grid",
            gridTemplateColumns: "1fr auto",
            gap: "12px",
          }}
        >
          <input
            aria-label="Search topic"
            placeholder="Search machine learning, data structures, operating systems..."
            style={{
              border: "none",
              outline: "none",
              background: "transparent",
              fontSize: "1rem",
              padding: "14px 16px",
              color: "var(--text)",
            }}
          />
          <button
            type="button"
            style={{
              border: "none",
              borderRadius: "16px",
              padding: "14px 22px",
              background: "var(--accent)",
              color: "#fffaf3",
              cursor: "pointer",
              fontWeight: 600,
            }}
          >
            Search
          </button>
        </div>

        <div
          style={{
            marginTop: "28px",
            display: "flex",
            flexWrap: "wrap",
            gap: "10px",
          }}
        >
          {sampleCategories.map((category) => (
            <span
              key={category}
              style={{
                padding: "10px 14px",
                borderRadius: "999px",
                background: "rgba(213, 155, 67, 0.12)",
                border: "1px solid rgba(213, 155, 67, 0.18)",
                color: "var(--accent-strong)",
                fontSize: "0.92rem",
              }}
            >
              {category}
            </span>
          ))}
        </div>
      </section>
    </main>
  );
}
