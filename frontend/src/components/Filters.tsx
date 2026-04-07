"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { useSearchParams, useRouter, usePathname } from "next/navigation";
import gsap from "gsap";
import { cn } from "@/lib/utils";

const FILTER_CONFIG = {
  type: [
    { label: "Research Papers", value: "paper" },
    { label: "Video Lectures", value: "video" },
    { label: "Code Repositories", value: "repository" },
    { label: "Articles & Blogs", value: "article,blog,book" },
  ],
  source: [
    { label: "ArXiv", value: "arxiv" },
    { label: "GitHub", value: "github" },
    { label: "YouTube", value: "youtube" },
    { label: "Wikipedia", value: "wikipedia" },
    { label: "Internal Crawler", value: "crawler" },
  ],
  years: [
    { label: "2026", value: "2026-2026" },
    { label: "2025", value: "2025-2025" },
    { label: "2024", value: "2024-2024" },
    { label: "2023 & Older", value: "0-2023" },
  ]
};

export default function Filters() {
  const containerRef = useRef<HTMLDivElement>(null);
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    gsap.fromTo(containerRef.current, 
      { x: -20, opacity: 0 },
      { x: 0, opacity: 1, duration: 0.6, ease: "power3.out", delay: 0.2 }
    );
  }, []);

  const updateFilters = useCallback((key: string, value: string) => {
    const params = new URLSearchParams(searchParams.toString());
    const currentValues = params.get(key)?.split(",") || [];
    
    // Support multi-value filters (e.g. "article,blog")
    const parts = value.split(",");
    const isAlreadyFullyChecked = parts.every(v => currentValues.includes(v));
    
    if (isAlreadyFullyChecked) {
      // Remove all parts from the current values
      const newValues = currentValues.filter((v) => !parts.includes(v));
      if (newValues.length > 0) {
        params.set(key, newValues.join(","));
      } else {
        params.delete(key);
      }
    } else {
      // Add all parts that aren't already there
      const resultValues = [...currentValues];
      parts.forEach(v => {
        if (!resultValues.includes(v)) {
          resultValues.push(v);
        }
      });
      params.set(key, resultValues.join(","));
    }
    
    // Always reset page when filters change
    params.delete("page");
    
    router.push(`${pathname}?${params.toString()}`, { scroll: false });
  }, [searchParams, pathname, router]);

  const isChecked = (key: string, value: string) => {
    const currentValues = searchParams.get(key)?.split(",") || [];
    if (!value) return false;
    
    const parts = value.split(",");
    // For multi-value filters, return true if ALL parts are included
    return parts.every(v => currentValues.includes(v));
  };

  return (
    <aside 
      ref={containerRef}
      className="hidden md:block w-64 shrink-0 sticky top-32 self-start bg-white/40 backdrop-blur-md border border-white/30 rounded-[20px] p-5 mr-8 shadow-[0_4px_20px_-10px_rgba(0,0,0,0.03)]"
    >
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-sm font-semibold tracking-wider text-theme-muted uppercase">Filters</h3>
        {searchParams.toString().includes("=") && (
          <button 
            onClick={() => router.push(pathname)}
            className="text-[10px] text-theme-accent hover:underline uppercase tracking-tighter"
          >
            Clear All
          </button>
        )}
      </div>
      
      <FilterSection title="Type" defaultOpen>
        {FILTER_CONFIG.type.map((f) => (
          <FilterCheckbox 
            key={f.value} 
            label={f.label} 
            checked={isChecked("type", f.value)}
            onChange={() => updateFilters("type", f.value)}
          />
        ))}
      </FilterSection>

      <div className="h-px bg-black/5 my-4" />

      <FilterSection title="Source" defaultOpen={false}>
        {FILTER_CONFIG.source.map((f) => (
          <FilterCheckbox 
            key={f.value} 
            label={f.label} 
            checked={isChecked("source", f.value)}
            onChange={() => updateFilters("source", f.value)}
          />
        ))}
      </FilterSection>

      <div className="h-px bg-black/5 my-4" />

      <FilterSection title="Year" defaultOpen={false}>
        {FILTER_CONFIG.years.map((f) => (
          <FilterCheckbox 
            key={f.value} 
            label={f.label} 
            checked={isChecked("years", f.value)}
            onChange={() => updateFilters("years", f.value)}
          />
        ))}
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
        className="w-full flex items-center justify-between text-theme-text font-medium py-2 outline-none group"
      >
        <span className="group-hover:text-theme-accent transition-colors">{title}</span>
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

function FilterCheckbox({ label, checked, onChange }: { label: string, checked: boolean, onChange: () => void }) {
  return (
    <label className="flex items-center justify-between group cursor-pointer select-none">
      <div className="flex items-center">
        <div className={cn(
          "w-4 h-4 rounded border flex items-center justify-center mr-3 transition-all duration-300",
          checked 
            ? "border-theme-accent bg-theme-accent text-white shadow-[0_0_10px_rgba(202,154,91,0.3)]" 
            : "border-theme-muted/40 group-hover:border-theme-accent"
        )}>
          {checked && (
            <svg className="w-2.5 h-2.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="4">
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          )}
          <input 
            type="checkbox" 
            className="hidden" 
            checked={checked} 
            onChange={onChange} 
          />
        </div>
        <span className={cn(
          "text-sm transition-colors duration-300",
          checked ? "text-theme-text font-medium" : "text-theme-muted group-hover:text-theme-text"
        )}>
          {label}
        </span>
      </div>
    </label>
  );
}
