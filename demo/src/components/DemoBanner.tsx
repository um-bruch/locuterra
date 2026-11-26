export default function DemoBanner() {
  return (
    <div className="locuterra-demo-banner bg-emerald-950 text-emerald-50 text-center text-xs sm:text-sm py-2 px-4">
      <span className="inline-block bg-emerald-800/80 text-emerald-100 font-semibold px-2 py-0.5 rounded-full mr-2">
        Pilot
      </span>
      Fiktive Kommune &bdquo;Grüntal&ldquo; &mdash; lokal &amp; ohne Echtbetrieb.{" "}
      <a
        href="https://github.com/um-bruch/locuterra"
        className="underline decoration-emerald-300/70 hover:text-white ml-1"
        target="_blank"
        rel="noopener noreferrer"
      >
        Mehr erfahren
      </a>
    </div>
  );
}
