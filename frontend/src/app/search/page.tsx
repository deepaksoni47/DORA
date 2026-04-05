import { Metadata } from 'next';
import SearchPageClient from './page-client';

export async function generateMetadata(props: {
  searchParams: Promise<{ q?: string }>;
}): Promise<Metadata> {
  const searchParams = await props.searchParams;
  const query = searchParams.q;
  return {
    title: query ? `${query} - DORA Search` : 'Search - DORA',
    description: query 
      ? `Search results for ${query} on DORA - Discovery Oriented Resource Aggregator.`
      : 'Search research papers, videos, and code on DORA.',
  };
}

export default function SearchPage(props: {
  searchParams: Promise<{ q?: string }>;
}) {
  return <SearchPageClient />;
}
