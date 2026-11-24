import Link from "next/link";
import { places, getPlace } from "@/data/places";
import { groups } from "@/data/groups";
import { resources } from "@/data/resources";
import ReachBadge from "@/components/ReachBadge";
import Card from "@/components/Card";

export function generateStaticParams() {
  return places.map((p) => ({ id: p.id }));
}

export default async function OrteDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const place = getPlace(id);
  if (!place) return <p>Ort nicht gefunden.</p>;

  const parent = place.parentPlaceId ? getPlace(place.parentPlaceId) : null;
  const children = places.filter((p) => p.parentPlaceId === id);
  const placeGroups = groups.filter((g) => g.placeId === id);
  const placeResources = resources.filter((r) => r.placeId === id);

  return (
    <div className="max-w-3xl mx-auto space-y-5">
      <Link
        href="/orte"
        className="inline-flex items-center gap-1 text-sm font-semibold text-emerald-800 hover:text-emerald-950"
      >
        &larr; Alle Orte
      </Link>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <div className="flex items-center gap-2.5 flex-wrap mb-2">
          <h1 className="text-2xl font-bold text-[#16221c] tracking-tight">
            {place.name}
          </h1>
          <ReachBadge level={place.visibilityScope} />
        </div>
        <p className="text-sm text-gray-600 leading-relaxed">{place.description}</p>
        {parent && (
          <p className="text-sm text-gray-600 mt-3 pt-3 border-t border-stone-100">
            Gehört zu:{" "}
            <Link
              href={`/orte/${parent.id}`}
              className="font-semibold text-emerald-800 hover:underline"
            >
              {parent.name}
            </Link>
          </p>
        )}
      </div>

      {children.length > 0 && (
        <section>
          <h2 className="font-semibold text-[#16221c] mb-3">Unterorte</h2>
          <div className="grid gap-3">
            {children.map((c) => (
              <Card
                key={c.id}
                href={`/orte/${c.id}`}
                emoji={"\u{1F4CD}"}
                title={c.name}
                headingLevel="h3"
                badge={<ReachBadge level={c.visibilityScope} />}
              >
                {c.description}
              </Card>
            ))}
          </div>
        </section>
      )}

      {placeGroups.length > 0 && (
        <section>
          <h2 className="font-semibold text-[#16221c] mb-3">Gruppen an diesem Ort</h2>
          <div className="grid gap-3">
            {placeGroups.map((g) => (
              <Card
                key={g.id}
                href={`/gruppen/${g.id}`}
                emoji={"\u{1F465}"}
                title={g.name}
                subtitle={`${g.memberCount} Mitglieder`}
                headingLevel="h3"
                badge={<ReachBadge level={g.visibilityScope} />}
              >
                {g.description}
              </Card>
            ))}
          </div>
        </section>
      )}

      {placeResources.length > 0 && (
        <section>
          <h2 className="font-semibold text-[#16221c] mb-3">Ressourcen an diesem Ort</h2>
          <div className="grid gap-3">
            {placeResources.map((r) => (
              <Card
                key={r.id}
                href={`/ressourcen/${r.id}`}
                emoji={"\u{1F91D}"}
                title={r.title}
                headingLevel="h3"
                badge={<ReachBadge level={r.visibilityScope} />}
              >
                {r.description}
              </Card>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
