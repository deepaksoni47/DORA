import type { Metadata } from "next";
import { Inter } from "next/font/google";
import SmoothScroll from "@/components/SmoothScroll";
import SplashCursor from "@/components/SplashCursor";
import "./globals.css";

const inter = Inter({ subsets: ["latin"], variable: "--font-inter" });

export const metadata: Metadata = {
  metadataBase: new URL("https://dora-search.edu"),
  title: "DORA | Discovery Oriented Resource Aggregator",
  description: "A federated academic search engine that aggregates research papers, educational videos, and open-source code from arXiv, YouTube, GitHub, and Wikipedia.",
  keywords: ["Academic Search", "Research Aggregator", "Federated Search", "arXiv", "GitHub Search", "Educational Videos", "DORA"],
  authors: [{ name: "DORA Team" }],
  creator: "DORA Team",
  publisher: "DORA",
  formatDetection: {
    email: false,
    address: false,
    telephone: false,
  },
  icons: {
    icon: "/logo.png",
    shortcut: "/logo.png",
    apple: "/logo.png",
  },
  openGraph: {
    title: "DORA | Discovery Oriented Resource Aggregator",
    description: "Multi-source academic discovery at your fingertips. From theory to implementation.",
    url: "https://dora-search.edu",
    siteName: "DORA",
    images: [
      {
        url: "/logo.png",
        width: 800,
        height: 600,
        alt: "DORA Logo",
      },
    ],
    locale: "en_US",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "DORA | Discovery Oriented Resource Aggregator",
    description: "The ultimate academic resource aggregator.",
    images: ["/logo.png"],
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={inter.variable}>
      <body className="font-sans antialiased bg-theme-bg text-theme-text min-h-screen">
        <SplashCursor 
          colorHex="#ca9a5b" 
          SIM_RESOLUTION={64} 
          DYE_RESOLUTION={512} 
          PRESSURE_ITERATIONS={10} 
        />
        <SmoothScroll>
          {children}
        </SmoothScroll>
      </body>
    </html>
  );
}
