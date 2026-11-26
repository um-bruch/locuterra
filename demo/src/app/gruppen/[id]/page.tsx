import Link from "next/link";
import { groups, getGroup } from "@/data/groups";
import { getAccount, accounts } from "@/data/accounts";
import { getPlace } from "@/data/places";
import ReachBadge from "@/components/ReachBadge";

export function generateStaticParams() {
  return groups.map((g) => ({ id: g.id }));
}

export default async function GruppenDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const group = getGroup(id);
  if (!group) return <p>Gruppe nicht gefunden.</p>;

  const steward = getAccount(group.stewardAccountId);
  const place = getPlace(group.placeId);
  const sampleMembers = accounts.slice(0, 4);

  return (
    <div className="max-w-3xl mx-auto space-y-5">
      <Link
        href="/gruppen"
        className="inline-flex items-center gap-1 text-sm font-semibold text-emerald-800 hover:text-emerald-950"
      >
        &larr; Alle Gruppen
      </Link>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <div className="flex items-start gap-3.5">
          <span
            className="w-12 h-12 rounded-2xl bg-[#e2f2e8] flex items-center justify-center text-2xl shrink-0"
            aria-hidden="true"
          >
            {"\u{1F465}"}
          </span>
          <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-xl sm:text-2xl font-bold text-[#16221c] tracking-tight">
                {group.name}
              </h1>
              <ReachBadge level={group.visibilityScope} />
            </div>
            <p className="text-xs font-medium text-gray-600 mt-1">
              {group.memberCount} Mitglieder {"\u{00B7}"}{" "}
              {group.membershipPolicy === "offen"
                ? "Offene Gruppe"
                : group.membershipPolicy === "einladung"
                  ? "Nur mit Einladung"
                  : "Auf Genehmigung"}
            </p>
          </div>
        </div>
        <p className="text-sm text-gray-600 mt-4 leading-relaxed">
          {group.description}
        </p>
        <div className="flex items-center gap-4 flex-wrap mt-4 pt-4 border-t border-stone-100 text-sm text-gray-600">
          {place && (
            <Link
              href={`/orte/${place.id}`}
              className="font-semibold text-emerald-800 hover:underline"
            >
              <span aria-hidden="true">{"\u{1F4CD}"}</span> {place.name}
            </Link>
          )}
          {steward && (
            <span>
              Ansprechperson: <span aria-hidden="true">{steward.avatarEmoji}</span>{" "}
              {steward.displayName}
            </span>
          )}
        </div>
      </div>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <h2 className="font-semibold text-[#16221c] mb-3">Mitglieder</h2>
        <div className="flex flex-wrap gap-2">
          {sampleMembers.map((a) => (
            <div
              key={a.id}
              className="flex items-center gap-2 bg-[#f4f6f1] border border-stone-200/80 rounded-full px-3 py-1.5"
            >
              <span className="text-base" aria-hidden="true">{a.avatarEmoji}</span>
              <span className="text-xs font-medium text-[#16221c]">{a.displayName}</span>
            </div>
          ))}
          {group.memberCount > sampleMembers.length && (
            <div className="flex items-center text-xs font-medium text-gray-600 px-3 py-1.5">
              +{group.memberCount - sampleMembers.length} weitere
            </div>
          )}
        </div>
      </div>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <h2 className="font-semibold text-[#16221c] mb-1.5">Mitmachen</h2>
        <p className="text-sm text-gray-600 mb-4 leading-relaxed">
          {group.membershipPolicy === "offen"
            ? "Diese Gruppe ist offen — du kannst direkt beitreten."
            : group.membershipPolicy === "einladung"
              ? "Diese Gruppe ist nur per Einladung zugänglich."
              : "Ein Beitritt wird kurz von der Gruppen-Ansprechperson bestätigt."}
        </p>
        <button
          disabled
          className="w-full sm:w-auto bg-emerald-200 text-emerald-600 px-5 py-3 rounded-xl text-sm font-semibold cursor-not-allowed"
        >
          Beitreten (Demo)
        </button>
      </div>
    </div>
  );
}
