import type { Metadata, Viewport } from "next";
import "./globals.css";
import Navigation from "@/components/Navigation";
import DemoBanner from "@/components/DemoBanner";
import SwRegister from "@/components/SwRegister";

export const metadata: Metadata = {
  metadataBase: new URL("https://um-bruch.github.io"),
  title: "LOCUTERRA — Civic Social Network Demonstrator",
  description:
    "Open-source concept demonstrator for a public-interest, location-based civic social network.",
  keywords: [
    "LOCUTERRA",
    "civic social network",
    "location-based community platform",
    "municipal digital commons",
    "public interest social network",
    "Next.js civic tech demonstrator",
  ],
  alternates: {
    canonical: "/locuterra/",
  },
  openGraph: {
    title: "LOCUTERRA — Civic Social Network Demonstrator",
    description:
      "A public-interest, location-based civic network concept for municipalities, non-profits, and public-sector stewards.",
    url: "/locuterra/",
    siteName: "LOCUTERRA",
    images: [
      {
        url: "/locuterra/demo-preview.png",
        width: 1280,
        height: 720,
        alt: "LOCUTERRA demo home screen for the fictional municipality Grüntal",
      },
    ],
    locale: "de_DE",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "LOCUTERRA — Civic Social Network Demonstrator",
    description:
      "Open-source concept demonstrator for a public-interest, location-based civic social network.",
    images: ["/locuterra/demo-preview.png"],
  },
  manifest: "/locuterra/manifest.webmanifest",
};

export const viewport: Viewport = {
  themeColor: "#0f5132",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="de" className="h-full antialiased">
      <body className="min-h-full flex flex-col bg-[#f4f6f1] text-[#16221c]">
        <a
          href="#main-content"
          className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:bg-emerald-900 focus:text-white focus:px-4 focus:py-2 focus:rounded-xl focus:text-sm focus:font-medium focus:shadow-lg"
        >
          Zum Hauptinhalt springen
        </a>
        <SwRegister />
        <DemoBanner />
        <Navigation />
        <main
          id="main-content"
          tabIndex={-1}
          className="flex-1 max-w-4xl mx-auto w-full px-4 py-5 sm:py-7 focus:outline-none"
        >
          {children}
        </main>
        <footer className="locuterra-footer border-t border-stone-200/90 bg-white/70 py-4 text-center text-xs text-gray-600">
          LOCUTERRA Konzept-Demonstrator &mdash; Fiktive Daten, keine echten
          Personen &mdash;{" "}
          <a
            href="https://um-bruch.org"
            className="underline hover:text-emerald-800"
            target="_blank"
            rel="noopener noreferrer"
          >
            Um:bruch
          </a>
        </footer>
      </body>
    </html>
  );
}
