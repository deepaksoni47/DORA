import Hero from "@/components/Hero";
import Exploration3DSection from "@/components/Exploration3DSection";
import DomainSection from "@/components/DomainSection";
import PreviewSection from "@/components/PreviewSection";
import Footer from "@/components/Footer";

export default function HomePage() {
  return (
    <main className="flex flex-col min-h-screen w-full relative bg-transparent overflow-x-clip">
      <Hero />
      <Exploration3DSection />
      <DomainSection />
      <PreviewSection />
      <Footer />
    </main>
  );
}
