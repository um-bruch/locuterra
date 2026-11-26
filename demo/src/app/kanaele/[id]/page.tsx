import Link from "next/link";
import { channels, getChannel } from "@/data/channels";
import { getPlace } from "@/data/places";
import ReachBadge from "@/components/ReachBadge";

export function generateStaticParams() {
  return channels.map((c) => ({ id: c.id }));
}

export default async function KanalDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const channel = getChannel(id);
  if (!channel) return <p>Kanal nicht gefunden.</p>;

  const place = getPlace(channel.placeId);

  return (
    <div className="max-w-3xl mx-auto space-y-5">
      <Link
        href="/kanaele"
        className="inline-flex items-center gap-1 text-sm font-semibold text-emerald-800 hover:text-emerald-950"
      >
        &larr; Alle Kanäle
      </Link>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <div className="flex items-start gap-3.5">
          <span
            className="w-12 h-12 rounded-2xl bg-[#e2f2e8] flex items-center justify-center text-2xl shrink-0"
            aria-hidden="true"
          >
            📢
          </span>
          <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-xl sm:text-2xl font-bold text-[#16221c] tracking-tight">
                {channel.name}
              </h1>
              <ReachBadge level={channel.visibilityScope} />
            </div>
            <p className="text-xs font-medium text-gray-600 mt-1">
              {channel.subscriberCount} Abonnenten
              {place ? ` · ${place.name}` : ""}
            </p>
          </div>
        </div>
        <p className="text-sm text-gray-600 mt-4 leading-relaxed">
          {channel.description}
        </p>
        <div className="mt-5">
          <button
            disabled
            className="w-full sm:w-auto bg-emerald-200 text-emerald-600 px-5 py-3 rounded-xl text-sm font-semibold cursor-not-allowed"
          >
            Abonnieren (Demo)
          </button>
        </div>
      </div>

      <div className="bg-white border border-stone-200/90 rounded-2xl overflow-hidden shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <div className="px-5 py-4 border-b border-stone-100">
          <h2 className="font-semibold text-[#16221c]">
            Beiträge ({channel.posts.length})
          </h2>
        </div>
        <div className="divide-y divide-stone-100">
          {channel.posts.map((post) => (
            <div key={post.id} className="p-5">
              <p className="text-xs text-gray-600 mb-1">{post.postedAt}</p>
              <h3 className="font-semibold text-[#16221c]">{post.title}</h3>
              <p className="text-sm text-gray-600 mt-1.5 leading-relaxed">
                {post.body}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
