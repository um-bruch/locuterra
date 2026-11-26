import Card from "@/components/Card";
import ReachBadge from "@/components/ReachBadge";
import { groups } from "@/data/groups";
import { getAccount } from "@/data/accounts";
import { getPlace } from "@/data/places";

export default function GruppenPage() {
  return (
    <div className="space-y-5">
      <header>
        <p className="text-xs font-semibold uppercase tracking-wider text-emerald-800 mb-1">
          Mitmachen
        </p>
        <h1 className="text-2xl font-bold text-[#16221c] tracking-tight">
          Gruppen in Grüntal
        </h1>
        <p className="text-sm text-gray-600 mt-1">
          Lokale Initiativen, Vereine und offene Gruppen in deiner Nachbarschaft.
        </p>
      </header>
      <div className="grid gap-3 md:grid-cols-2">
        {groups.map((group) => {
          const steward = getAccount(group.stewardAccountId);
          const place = getPlace(group.placeId);
          return (
            <Card
              key={group.id}
              href={`/gruppen/${group.id}`}
              emoji="\u{1F465}"
              title={group.name}
              subtitle={`${group.memberCount} Mitglieder \u{00B7} ${place?.name || ""}`}
              badge={<ReachBadge level={group.visibilityScope} />}
            >
              <p>{group.description}</p>
              {steward && (
                <p className="text-xs text-gray-600 mt-1.5">
                  Ansprechperson: {steward.displayName}
                </p>
              )}
            </Card>
          );
        })}
      </div>
    </div>
  );
}
