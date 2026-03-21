import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "DORA",
  description: "Educational meta search engine for academic discovery",
};

type RootLayoutProps = Readonly<{
  children: React.ReactNode;
}>;

export default function RootLayout({ children }: RootLayoutProps) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
