"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import gsap from "gsap";

const PLACEHOLDERS = [
  "machine learning",
  "graph algorithms",
  "system design",
  "react hooks",
  "quantum computing"
];

interface SearchBarProps {
  onFocusChange?: (focused: boolean) => void;
  compact?: boolean;
  className?: string;
  defaultValue?: string;
}

export default function SearchBar({
  onFocusChange,
  compact = false,
  className = "",
  defaultValue = ""
}: SearchBarProps) {
  const router = useRouter();
  const containerRef = useRef<HTMLDivElement>(null);
  const placeholderRef = useRef<HTMLDivElement>(null);
  const [index, setIndex] = useState(0);
  const [hasText, setHasText] = useState(defaultValue.length > 0);

  useEffect(() => {
    if (hasText) return;
    const interval = setInterval(() => {
      if (!placeholderRef.current) return;

      gsap.to(placeholderRef.current, {
        opacity: 0,
        y: -10,
        duration: 0.4,
        ease: "power2.in",
        onComplete: () => {
          setIndex((prev) => (prev + 1) % PLACEHOLDERS.length);
          if (placeholderRef.current) {
            gsap.fromTo(
              placeholderRef.current,
              { opacity: 0, y: 10 },
              { opacity: 1, y: 0, duration: 0.4, ease: "power2.out", delay: 0.1 }
            );
          }
        }
      });
    }, 3000);
    return () => clearInterval(interval);
  }, [hasText]);

  const handleFocus = () => {
    if (onFocusChange) onFocusChange(true);
    gsap.to(containerRef.current, {
      scale: compact ? 1.015 : 1.025,
      y: -2,
      boxShadow:
        "0 24px 55px -24px rgba(88, 57, 22, 0.45), inset 0 1px 0 rgba(255,255,255,0.82), inset 0 -10px 18px rgba(183, 131, 66, 0.12)",
      borderColor: "rgba(202, 154, 91, 0.95)",
      backgroundColor: "rgba(251, 243, 229, 0.97)",
      duration: 0.3,
      ease: "power2.out"
    });
  };

  const handleBlur = () => {
    if (onFocusChange) onFocusChange(false);
    gsap.to(containerRef.current, {
      scale: 1,
      y: 0,
      boxShadow:
        "0 18px 42px -24px rgba(88, 57, 22, 0.32), inset 0 1px 0 rgba(255,255,255,0.8), inset 0 -12px 22px rgba(191, 145, 82, 0.12)",
      borderColor: "rgba(202, 154, 91, 0.68)",
      backgroundColor: "rgba(248, 239, 224, 0.94)",
      duration: 0.3,
      ease: "power2.out"
    });
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const input = e.currentTarget.elements.namedItem("q") as HTMLInputElement;
    const query = input?.value.trim() || "";
    if (query) {
      router.push(`/search?q=${encodeURIComponent(query)}`);
    } else {
      router.push(`/search`);
    }
  };

  return (
    <div
      ref={containerRef}
      className={`relative z-20 w-full overflow-hidden rounded-[24px] border ${compact ? "max-w-xl" : "max-w-2xl mx-auto"} ${className}`}
      style={{
        borderColor: "rgba(202, 154, 91, 0.68)",
        background:
          "linear-gradient(180deg, rgba(252, 245, 233, 0.98) 0%, rgba(246, 233, 211, 0.96) 100%)",
        boxShadow:
          "0 18px 42px -24px rgba(88, 57, 22, 0.32), inset 0 1px 0 rgba(255,255,255,0.8), inset 0 -12px 22px rgba(191, 145, 82, 0.12)",
      }}
    >
      <div className="pointer-events-none absolute inset-x-5 top-0 h-px bg-white/80" />
      <div className="pointer-events-none absolute inset-x-8 bottom-0 h-[2px] bg-[#b9894f]/30 blur-[1px]" />
      <div className={`relative flex items-center ${compact ? "px-4 py-2.5" : "px-5 md:px-7 py-4 md:py-5"}`}>
        <svg
          className={`${compact ? "w-4 h-4 mr-3" : "w-5 h-5 md:w-6 md:h-6 mr-3 md:mr-4"} text-[#9f7440] transition-colors shrink-0`}
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <form onSubmit={handleSubmit} className={`relative flex-1 flex items-center ${compact ? "h-5 md:h-6" : "h-7 md:h-8"}`}>
          <input
            type="text"
            name="q"
            defaultValue={defaultValue}
            className={`absolute inset-0 w-full h-full bg-transparent outline-none text-theme-text ${compact ? "text-sm md:text-base" : "text-base md:text-lg"} font-medium placeholder-transparent`}
            onFocus={handleFocus}
            onBlur={handleBlur}
            onChange={(e) => setHasText(e.target.value.length > 0)}
            style={{ zIndex: 10 }}
          />
          {!hasText && (
            <div
              ref={placeholderRef}
              className={`absolute flex items-center w-full h-full text-[#6f5a40] pointer-events-none ${compact ? "text-sm md:text-base" : "text-base md:text-lg"} font-medium opacity-85`}
            >
              Search <span className="italic ml-1 opacity-75">"{PLACEHOLDERS[index]}"</span>
            </div>
          )}
        </form>
        <div className="hidden sm:flex items-center ml-3 space-x-1.5">
          <kbd className={`px-2 py-1 ${compact ? "text-[10px]" : "text-xs"} font-sans text-[#7d6445] bg-white/65 rounded-md border border-[#ca9a5b]/35 shadow-[inset_0_1px_0_rgba(255,255,255,0.7),0_4px_10px_-8px_rgba(0,0,0,0.35)]`}>
            Ctrl
          </kbd>
          <kbd className={`px-2 py-1 ${compact ? "text-[10px]" : "text-xs"} font-sans text-[#7d6445] bg-white/65 rounded-md border border-[#ca9a5b]/35 shadow-[inset_0_1px_0_rgba(255,255,255,0.7),0_4px_10px_-8px_rgba(0,0,0,0.35)]`}>
            K
          </kbd>
        </div>
      </div>
    </div>
  );
}
