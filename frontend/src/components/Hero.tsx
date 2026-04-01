"use client";

import { useEffect, useRef, useState } from "react";
import gsap from "gsap";
import LogoDora from "./LogoDora";
import SearchBar from "./SearchBar";
import DotGrid from "./DotGrid";

export default function Hero() {
  const containerRef = useRef<HTMLDivElement>(null);
  const headingRef = useRef<HTMLHeadingElement>(null);
  const searchWrapperRef = useRef<HTMLDivElement>(null);
  const logoWrapperRef = useRef<HTMLDivElement>(null);

  const [isSearchFocused, setIsSearchFocused] = useState(false);

  useEffect(() => {
    // Initial load animation timeline
    const tl = gsap.timeline({ defaults: { ease: "power3.out" } });

    // Ensure elements are invisible initially
    gsap.set(
      [logoWrapperRef.current, headingRef.current, searchWrapperRef.current],
      {
        opacity: 0,
        y: 20,
      },
    );

    tl.to(logoWrapperRef.current, {
      opacity: 1,
      y: 0,
      duration: 1.2,
      delay: 0.2,
    })
      .to(
        headingRef.current,
        {
          opacity: 1,
          y: 0,
          duration: 1,
        },
        "-=0.6",
      )
      .to(
        searchWrapperRef.current,
        {
          opacity: 1,
          y: 0,
          duration: 1,
        },
        "-=0.7",
      );

    return () => {
      tl.kill();
    };
  }, []);

  return (
    <section
      ref={containerRef}
      className="relative min-h-screen w-full flex flex-col items-center justify-center overflow-hidden px-4 sm:px-6"
    >
      {/* Dot Grid Background Layer */}
      <div className="absolute inset-0 z-0">
        <DotGrid
          dotSize={4}
          gap={22}
          baseColor="#d8c1a0"
          activeColor="#ca9a5b"
          proximity={140}
          shockRadius={260}
          shockStrength={5}
          resistance={700}
          returnDuration={1.4}
        />
      </div>

      {/* Main Content Layer */}
      <div className="relative z-10 flex flex-col items-center w-full max-w-4xl">
        {/* Logo Element */}
        <div ref={logoWrapperRef} className="mb-6 md:mb-8">
          <LogoDora isSearchFocused={isSearchFocused} />
        </div>

        {/* Heading Element */}
        <h1
          ref={headingRef}
          className="max-w-2xl text-base sm:text-lg md:text-[1.45rem] text-theme-text/75 font-serif italic mb-10 md:mb-14 tracking-[0.08em] text-center drop-shadow-[0_4px_14px_rgba(255,248,235,0.45)]"
        >
          Explore Knowledge Differently
        </h1>

        {/* Interactive Search Bar */}
        <div ref={searchWrapperRef} className="w-full">
          <SearchBar onFocusChange={setIsSearchFocused} />
        </div>
      </div>

      {/* Scroll Hint */}
      <div className="absolute bottom-10 left-1/2 -translate-x-1/2 flex flex-col items-center opacity-50 animate-bounce cursor-default select-none hidden sm:flex">
        <span className="text-[10px] uppercase tracking-widest text-theme-muted mb-2 font-medium">
          Scroll to discover
        </span>
        <svg
          fill="none"
          viewBox="0 0 24 24"
          strokeWidth="1.5"
          stroke="currentColor"
          className="w-4 h-4 text-theme-muted"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M19.5 8.25l-7.5 7.5-7.5-7.5"
          />
        </svg>
      </div>
    </section>
  );
}
