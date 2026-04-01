"use client";

import { useState, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import SearchBar from "@/components/SearchBar";
import Tabs from "@/components/Tabs";
import Filters from "@/components/Filters";
import ResultsList from "@/components/ResultsList";

function SearchContent() {
  const searchParams = useSearchParams();
  const query = searchParams.get("q") || "";
  const [activeTab, setActiveTab] = useState("All");

  return (
    <div className="min-h-screen bg-transparent relative selection:bg-theme-accent/20">
      {/* Sticky Top Header */}
      <header className="sticky top-0 z-40 w-full pt-4 md:pt-6 pb-0 px-4 md:px-8 bg-theme-bg/85 backdrop-blur-xl border-b border-white/20 shadow-[0_4px_30px_-10px_rgba(0,0,0,0.05)]">
        <div className="max-w-[1400px] mx-auto flex flex-col items-center md:items-start">
          <div className="w-full flex items-center justify-between mb-2">
            {/* Minimal Logo representing DORA to go back home */}
            <a href="/" className="hidden md:flex items-center text-3xl font-light tracking-widest text-theme-text mr-8 select-none opacity-90 hover:opacity-100 transition-opacity">
              <span>D</span>
              <span className="mx-0.5 text-[0.85em]">
                <svg width="1em" height="1em" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round" className="text-theme-accent">
                  <circle cx="10" cy="10" r="7" />
                  <line x1="21" y1="21" x2="15" y2="15" />
                </svg>
              </span>
              <span>RA</span>
            </a>
            
            <div className="flex-1 w-full md:max-w-xl">
              <SearchBar compact defaultValue={query} key={query} />
            </div>
            
            {/* Empty spacer for balancing flex on desktop */}
            <div className="hidden md:block w-32 ml-8"></div>
          </div>
          
          {/* Tabs positioned carefully under search or aligned */}
          <div className="md:ml-[160px] w-full md:w-auto mt-2">
            <Tabs activeTab={activeTab} onTabChange={setActiveTab} />
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-[1400px] mx-auto px-4 md:px-8 py-8 flex items-start">
        {/* Left Sidebar */}
        <Filters />
        
        {/* Results Container */}
        <div className="flex-1 flex justify-center w-full relative z-10 min-w-0">
          <ResultsList activeCategory={activeTab} searchQuery={query} />
        </div>
      </main>
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-transparent w-full" />}>
      <SearchContent />
    </Suspense>
  );
}
