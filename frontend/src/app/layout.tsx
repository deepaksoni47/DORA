import type { Metadata } from "next";
import { Inter } from "next/font/google";
import SmoothScroll from "@/components/SmoothScroll";
import SplashCursor from "@/components/SplashCursor";
import "./globals.css";

const inter = Inter({ subsets: ["latin"], variable: "--font-inter" });

export const metadata: Metadata = {
  title: "DORA",
  description: "Discovery Oriented Resource Aggregator",
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
