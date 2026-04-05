"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

interface PaginationProps {
  currentPage: number;
  totalResults: number;
  pageSize: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  totalResults,
  pageSize,
  onPageChange,
}: PaginationProps) {
  const totalPages = Math.ceil(totalResults / pageSize);

  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const pages = [];
    const maxVisiblePages = 5;
    
    if (totalPages <= maxVisiblePages) {
      for (let i = 0; i < totalPages; i++) pages.push(i);
    } else {
      let start = Math.max(0, currentPage - 2);
      let end = Math.min(totalPages - 1, start + maxVisiblePages - 1);
      
      if (end === totalPages - 1) {
        start = Math.max(0, end - maxVisiblePages + 1);
      }
      
      for (let i = start; i <= end; i++) pages.push(i);
    }
    return pages;
  };

  return (
    <nav className="flex items-center justify-center space-x-2 py-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage <= 0}
        className={cn(
          "p-2 rounded-full transition-all duration-300 border border-theme-accent/20 bg-white/40 backdrop-blur-md shadow-sm hover:shadow-md active:scale-95",
          "disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:shadow-sm disabled:active:scale-100",
          "text-theme-text hover:bg-theme-accent/10"
        )}
        aria-label="Previous page"
      >
        <ChevronLeft size={20} className="stroke-[1.5px]" />
      </button>

      <div className="flex items-center space-x-1 px-4 py-1.5 rounded-full bg-white/30 backdrop-blur-lg border border-white/40 shadow-[0_4px_24px_-10px_rgba(202,154,91,0.1)]">
        {getPageNumbers().map((page) => (
          <button
            key={page}
            onClick={() => onPageChange(page)}
            className={cn(
              "w-10 h-10 rounded-full flex items-center justify-center text-sm font-medium transition-all duration-500",
              currentPage === page
                ? "bg-theme-accent text-white shadow-lg shadow-theme-accent/20 scale-110"
                : "text-theme-muted hover:text-theme-text hover:bg-theme-accent/10"
            )}
          >
            {page + 1}
          </button>
        ))}
      </div>

      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage >= totalPages - 1}
        className={cn(
          "p-2 rounded-full transition-all duration-300 border border-theme-accent/20 bg-white/40 backdrop-blur-md shadow-sm hover:shadow-md active:scale-95",
          "disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:shadow-sm disabled:active:scale-100",
          "text-theme-text hover:bg-theme-accent/10"
        )}
        aria-label="Next page"
      >
        <ChevronRight size={20} className="stroke-[1.5px]" />
      </button>
    </nav>
  );
}
