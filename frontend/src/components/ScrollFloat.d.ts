import { JSX } from "react";

export interface ScrollFloatProps {
  children: string;
  scrollContainerRef?: { current: HTMLElement | null };
  containerClassName?: string;
  textClassName?: string;
  animationDuration?: number;
  ease?: string;
  scrollStart?: string;
  scrollEnd?: string;
  stagger?: number;
}

declare const ScrollFloat: (props: ScrollFloatProps) => JSX.Element;
export default ScrollFloat;
