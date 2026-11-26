"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { isNavActive } from "@/lib/isNavActive";

const navItems = [
  { href: "/", label: "Start", icon: "\u{1F3E0}" },
  { href: "/orte", label: "Orte", icon: "\u{1F4CD}" },
  { href: "/gruppen", label: "Gruppen", icon: "\u{1F465}" },
  { href: "/ressourcen", label: "Teilen", icon: "\u{1F91D}" },
  { href: "/kanaele", label: "Kanäle", icon: "\u{1F4E2}" },
  { href: "/nachrichten", label: "Nachrichten", icon: "\u{1F4AC}" },
  { href: "/profil", label: "Ich", icon: "\u{1F464}" },
];

export default function Navigation() {
  const pathname = usePathname();

  return (
    <nav
      aria-label="Hauptnavigation"
      className="bg-white/95 backdrop-blur border-b border-stone-200/90 sticky top-0 z-40"
    >
      <div className="max-w-4xl mx-auto px-4">
        <div className="flex items-center justify-between h-14">
          <Link
            href="/"
            className="flex items-center gap-2.5 font-bold text-emerald-900 text-lg tracking-tight"
          >
            <span
              className="w-8 h-8 rounded-xl bg-emerald-900 text-white flex items-center justify-center text-sm shadow-xs"
              aria-hidden="true"
            >
              {"\u{1F30D}"}
            </span>
            <span>LOCUTERRA</span>
          </Link>
          <div className="hidden md:flex items-center gap-1">
            {navItems.map((item) => {
              const active = isNavActive(pathname, item.href);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  aria-current={active ? "page" : undefined}
                  className={`px-3 py-2 rounded-xl text-sm transition-colors ${
                    active
                      ? "bg-emerald-100/90 text-emerald-900 font-semibold"
                      : "text-gray-700 hover:bg-stone-100 hover:text-gray-900"
                  }`}
                >
                  <span className="mr-1.5" aria-hidden="true">
                    {item.icon}
                  </span>
                  {item.label}
                </Link>
              );
            })}
          </div>
        </div>
      </div>
      {/* Mobile navigation (Web/PWA fallback; hidden inside Android shell) */}
      <div className="md:hidden flex overflow-x-auto border-t border-stone-100 px-3 py-1.5 gap-1.5">
        {navItems.map((item) => {
          const active = isNavActive(pathname, item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-current={active ? "page" : undefined}
              className={`flex-shrink-0 px-3 py-1.5 rounded-full text-xs transition-colors ${
                active
                  ? "bg-emerald-900 text-white font-semibold"
                  : "bg-stone-100 text-gray-700 hover:bg-stone-200 hover:text-gray-900"
              }`}
            >
              <span className="mr-1" aria-hidden="true">
                {item.icon}
              </span>
              {item.label}
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
