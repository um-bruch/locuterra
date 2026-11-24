import Link from "next/link";
import ReachBadge from "@/components/ReachBadge";
import { channels } from "@/data/channels";
import { getPlace } from "@/data/places";

export default function KanaelePage() {
  return (
    <div className="space-y-5">
      <header>
        <p className="text-xs font-semibold uppercase tracking-wider text-emerald-800 mb-1">
          Aktuelles
        </p>
        <h1 className="text-2xl font-bold text-[#16221c] tracking-tight">
          Informationskanäle
        </h1>
        <p className="text-sm text-gray-600 mt-1">
          Offizielle Bekanntmachungen und lokale Kanäle aus Grüntal.
        </p>
      </header>
      <div className="space-y-4">
        {channels.map((ch) => {
          const place = getPlace(ch.placeId);
          return (
            <div
              key={ch.id}
              className="bg-white border border-stone-200/90 rounded-2xl overflow-hidden shadow-[0_1px_2px_rgba(22,34,28,0.04)]"
            >
              <div className="p-4 sm:p-5 border-b border-stone-100">
                <div className="flex items-start justify-between gap-3 flex-wrap">
                  <div className="flex items-center gap-3 min-w-0">
                    <span
                      className="w-10 h-10 rounded-xl bg-[#e2f2e8] flex items-center justify-center text-lg shrink-0"
                      aria-hidden="true"
                    >
                      {"\u{1F4E2}"}
                    </span>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <h2 className="font-semibold text-[#16221c]">
                          <Link
                            href={`/kanaele/${ch.id}`}
                            className="hover:text-emerald-800 hover:underline"
                          >
                            {ch.name}
                          </Link>
                        </h2>
                        <ReachBadge level={ch.visibilityScope} />
                      </div>
                      <p className="text-xs text-gray-600 mt-0.5">
                        {ch.subscriberCount} Abonnenten {"\u{00B7}"} {place?.name}
                      </p>
                    </div>
                  </div>
                  <Link
                    href={`/kanaele/${ch.id}`}
                    className="inline-flex items-center text-xs font-semibold text-emerald-900 bg-[#e2f2e8] px-3 py-1.5 rounded-full hover:bg-emerald-200/70 shrink-0"
                  >
                    Kanal öffnen &rarr;
                  </Link>
                </div>
                <p className="text-sm text-gray-600 mt-3 leading-relaxed">
                  {ch.description}
                </p>
              </div>
              <div className="divide-y divide-stone-100">
                {ch.posts.map((post) => (
                  <div key={post.id} className="p-4 sm:px-5">
                    <span className="text-xs text-gray-600 block mb-1">
                      {post.postedAt}
                    </span>
                    <h3 className="font-semibold text-[#16221c] text-sm">
                      {post.title}
                    </h3>
                    <p className="text-sm text-gray-600 mt-1 leading-relaxed">
                      {post.body}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
