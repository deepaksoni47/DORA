"use client";

import { useEffect, useRef, useCallback } from "react";
import gsap from "gsap";
import ScrollTrigger from "gsap/ScrollTrigger";

gsap.registerPlugin(ScrollTrigger);

/* ─── Domain data ─── */
const DOMAINS = [
  {
    id: "papers",
    title: "Scholarly Horizons",
    desc: "Discover peer-reviewed literature and preprints across every discipline. Surface the research that shapes tomorrow.",
    accentColor: "#ca9a5b",
    image: "/domains/papers.png",
    alt: "Academic Papers",
  },
  {
    id: "videos",
    title: "Visual Learning",
    desc: "Dive into seminar talks, lecture series, and educational content curated from the world's leading minds.",
    accentColor: "#7ba7cc",
    image: "/domains/videos.png",
    alt: "Video Lectures",
  },
  {
    id: "code",
    title: "Code Forge",
    desc: "Find framework implementations, developer tools, and open-source software — ready to build upon.",
    accentColor: "#7dab8a",
    image: "/domains/code.png",
    alt: "Code & Tools",
  },
  {
    id: "articles",
    title: "Written Insights",
    desc: "Explore modern tutorials, in-depth guides, and thought pieces from practitioners and thinkers.",
    accentColor: "#c4897e",
    image: "/domains/articles.png",
    alt: "Articles & Blogs",
  },
];

/* Background color transitions — subtle warm tones */
const BG_COLORS = ["#fdfbf7", "#f5f8fa", "#f7faf7", "#faf7f7"];

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

export default function DomainSection() {
  const sectionRef = useRef<HTMLElement>(null);
  const archRef = useRef<HTMLDivElement>(null);
  const rightRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!archRef.current || !rightRef.current || !sectionRef.current) return;

    // Set initial z-indices for individual image wrappers
    const wrappers = rightRef.current.querySelectorAll<HTMLElement>(".domain-img-wrapper");
    wrappers.forEach((el, i) => {
      el.style.zIndex = String(DOMAINS.length - i);
    });

    const ctx = gsap.context(() => {
      const mm = gsap.matchMedia();

      mm.add("(min-width: 769px)", () => {
        const imgs = gsap.utils.toArray<HTMLImageElement>(".domain-img-wrapper img");
        const contentBlocks = gsap.utils.toArray<HTMLElement>(".domain-content");

        // 1. PINNING: Create a unified timeline with pinnnig
        // We use pinType: "transform" for better compatibility with Lenis/smooth-scroll
        const mainTl = gsap.timeline({
          scrollTrigger: {
            trigger: archRef.current,
            start: "top top",
            end: "bottom bottom",
            pin: rightRef.current,
            pinSpacing: true,
            scrub: true,
            invalidateOnRefresh: true,
            anticipatePin: 1,
            // pinType: "transform" ensures the element doesn't "jump" to fixed document coordinates
            pinType: "transform",
          }
        });

        // Pre-set initial state
        gsap.set(imgs, { clipPath: "inset(0% 0% 0% 0%)", objectPosition: "center center" });

        // 2. TEXT ANIMATIONS: Correlate text opacity with scrubbed scroll
        contentBlocks.forEach((block, i) => {
          gsap.fromTo(
            block,
            { opacity: 0, y: 40 },
            {
              opacity: 1,
              y: 0,
              ease: "power2.out",
              scrollTrigger: {
                trigger: block.closest(".domain-arch__info"),
                start: "top 75%",
                end: "top 25%",
                scrub: 1,
              },
            }
          );
        });

        // 3. IMAGE SEQUENCE & BACKGROUND COLORS
        // We partition the timeline duration based on the number of domain segments
        const duration = 1 / (DOMAINS.length - 1);

        for (let i = 0; i < DOMAINS.length - 1; i++) {
          const currentImg = imgs[i];
          const nextImg = imgs[i+1];
          const startTime = i * duration;

          mainTl.to(currentImg, {
            clipPath: "inset(0% 0% 100% 0%)",
            objectPosition: "center 65%",
            duration: duration,
            ease: "none"
          }, startTime)
          .to(nextImg, {
            objectPosition: "center 35%",
            duration: duration,
            ease: "none"
          }, startTime)
          .to(sectionRef.current, {
            backgroundColor: BG_COLORS[i + 1],
            duration: duration * 0.5,
            ease: "power1.inOut"
          }, startTime + duration * 0.5);
        }
      });

      mm.add("(max-width: 768px)", () => {
        const imgs = gsap.utils.toArray<HTMLImageElement>(".domain-img-wrapper img");
        imgs.forEach((img) => {
          gsap.fromTo(img, 
            { objectPosition: "center 70%" },
            {
              objectPosition: "center 30%",
              ease: "none",
              scrollTrigger: {
                trigger: img.closest(".domain-img-wrapper"),
                start: "top bottom",
                end: "bottom top",
                scrub: true
              }
            }
          );
        });
      });
    });

    ScrollTrigger.refresh();

    return () => ctx.revert();
  }, []);

  return (
    <section
      ref={sectionRef}
      id="domain-section"
      className="relative w-full transition-colors duration-700 bg-theme-bg"
    >
      <div className="pt-32 pb-12 px-6 text-center max-w-4xl mx-auto">
        <p className="text-xs uppercase tracking-[0.3em] text-theme-muted mb-4 font-medium">
          DORA Intelligence Multi-Domain
        </p>
        <h2 className="text-4xl md:text-5xl lg:text-6xl font-serif italic text-theme-text/90 tracking-tight">
          Four lenses, one unified discovery engine
        </h2>
      </div>

      <div ref={archRef} className="domain-arch px-6">
        {/* Left scrolling list */}
        <div className="domain-arch__left">
          {DOMAINS.map((domain) => (
            <div key={domain.id} className="domain-arch__info">
              <div className="domain-content">
                <h3 className="domain-arch__header mb-4">{domain.title}</h3>
                <p className="domain-arch__desc mb-10">{domain.desc}</p>
                <a
                  href={`/search?domain=${domain.id}`}
                  className="domain-arch__link"
                  style={{ 
                    color: domain.accentColor,
                    borderColor: `${domain.accentColor}30`,
                    background: `${domain.accentColor}08`
                  }}
                >
                  <LeafIcon />
                  <span className="font-semibold text-sm">Start exploring</span>
                </a>
              </div>
            </div>
          ))}
        </div>

        {/* Right pinned column */}
        <div ref={rightRef} className="domain-arch__right">
          {DOMAINS.map((domain) => (
            <div
              key={domain.id}
              className="domain-img-wrapper"
            >
              <img src={domain.image} alt={domain.alt} />
            </div>
          ))}
        </div>
      </div>

      <div className="h-[20vh]" />
    </section>
  );
}
