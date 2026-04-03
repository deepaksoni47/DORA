"use client";

import { useEffect, useRef } from "react";
import gsap from "gsap";
import ScrollTrigger from "gsap/ScrollTrigger";
import ResultCard, { ResultItem } from "./ResultCard";

const PREVIEW_RESULTS: ResultItem[] = [
  { id: "p1", title: "Emergent Abilities of Large Language Models", description: "Analyzing how scaling up language models leads to sudden improvements on certain tasks...", source: "TMLR 2022", type: "paper", year: 2022, url: "https://arxiv.org/abs/2206.07682" },
  { id: "p2", title: "Neural Networks: Zero to Hero", description: "A course by Andrej Karpathy on building neural networks, from backpropagation to GPT.", source: "YouTube", type: "video", year: 2023, url: "https://www.youtube.com/playlist?list=PLAqhIrjkxbuWI23v9cThsA9GvCAUbznKZ" },
  { id: "p3", title: "karpathy/micrograd", description: "A tiny scalar-valued autograd engine with a small PyTorch-like neural network library on top.", source: "GitHub", type: "repository", year: 2020, url: "https://github.com/karpathy/micrograd" }
];

function LeafIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="11" height="11" fill="none" viewBox="0 0 11 11">
      <path
        fill="currentColor"
        d="M5 2c0 1.105-1.895 2-3 2a2 2 0 1 1 0-4c1.105 0 3 .895 3 2ZM11 3.5c0 1.105-.895 3-2 3s-2-1.895-2-3a2 2 0 1 1 4 0ZM6 9a2 2 0 1 1-4 0c0-1.105.895-3 2-3s2 1.895 2 3Z"
      />
    </svg>
  );
}

export default function PreviewSection() {
  const sectionRef = useRef<HTMLElement>(null);
  const listRef = useRef<HTMLDivElement>(null);
  const headerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!sectionRef.current || !listRef.current || !headerRef.current) return;
    
    // Background transition
    gsap.fromTo(sectionRef.current,
      { backgroundColor: "transparent" },
      {
        backgroundColor: "#fdfbf7",
        ease: "none",
        scrollTrigger: {
          trigger: sectionRef.current,
          start: "top 90%",
          end: "top 40%",
          scrub: true,
        }
      }
    );

    // Staggered reveal for header elements
    const headerChildren = headerRef.current.children;
    gsap.fromTo(headerChildren,
      { opacity: 0, y: 30 },
      {
        opacity: 1,
        y: 0,
        stagger: 0.1,
        duration: 1,
        ease: "power3.out",
        scrollTrigger: {
          trigger: headerRef.current,
          start: "top 85%",
        }
      }
    );

    // Staggered reveal for cards
    const cards = listRef.current.querySelectorAll('.result-card');
    gsap.fromTo(cards,
      { opacity: 0, y: 50 },
      {
        opacity: 1,
        y: 0,
        stagger: 0.15,
        duration: 1,
        ease: "power2.out",
        scrollTrigger: {
          trigger: listRef.current,
          start: "top 75%",
        }
      }
    );
  }, []);

  return (
    <section ref={sectionRef} className="relative w-full pt-48 pb-40 flex flex-col items-center transition-colors">
      <div className="w-full max-w-4xl px-6">
        <div ref={headerRef} className="text-center mb-20 space-y-4">
          <p className="text-xs uppercase tracking-[0.4em] text-theme-muted font-medium ml-1">
            DORA Intelligence Preview
          </p>
          <h2 className="text-4xl md:text-5xl lg:text-5xl font-serif italic text-theme-text/90 leading-[1.1] tracking-tight">
            Grounded by reality. <br className="hidden md:block" /> Ready to explore.
          </h2>
          <div className="w-12 h-[1px] bg-theme-accent/30 mx-auto mt-8 mb-4" />
        </div>
        
        <div ref={listRef} className="max-w-2xl mx-auto space-y-6 pb-4">
          {PREVIEW_RESULTS.map((item) => (
            <ResultCard key={item.id} item={item} />
          ))}
        </div>
        
        <div className="flex justify-center mt-16 scale-reveal">
          <a
            href="/search"
            className="group relative flex items-center gap-3 px-10 py-4 rounded-full bg-theme-text text-theme-bg font-semibold text-sm tracking-widest transition-all duration-500 hover:scale-105 hover:shadow-xl hover:shadow-theme-accent/10"
          >
            <LeafIcon />
            <span>START SEARCHING</span>
            <svg 
              className="w-4 h-4 transition-transform duration-300 group-hover:translate-x-1" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M17 8l4 4m0 0l-4 4m4-4H3" />
            </svg>
          </a>
        </div>
      </div>
    </section>
  );
}

