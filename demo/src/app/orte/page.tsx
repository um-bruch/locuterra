import Card from "@/components/Card";
import ReachBadge from "@/components/ReachBadge";
import { places } from "@/data/places";

const placeIcons: Record<string, string> = {
  stadtteil: "\u{1F3D8}",
  treffpunkt: "\u{1F91D}",
  einrichtung: "\u{1F3DB}",
  natur: "\u{1F333}",
  platz: "\u{26F2}",
};

export default function OrtePage() {
  return (
    <div className="space-y-5">
      <header>
        <p className="text-xs font-semibold uppercase tracking-wider text-emerald-800 mb-1">
          Kommune Grüntal
        </p>
        <h1 className="text-2xl font-bold text-[#16221c] tracking-tight">
          Orte in Grüntal
        </h1>
        <p className="text-sm text-gray-600 mt-1">
          Entdecke Ortsteile, Treffpunkte, Einrichtungen und Naturorte in deiner Nähe.
        </p>
      </header>
      <div className="grid gap-3 md:grid-cols-2">
        {places.map((place) => (
          <Card
            key={place.id}
            href={`/orte/${place.id}`}
            emoji={placeIcons[place.placeType] || "\u{1F4CD}"}
            title={place.name}
            subtitle={place.placeType.charAt(0).toUpperCase() + place.placeType.slice(1)}
            badge={<ReachBadge level={place.visibilityScope} />}
          >
            {place.description}
          </Card>
        ))}
      </div>
    </div>
  );
}
