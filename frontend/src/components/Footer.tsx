export default function Footer() {
  return (
    <footer className="w-full py-12 flex flex-col items-center justify-center bg-transparent relative z-30 border-t border-black/5">
      <div className="flex items-center text-2xl md:text-3xl font-light tracking-widest text-theme-text opacity-70 mb-4 select-none">
        <span>D</span>
        <span className="mx-0.5 text-[0.85em] flex items-center justify-center">
          <svg width="1em" height="1em" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="10" cy="10" r="7" />
            <line x1="21" y1="21" x2="15" y2="15" />
          </svg>
        </span>
        <span>RA</span>
      </div>
      <p className="text-xs md:text-sm text-theme-muted font-medium">
        Discovery Oriented Resource Aggregator &copy; {new Date().getFullYear()}
      </p>
    </footer>
  );
}
