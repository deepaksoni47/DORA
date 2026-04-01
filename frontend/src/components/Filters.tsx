"use client";

import { useEffect, useRef, useState } from "react";
import gsap from "gsap";

export default function Filters() {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    gsap.fromTo(containerRef.current, 
      { x: -20, opacity: 0 },
      { x: 0, opacity: 1, duration: 0.6, ease: "power3.out", delay: 0.2 }
    );
  }, []);

  return (
    <aside 
      ref={containerRef}
      className="hidden md:block w-64 shrink-0 sticky top-32 self-start bg-white/40 backdrop-blur-md border border-white/30 rounded-[20px] p-5 mr-8 shadow-[0_4px_20px_-10px_rgba(0,0,0,0.03)]"
    >
      <h3 className="text-sm font-semibold tracking-wider text-theme-muted uppercase mb-6">Filters</h3>
      
      <FilterSection title="Type" defaultOpen>
        <FilterCheckbox label="Research Papers" count={124} />
        <FilterCheckbox label="Video Lectures" count={42} />
        <FilterCheckbox label="Code Repositories" count={18} />
        <FilterCheckbox label="Articles" count={89} />
      </FilterSection>

      <div className="h-px bg-black/5 my-4" />

      <FilterSection title="Source" defaultOpen={false}>
        <FilterCheckbox label="ArXiv" />
        <FilterCheckbox label="GitHub" />
        <FilterCheckbox label="YouTube" />
        <FilterCheckbox label="Medium" />
      </FilterSection>

      <div className="h-px bg-black/5 my-4" />

      <FilterSection title="Year" defaultOpen={false}>
        <FilterCheckbox label="2024" />
        <FilterCheckbox label="2023" />
        <FilterCheckbox label="2022 & Older" />
      </FilterSection>
    </aside>
  );
}

function FilterSection({ title, children, defaultOpen = true }: { title: string, children: React.ReactNode, defaultOpen?: boolean }) {
  const [isOpen, setIsOpen] = useState(defaultOpen);
  const contentRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!contentRef.current) return;
    if (isOpen) {
      gsap.to(contentRef.current, { height: "auto", opacity: 1, duration: 0.3, ease: "power2.out" });
    } else {
      gsap.to(contentRef.current, { height: 0, opacity: 0, duration: 0.3, ease: "power2.out" });
    }
  }, [isOpen]);

  return (
    <div className="mb-2">
      <button 
        onClick={() => setIsOpen(!isOpen)}
        className="w-full flex items-center justify-between text-theme-text font-medium py-2 outline-none"
      >
        <span>{title}</span>
        <svg 
          className={`w-4 h-4 text-theme-muted transition-transform duration-300 ${isOpen ? "rotate-180" : ""}`}
          fill="none" viewBox="0 0 24 24" stroke="currentColor"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      <div ref={contentRef} className="overflow-hidden" style={{ height: defaultOpen ? "auto" : 0, opacity: defaultOpen ? 1 : 0 }}>
        <div className="pt-2 pb-1 space-y-2">
          {children}
        </div>
      </div>
    </div>
  );
}

function FilterCheckbox({ label, count }: { label: string, count?: number }) {
  return (
    <label className="flex items-center justify-between group cursor-pointer">
      <div className="flex items-center">
        <div className="w-4 h-4 rounded border border-theme-muted/40 flex items-center justify-center mr-3 transition-colors group-hover:border-theme-accent">
          {/* subtle checkmark placeholder */}
        </div>
        <span className="text-sm text-theme-muted group-hover:text-theme-text transition-colors">{label}</span>
      </div>
      {count !== undefined && (
        <span className="text-xs text-theme-muted/60">{count}</span>
      )}
    </label>
  );
}
