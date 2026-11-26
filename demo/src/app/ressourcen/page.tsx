import Card from "@/components/Card";
import ReachBadge from "@/components/ReachBadge";
import { resources } from "@/data/resources";
import { getAccount } from "@/data/accounts";
import { getPlace } from "@/data/places";

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

export default function RessourcenPage() {
  return (
    <div className="space-y-5">
      <header>
        <p className="text-xs font-semibold uppercase tracking-wider text-emerald-800 mb-1">
          Nachbarschaftshilfe
        </p>
        <h1 className="text-2xl font-bold text-[#16221c] tracking-tight">
          Teilen &amp; Ausleihen
        </h1>
        <p className="text-sm text-gray-600 mt-1">
          Geräte, Können, Räume oder Hilfe &mdash; ohne Geld, direkt im Ort.
        </p>
      </header>
      <div className="grid gap-3 md:grid-cols-2">
        {resources.map((res) => {
          const owner = getAccount(res.ownerId);
          const place = getPlace(res.placeId);
          return (
            <Card
              key={res.id}
              href={`/ressourcen/${res.id}`}
              emoji={kindIcons[res.resourceKind] || "\u{1F91D}"}
              title={res.title}
              subtitle={`${kindLabels[res.resourceKind] || res.resourceKind} \u{00B7} ${owner?.displayName || ""} \u{00B7} ${place?.name || ""}`}
              badge={<ReachBadge level={res.visibilityScope} />}
            >
              {res.description}
            </Card>
          );
        })}
      </div>
    </div>
  );
}
