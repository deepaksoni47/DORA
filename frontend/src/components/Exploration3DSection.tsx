"use client";

import { useEffect, useRef, useMemo } from "react";
import gsap from "gsap";
import ScrollTrigger from "gsap/ScrollTrigger";
import { Canvas, useFrame } from "@react-three/fiber";
import ScrollFloat from "./ScrollFloat";
import * as THREE from "three";

function ExplorationScene() {
  const points = useRef<THREE.Points>(null);
  const particlesCount = 600;

  const positions = useMemo(() => {
    const pos = new Float32Array(particlesCount * 3);
    for (let i = 0; i < particlesCount; i++) {
        const r = 12 * Math.cbrt(Math.random());
        const theta = Math.random() * 2 * Math.PI;
        const phi = Math.acos(2 * Math.random() - 1);
        pos[i * 3] = r * Math.sin(phi) * Math.cos(theta);
        pos[i * 3 + 1] = r * Math.sin(phi) * Math.sin(theta);
        pos[i * 3 + 2] = r * Math.cos(phi);
    }
    return pos;
  }, [particlesCount]);

  useEffect(() => {
    if (!points.current) return;
    
    // Animate the actual 3D points based on the scroll position of the section
    // Real 3D depth and rotation, no faux DOM scaling
    gsap.to(points.current.rotation, {
      y: Math.PI,
      x: Math.PI / 6,
      ease: "none",
      scrollTrigger: {
        trigger: "#exploration-section",
        start: "top bottom",
        end: "bottom top",
        scrub: 1,
      }
    });

    gsap.to(points.current.position, {
      z: 6, // plunge the camera into the sphere
      ease: "power2.inOut",
      scrollTrigger: {
        trigger: "#exploration-section",
        start: "top bottom",
        end: "bottom top",
        scrub: 1,
      }
    });
  }, []);

  useFrame((state, delta) => {
    if (points.current) {
      points.current.rotation.y += delta * 0.05; // Base idle rotation
    }
  });

  return (
    <points ref={points}>
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          args={[positions, 3]}
        />
      </bufferGeometry>
      <pointsMaterial
        size={0.06}
        color="#2a2a2a"
        transparent
        opacity={0.4}
        sizeAttenuation
      />
    </points>
  );
}

export default function Exploration3DSection() {
  const textRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    gsap.fromTo(textRef.current, 
      { opacity: 0, scale: 0.95 },
      { 
        opacity: 1, 
        scale: 1,
        ease: "power2.out",
        scrollTrigger: {
          trigger: "#exploration-section",
          start: "top 60%",
          end: "top 30%",
          scrub: 1,
        }
      }
    );
  }, []);

  return (
    <section id="exploration-section" className="relative w-full h-[120vh] flex items-center justify-center overflow-hidden bg-transparent">
      {/* 3D Component Wrapper */}
      <div className="absolute inset-0 w-full h-full pointer-events-none">
        <Canvas 
          camera={{ position: [0, 0, 8], fov: 60 }}
          dpr={[1, 1.5]}
          gl={{ antialias: false, powerPreference: "high-performance" }}
        >
          <ExplorationScene />
        </Canvas>
      </div>
      
      <div ref={textRef} className="relative z-10 w-full max-w-5xl px-4 md:px-6 text-center pointer-events-none pb-20">
        <ScrollFloat
          containerClassName="pb-20"
          textClassName="font-serif italic text-theme-text tracking-[0.03em] drop-shadow-sm opacity-90 !leading-[1.15] !text-[clamp(1.9rem,4.8vw,4.6rem)]"
          animationDuration={1.2}
          ease="power3.out"
          scrollStart="top 72%"
          scrollEnd="bottom 45%"
          stagger={0.02}
        >
          Search across interconnected knowledge domains
        </ScrollFloat>
      </div>
    </section>
  );
}




