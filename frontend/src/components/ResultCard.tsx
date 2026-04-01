"use client";

import { useRef } from "react";
import gsap from "gsap";

export interface ResultItem {
  id: string;
  title: string;
  description: string;
  source: string;
  type: "Paper" | "Video" | "Code" | "Article";
  year: number;
}

export default function ResultCard({ item }: { item: ResultItem }) {
  const cardRef = useRef<HTMLDivElement>(null);

  const handleMouseEnter = () => {
    gsap.to(cardRef.current, {
      y: -4,
      scale: 1.01,
      boxShadow: "0 12px 30px -10px rgba(0,0,0,0.08)",
      backgroundColor: "rgba(255, 255, 255, 0.9)",
      duration: 0.3,
      ease: "power2.out"
    });
  };

  const handleMouseLeave = () => {
    gsap.to(cardRef.current, {
      y: 0,
      scale: 1,
      boxShadow: "0 4px 15px -10px rgba(0,0,0,0.03)",
      backgroundColor: "rgba(255, 255, 255, 0.6)",
      duration: 0.3,
      ease: "power2.out"
    });
  };

  const typeConfig = {
    Paper: { color: "text-blue-600 bg-blue-50/50", icon: "M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" },
    Video: { color: "text-red-600 bg-red-50/50", icon: "M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z M21 12a9 9 0 11-18 0 9 9 0 0118 0z" },
    Code: { color: "text-green-600 bg-green-50/50", icon: "M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" },
    Article: { color: "text-orange-600 bg-orange-50/50", icon: "M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" }
  };

  const config = typeConfig[item.type];

  return (
    <div 
      ref={cardRef}
      className="result-card relative z-0 opacity-0 w-full p-6 rounded-[16px] bg-white/60 backdrop-blur-sm border border-white/40 cursor-pointer transition-colors"
      style={{ boxShadow: "0 4px 15px -10px rgba(0,0,0,0.03)" }}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <div className="flex items-start justify-between mb-3">
        <h2 className="text-xl font-medium text-theme-text leading-tight pr-4">{item.title}</h2>
        <span className={`shrink-0 flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${config.color}`}>
          <svg className="w-3.5 h-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={config.icon} />
          </svg>
          {item.type}
        </span>
      </div>
      
      <p className="text-theme-muted text-sm leading-relaxed mb-6 line-clamp-2">
        {item.description}
      </p>
      
      <div className="flex items-center space-x-4 text-xs font-medium text-theme-muted/70">
        <span className="flex items-center">
          <svg className="w-4 h-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
          </svg>
          {item.source}
        </span>
        <span>•</span>
        <span>{item.year}</span>
      </div>
    </div>
  );
}

