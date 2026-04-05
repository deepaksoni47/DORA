import { MetadataRoute } from 'next'
 
export default function sitemap(): MetadataRoute.Sitemap {
  return [
    {
      url: 'https://dora-search.edu',
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 1,
    },
    {
      url: 'https://dora-search.edu/search',
      lastModified: new Date(),
      changeFrequency: 'always',
      priority: 0.8,
    },
  ]
}
