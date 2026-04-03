"use client";

import { useEffect, useRef, useState } from "react";
import gsap from "gsap";

const TABS = ["All", "Papers", "Videos", "Resources"];

interface TabsProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
}

export default function Tabs({ activeTab, onTabChange }: TabsProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const activeBgRef = useRef<HTMLDivElement>(null);
  const [activeIndex, setActiveIndex] = useState(0);

  useEffect(() => {
    setActiveIndex(TABS.indexOf(activeTab) !== -1 ? TABS.indexOf(activeTab) : 0);
  }, [activeTab]);

  useEffect(() => {
    if (!containerRef.current || !activeBgRef.current) return;
    
    const tabs = Array.from(containerRef.current.querySelectorAll<HTMLButtonElement>('.tab-item'));
    const currentTab = tabs[activeIndex];
    
    if (currentTab) {
      const { offsetLeft, offsetWidth } = currentTab;
      
      gsap.to(activeBgRef.current, {
        x: offsetLeft,
        width: offsetWidth,
        duration: 0.4,
        ease: "power3.out"
      });
    }
  }, [activeIndex]);

  return (
    <div className="relative border-b border-black/5 pb-0" ref={containerRef}>
      <div className="flex items-center space-x-2 md:space-x-8 overflow-x-auto no-scrollbar">
        {TABS.map((tab, idx) => {
          const isActive = idx === activeIndex;
          return (
            <button
              key={tab}
              onClick={() => onTabChange(tab)}
              className={`tab-item whitespace-nowrap relative z-10 px-2 py-3 text-sm font-medium transition-colors duration-300 outline-none ${
                isActive ? "text-theme-text" : "text-theme-muted hover:text-theme-text/80"
              }`}
            >
              {tab}
            </button>
          );
        })}
      </div>
      
      {/* Animated active indicator */}
      <div 
        ref={activeBgRef}
        className="absolute bottom-0 left-0 h-[2px] bg-theme-accent z-20 rounded-t-full"
        style={{ width: 0, transform: "translateX(0)" }}
      />
    </div>
  );
}
