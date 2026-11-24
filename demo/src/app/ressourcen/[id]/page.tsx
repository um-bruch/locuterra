import Link from "next/link";
import { resources, getResource } from "@/data/resources";
import { getAccount } from "@/data/accounts";
import { getPlace } from "@/data/places";
import ReachBadge from "@/components/ReachBadge";

const kindLabels: Record<string, string> = {
  geraet: "Gerät",
  skill: "Können",
  suche: "Suche",
  raum: "Raum",
  hilfe: "Hilfe",
};

const kindIcons: Record<string, string> = {
  geraet: "\u{1F527}",
  skill: "\u{1F4A1}",
  suche: "\u{1F50D}",
  raum: "\u{1F3E0}",
  hilfe: "\u{2764}",
};

export function generateStaticParams() {
  return resources.map((r) => ({ id: r.id }));
}

export default async function RessourcenDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const resource = getResource(id);
  if (!resource) return <p>Ressource nicht gefunden.</p>;

  const owner = getAccount(resource.ownerId);
  const place = getPlace(resource.placeId);

  return (
    <div className="max-w-3xl mx-auto space-y-5">
      <Link
        href="/ressourcen"
        className="inline-flex items-center gap-1 text-sm font-semibold text-emerald-800 hover:text-emerald-950"
      >
        &larr; Alle Ressourcen
      </Link>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <div className="flex items-start gap-3.5">
          <span
            className="w-12 h-12 rounded-2xl bg-[#edf3ee] flex items-center justify-center text-2xl shrink-0"
            aria-hidden="true"
          >
            {kindIcons[resource.resourceKind] || "\u{1F91D}"}
          </span>
          <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-xl sm:text-2xl font-bold text-[#16221c] tracking-tight">
                {resource.title}
              </h1>
              <ReachBadge level={resource.visibilityScope} />
            </div>
            <p className="text-xs font-medium text-gray-600 mt-1">
              {kindLabels[resource.resourceKind] || resource.resourceKind} · Kostenlose Nachbarschaftshilfe
            </p>
          </div>
        </div>
        <p className="text-sm text-gray-600 mt-4 leading-relaxed">
          {resource.description}
        </p>
      </div>

      <div className="grid sm:grid-cols-2 gap-3">
        {owner && (
          <div className="bg-white border border-stone-200/90 rounded-2xl p-4 sm:p-5 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-gray-600 mb-2.5">
              Anbieter
            </h2>
            <div className="flex items-center gap-3">
              <span
                className="w-10 h-10 rounded-full bg-[#e2f2e8] flex items-center justify-center text-xl shrink-0"
                aria-hidden="true"
              >
                {owner.avatarEmoji}
              </span>
              <div>
                <p className="font-semibold text-[#16221c] text-sm">
                  {owner.displayName}
                </p>
                <p className="text-xs text-gray-600">{owner.bio}</p>
              </div>
            </div>
          </div>
        )}
        {place && (
          <div className="bg-white border border-stone-200/90 rounded-2xl p-4 sm:p-5 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-gray-600 mb-2.5">
              Ort
            </h2>
            <Link
              href={`/orte/${place.id}`}
              className="flex items-center gap-2 font-semibold text-sm text-emerald-800 hover:underline"
            >
              <span aria-hidden="true">{"\u{1F4CD}"}</span>
              <span>{place.name}</span>
            </Link>
            <p className="text-xs text-gray-600 mt-1 leading-relaxed">
              {place.description}
            </p>
          </div>
        )}
      </div>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <h2 className="font-semibold text-[#16221c] mb-1.5">Kontakt aufnehmen</h2>
        <p className="text-sm text-gray-600 mb-4 leading-relaxed">
          Schreibe eine direkte, nicht-kommerzielle Anfrage an {owner?.displayName || "den Anbieter"}.
        </p>
        <button
          disabled
          className="w-full sm:w-auto bg-emerald-200 text-emerald-600 px-5 py-3 rounded-xl text-sm font-semibold cursor-not-allowed"
        >
          Nachricht schreiben (Demo)
        </button>
      </div>
    </div>
  );
}
