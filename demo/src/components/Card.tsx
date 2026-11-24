import Link from "next/link";
import { ReactNode } from "react";

interface CardProps {
  href: string;
  emoji: string;
  title: string;
  subtitle?: string;
  badge?: ReactNode;
  headingLevel?: "h2" | "h3";
  children: ReactNode;
}

export default function Card({
  href,
  emoji,
  title,
  subtitle,
  badge,
  headingLevel = "h2",
  children,
}: CardProps) {
  const Heading = headingLevel;
  return (
    <Link
      href={href}
      className="group block bg-white border border-stone-200/90 rounded-2xl p-4 sm:p-5 shadow-[0_1px_2px_rgba(22,34,28,0.04)] hover:border-emerald-700/40 hover:shadow-[0_6px_20px_rgba(15,81,50,0.08)] active:scale-[0.99] transition-all"
    >
      <div className="flex items-start gap-3.5">
        <span
          className="w-11 h-11 rounded-xl bg-[#edf3ee] text-xl flex items-center justify-center flex-shrink-0"
          aria-hidden="true"
        >
          {emoji}
        </span>
        <div className="min-w-0 flex-1">
          <div className="flex items-center justify-between gap-2 flex-wrap">
            <Heading className="font-semibold text-[#16221c] text-[15px] leading-snug group-hover:text-emerald-900">
              {title}
            </Heading>
            {badge}
          </div>
          {subtitle && (
            <p className="text-xs font-medium text-gray-600 mt-0.5">{subtitle}</p>
          )}
          <div className="text-sm text-gray-600 mt-2 leading-relaxed line-clamp-2">
            {children}
          </div>
        </div>
      </div>
    </Link>
  );
}
