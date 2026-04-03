"use client";

import { useEffect, useRef, useState } from "react";
import gsap from "gsap";
import ResultCard, { ResultItem } from "./ResultCard";

interface ResultsListProps {
  activeCategory: string;
  searchQuery?: string;
}

export default function ResultsList({ activeCategory, searchQuery }: ResultsListProps) {
  const [isLoading, setIsLoading] = useState(true);
  const [results, setResults] = useState<ResultItem[]>([]);
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const controller = new AbortController();
    setIsLoading(true);
    setResults([]);
    
    let typeParam = "";
    if (activeCategory === "Papers") typeParam = "paper";
    else if (activeCategory === "Videos") typeParam = "video";
    else if (activeCategory === "Resources") typeParam = "article,blog,book,course,repository,reference,concept";
    
    const formattedQuery = searchQuery?.trim() || "machine learning";
    const fetchUrl = `/api/search?q=${encodeURIComponent(formattedQuery)}${typeParam ? `&type=${typeParam}` : ""}`;
    
    fetch(fetchUrl, { signal: controller.signal })
      .then(res => res.json())
      .then(data => {
        setResults(data);
        setIsLoading(false);
      })
      .catch(err => {
        if (err.name === 'AbortError') return;
        console.error("Failed to fetch results:", err);
        setIsLoading(false);
      });

    return () => controller.abort();
  }, [activeCategory, searchQuery]);

  useEffect(() => {
    if (!isLoading && results.length > 0 && listRef.current) {
      const cards = listRef.current.querySelectorAll('.result-card');
      gsap.fromTo(cards, 
        { opacity: 0, y: 30 },
        { opacity: 1, y: 0, duration: 0.5, stagger: 0.1, ease: "power2.out" }
      );
    }
  }, [isLoading, results]);

  if (isLoading) {
    return (
      <div className="w-full max-w-3xl space-y-4 pt-2">
        {[1, 2, 3].map((i) => (
          <div key={i} className="w-full p-6 rounded-[16px] bg-white/30 backdrop-blur-sm border border-white/20 animate-pulse">
            <div className="flex justify-between mb-4">
              <div className="h-6 bg-theme-muted/10 rounded w-2/3"></div>
              <div className="h-6 bg-theme-muted/10 rounded w-16"></div>
            </div>
            <div className="h-4 bg-theme-muted/10 rounded w-full mb-2"></div>
            <div className="h-4 bg-theme-muted/10 rounded w-4/5 mb-6"></div>
            <div className="h-4 bg-theme-muted/10 rounded w-1/3"></div>
          </div>
        ))}
      </div>
    );
  }

  if (results.length === 0) {
    return (
      <div className="w-full max-w-3xl pt-12 flex flex-col items-center justify-center text-center animate-in fade-in duration-500">
        <div className="w-16 h-16 mb-4 rounded-full bg-theme-muted/5 flex items-center justify-center">
          <svg className="w-8 h-8 text-theme-muted/50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M12 20a8 8 0 100-16 8 8 0 000 16z" />
          </svg>
        </div>
        <h3 className="text-xl font-medium text-theme-text mb-2">No results found</h3>
        <p className="text-theme-muted">We couldn't find anything matching your filters or query. Try broader terms or "All" categories.</p>
      </div>
    );
  }

  return (
    <div ref={listRef} className="w-full max-w-3xl space-y-4 pt-2 pb-20">
      {results.map((item) => (
        <ResultCard key={item.id} item={item} />
      ))}
    </div>
  );
}
