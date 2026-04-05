"use client";

export interface ResultItem {
  id: string;
  title: string;
  description: string;
  source: string;
  type: string;
  year: number;
  url: string;
}

export default function ResultCard({ item }: { item: ResultItem }) {
  const typeConfig: Record<string, { color: string; icon: string }> = {
    paper: { color: "text-blue-600 bg-blue-50/50", icon: "M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" },
    video: { color: "text-red-600 bg-red-50/50", icon: "M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z M21 12a9 9 0 11-18 0 9 9 0 0118 0z" },
    repository: { color: "text-green-600 bg-green-50/50", icon: "M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" },
    article: { color: "text-orange-600 bg-orange-50/50", icon: "M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" },
    course: { color: "text-purple-600 bg-purple-50/50", icon: "M12 14l9-5-9-5-9 5 9 5z" },
    book: { color: "text-amber-700 bg-amber-50/50", icon: "M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" },
    blog: { color: "text-emerald-600 bg-emerald-50/50", icon: "M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" },
    reference: { color: "text-indigo-600 bg-indigo-50/50", icon: "M8 14v3m4-3v3m4-3v3M3 21h18M3 10h18M3 7l9-4 9 4M4 10h16v11H4V10z" },
    concept: { color: "text-indigo-600 bg-indigo-50/50", icon: "M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" }
  };

  const dt = item.type ? item.type.toLowerCase() : "article";
  const config = typeConfig[dt] || typeConfig["article"];
  
  const displayType = item.type ? item.type.charAt(0).toUpperCase() + item.type.slice(1).toLowerCase() : "Article";

  return (
    <a 
      href={item.url}
      target="_blank"
      rel="noopener noreferrer"
      className="result-card block relative z-0 opacity-0 w-full p-6 rounded-[16px] bg-white/60 hover:bg-white/80 backdrop-blur-sm border border-white/40 shadow-[0_4px_15px_-10px_rgba(0,0,0,0.03)] transition-all duration-300 active:scale-[0.98] will-change-transform transform-gpu"
    >
      <div className="flex items-start justify-between mb-3">
        <h2 className="text-xl font-medium text-theme-text leading-tight pr-4">{item.title}</h2>
        <span className={`shrink-0 flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${config.color}`}>
          <svg className="w-3.5 h-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={config.icon} />
          </svg>
          {displayType}
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
    </a>
  );
}
