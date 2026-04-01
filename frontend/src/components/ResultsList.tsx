"use client";

import { useEffect, useRef, useState } from "react";
import gsap from "gsap";
import ResultCard, { ResultItem } from "./ResultCard";

const MOCK_RESULTS: ResultItem[] = [
  {
    id: "1",
    title: "Attention Is All You Need",
    description: "The dominant sequence transduction models are based on complex recurrent or convolutional neural networks that include an encoder and a decoder. We propose a new simple network architecture, the Transformer...",
    source: "arXiv:1706.03762",
    type: "Paper",
    year: 2017
  },
  {
    id: "2",
    title: "Graph Neural Networks for knowledge representation",
    description: "A comprehensive overview of graph neural networks (GNNs) in natural language processing and knowledge graph representation, demonstrating state-of-the-art results...",
    source: "Medium Towards Data Science",
    type: "Article",
    year: 2023
  },
  {
    id: "3",
    title: "Building a RAG System from Scratch",
    description: "Step-by-step video lecture on how to implement Retrieval-Augmented Generation using LangChain and Pinecone. Includes source code review and architectural deep dive.",
    source: "YouTube / AI Engineer",
    type: "Video",
    year: 2024
  },
  {
    id: "4",
    title: "langchain-ai / langchain",
    description: "⚡ Building applications with LLMs through composability ⚡",
    source: "GitHub",
    type: "Code",
    year: 2024
  }
];

interface ResultsListProps {
  activeCategory: string;
  searchQuery?: string;
}

export default function ResultsList({ activeCategory, searchQuery }: ResultsListProps) {
  const [isLoading, setIsLoading] = useState(true);
  const [results, setResults] = useState<ResultItem[]>([]);
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Simulate fetch based on filters
    setIsLoading(true);
    setResults([]);
    
    const timer = setTimeout(() => {
      let filtered = MOCK_RESULTS;
      if (activeCategory !== "All") {
         filtered = MOCK_RESULTS.filter(r => (activeCategory.includes(r.type) || r.type.includes(activeCategory.replace(/s$/, ''))));
      }
      
      setResults(filtered);
      setIsLoading(false);
    }, 600); // 600ms artificial delay for skeleton

    return () => clearTimeout(timer);
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
      {results.map((item, i) => (
        <ResultCard key={item.id} item={item} />
      ))}
    </div>
  );
}
