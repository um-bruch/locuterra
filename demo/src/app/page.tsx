import Link from "next/link";
import { places } from "@/data/places";
import { groups } from "@/data/groups";
import { resources } from "@/data/resources";
import { channels } from "@/data/channels";

const stats = [
  { label: "Orte", count: places.length, href: "/orte", icon: "\u{1F4CD}", hint: "Treffpunkte" },
  { label: "Gruppen", count: groups.length, href: "/gruppen", icon: "\u{1F465}", hint: "Mitmachen" },
  { label: "Teilen", count: resources.length, href: "/ressourcen", icon: "\u{1F91D}", hint: "Ohne Geld" },
  { label: "Kanäle", count: channels.length, href: "/kanaele", icon: "\u{1F4E2}", hint: "Offiziell" },
];

export default function HomePage() {
  const feedPosts = channels.flatMap((ch) =>
    ch.posts.map((post) => ({ channel: ch, post }))
  );

  return (
    <div className="space-y-6">
      {/* Calm Civic Editorial Hero Card */}
      <section className="rounded-3xl bg-gradient-to-br from-[#0f5132] via-[#145c3a] to-[#1b6b45] text-white p-6 sm:p-8 shadow-[0_12px_32px_rgba(15,81,50,0.16)]">
        <div className="inline-flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wider bg-white/15 text-emerald-50 px-3 py-1 rounded-full mb-3">
          <span aria-hidden="true">{"\u{1F4CD}"}</span>
          <span>Kommune Grüntal</span>
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight leading-tight">
          Willkommen in Grüntal
        </h1>
        <p className="text-emerald-50/90 text-sm sm:text-base leading-relaxed mt-2 max-w-xl">
          Finde Orte in deiner Nähe, tritt Gruppen bei, teile Geräte oder Hilfe
          ohne Geld und bleib über offizielle Kanäle informiert.
        </p>
        <div className="flex flex-wrap gap-2.5 mt-5">
          <Link
            href="/ressourcen"
            className="inline-flex items-center gap-1.5 bg-white text-emerald-950 font-semibold text-sm px-4 py-2.5 rounded-xl shadow-xs hover:bg-emerald-50 transition-colors"
          >
            Ressourcen entdecken &rarr;
          </Link>
          <Link
            href="/nachrichten"
            className="inline-flex items-center gap-1.5 bg-white/15 text-white font-medium text-sm px-4 py-2.5 rounded-xl border border-white/25 hover:bg-white/25 transition-colors"
          >
            Nachrichten öffnen
          </Link>
        </div>
      </section>

      {/* Compact 2x2 / 4x1 Directory Cards */}
      <section aria-label="Bereiche in Grüntal" className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {stats.map((s) => (
          <Link
            key={s.href}
            href={s.href}
            className="bg-white border border-stone-200/90 rounded-2xl p-4 shadow-[0_1px_2px_rgba(22,34,28,0.04)] hover:border-emerald-700/40 hover:shadow-md transition-all flex flex-col justify-between"
          >
            <div className="flex items-center justify-between mb-3">
              <span
                className="w-9 h-9 rounded-xl bg-[#edf3ee] flex items-center justify-center text-lg"
                aria-hidden="true"
              >
                {s.icon}
              </span>
              <span className="text-2xl font-bold text-emerald-900">{s.count}</span>
            </div>
            <div>
              <div className="font-semibold text-sm text-[#16221c]">{s.label}</div>
              <div className="text-xs text-gray-600">{s.hint}</div>
            </div>
          </Link>
        ))}
      </section>

      {/* Feed Timeline */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-lg font-bold text-[#16221c] tracking-tight">
            Neuigkeiten aus Grüntal
          </h2>
          <Link
            href="/kanaele"
            className="text-xs font-semibold text-emerald-800 hover:underline"
          >
            Alle Kanäle &rarr;
          </Link>
        </div>
        <div className="space-y-3">
          {feedPosts.map(({ channel, post }) => (
            <Link
              key={post.id}
              href={`/kanaele/${channel.id}`}
              className="block bg-white border border-stone-200/90 rounded-2xl p-4 sm:p-5 shadow-[0_1px_2px_rgba(22,34,28,0.04)] hover:border-emerald-700/40 transition-all"
            >
              <div className="flex items-center justify-between gap-2 flex-wrap mb-1.5">
                <span className="text-[11px] font-semibold text-emerald-900 bg-[#e2f2e8] px-2.5 py-0.5 rounded-full">
                  {channel.name}
                </span>
                <span className="text-xs text-gray-600">{post.postedAt}</span>
              </div>
              <h3 className="font-semibold text-[#16221c] text-[15px] leading-snug">
                {post.title}
              </h3>
              <p className="text-sm text-gray-600 mt-1.5 leading-relaxed">
                {post.body}
              </p>
            </Link>
          ))}
        </div>
      </section>

      {/* Harmonious Civic Action Strip */}
      <section className="grid md:grid-cols-3 gap-3">
        <div className="bg-[#e8f3ec] border border-emerald-200/90 rounded-2xl p-5">
          <h2 className="font-semibold text-emerald-800 mb-1">
            Ressourcen teilen
          </h2>
          <p className="text-sm text-emerald-900/80 mb-3 leading-relaxed">
            Biete Geräte, Können oder Hilfe an &mdash; ganz ohne Geldfluss.
          </p>
          <Link
            href="/ressourcen"
            className="text-sm font-semibold text-emerald-900 hover:underline"
          >
            Alle Ressourcen ansehen &rarr;
          </Link>
        </div>
        <div className="bg-white border border-stone-200/90 rounded-2xl p-5">
          <h2 className="font-semibold text-[#16221c] mb-1">
            Gruppen entdecken
          </h2>
          <p className="text-sm text-gray-600 mb-3 leading-relaxed">
            Finde Initiativen in deiner Nähe und werde Teil der Nachbarschaft.
          </p>
          <Link
            href="/gruppen"
            className="text-sm font-semibold text-emerald-900 hover:underline"
          >
            Gruppen durchstöbern &rarr;
          </Link>
        </div>
        <div className="bg-[#f7f2e8] border border-amber-200/80 rounded-2xl p-5">
          <h2 className="font-semibold text-amber-950 mb-1">
            Orte erkunden
          </h2>
          <p className="text-sm text-amber-950/80 mb-3 leading-relaxed">
            Entdecke Treffpunkte, Einrichtungen und Plätze rund um Grüntal.
          </p>
          <Link
            href="/orte"
            className="text-sm font-semibold text-amber-950 hover:underline"
          >
            Orte ansehen &rarr;
          </Link>
        </div>
      </section>
    </div>
  );
}
