"use client";

export default function LogoDora() {
  return (
    <div className="flex items-center text-7xl sm:text-8xl md:text-9xl font-serif tracking-[0.12em] text-theme-text select-none group">
      <span className="drop-shadow-[0_8px_16px_rgba(42,42,42,0.12)]">D</span>
      <span className="relative flex items-center justify-center ml-[-0.08em] mr-[0.04em] text-[0.82em]">
        {/* Layered Glass-Effect Magnifying Glass */}
        <svg
          width="1.1em"
          height="1.1em"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.1"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="text-theme-accent will-change-transform transform-gpu drop-shadow-[0_12px_24px_rgba(202,154,91,0.22)]"
        >
          {/* Subtle Lens Gradient Overlay (Internal) */}
          <defs>
            <radialGradient id="glassGradient" cx="50%" cy="50%" r="50%" fx="35%" fy="35%">
              <stop offset="0%" stopColor="white" stopOpacity="0.45" />
              <stop offset="60%" stopColor="white" stopOpacity="0.05" />
              <stop offset="100%" stopColor="white" stopOpacity="0" />
            </radialGradient>
          </defs>
          
          {/* Magnifying Glass Lens & Rim */}
          <circle cx="11" cy="11" r="7.5" strokeWidth="1.4" className="opacity-90" />
          <circle cx="11" cy="11" r="6" fill="url(#glassGradient)" stroke="none" className="opacity-70" />
          
          {/* Inner details for depth */}
          <path d="M11 5.5 A5.5 5.5 0 0 1 16.5 11" className="opacity-40" strokeWidth="0.8" />
          
          {/* Sophisticated Handle */}
          <line x1="16.5" y1="16.5" x2="21" y2="21" strokeWidth="1.8" className="opacity-95" />
          <path d="M16 16 L17.5 17.5" stroke="#fff" strokeWidth="0.5" className="opacity-40" />
        </svg>
      </span>
      <span className="drop-shadow-[0_8px_16px_rgba(42,42,42,0.12)] ml-[-0.08em]">RA</span>
    </div>
  );
}


