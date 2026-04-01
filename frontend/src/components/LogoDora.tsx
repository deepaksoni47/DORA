"use client";

import { useEffect, useRef } from "react";
import gsap from "gsap";

export default function LogoDora({ isSearchFocused }: { isSearchFocused: boolean }) {
  const oRef = useRef<SVGSVGElement>(null);
  
  useEffect(() => {
    if (!oRef.current) return;
    
    // Idle pulse animation
    const idleAnim = gsap.to(oRef.current, {
      scale: 1.05,
      duration: 3,
      repeat: -1,
      yoyo: true,
      ease: "sine.inOut",
      paused: isSearchFocused
    });
    
    if (isSearchFocused) {
      idleAnim.pause();
      gsap.to(oRef.current, {
        scale: 1.25,
        rotation: 10,
        duration: 0.4,
        ease: "power2.out",
        color: "#ca9a5b", // subtle highlight
      });
    } else {
      gsap.to(oRef.current, {
        scale: 1,
        rotation: 0,
        duration: 0.5,
        ease: "power2.out",
        color: "currentColor",
        onComplete: () => { idleAnim.play(); }
      });
    }

    return () => {
      idleAnim.kill();
    };
  }, [isSearchFocused]);

  return (
    <div className="flex items-center text-6xl sm:text-7xl md:text-8xl lg:text-9xl font-light tracking-[0.16em] text-theme-text opacity-95 select-none">
      <span className="drop-shadow-[0_10px_20px_rgba(255,255,255,0.18)]">D</span>
      <span className="flex items-center justify-center mx-[0.02em] md:mx-[0.04em] text-[0.9em] text-theme-accent drop-shadow-[0_8px_20px_rgba(202,154,91,0.18)]">
        <svg
          ref={oRef}
          width="1em"
          height="1em"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="will-change-transform transform-gpu"
        >
          <circle cx="14" cy="10" r="7" />
          <line x1="3" y1="21" x2="9" y2="15" />
        </svg>
      </span>
      <span className="drop-shadow-[0_10px_20px_rgba(255,255,255,0.18)]">RA</span>
    </div>
  );
}


