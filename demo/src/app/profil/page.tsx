import Link from "next/link";
import { getAccount } from "@/data/accounts";
import { getPlace } from "@/data/places";
import { resources } from "@/data/resources";
import ReachBadge from "@/components/ReachBadge";

export default function ProfilPage() {
  const user = getAccount("demo-user")!;
  const place = getPlace(user.primaryPlaceId);
  const myResources = resources.filter((r) => r.ownerId === "demo-user");

  return (
    <div className="max-w-2xl mx-auto space-y-4">
      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <div className="flex items-center gap-4">
          <span
            className="w-16 h-16 rounded-2xl bg-[#e2f2e8] flex items-center justify-center text-3xl shrink-0"
            aria-hidden="true"
          >
            {user.avatarEmoji}
          </span>
          <div className="min-w-0">
            <h1 className="text-xl sm:text-2xl font-bold text-[#16221c] tracking-tight">
              {user.displayName}
            </h1>
            <p className="text-sm text-gray-600 mt-0.5">{user.bio}</p>
            <div className="flex items-center gap-2 mt-2 text-xs text-gray-600 flex-wrap">
              <span>
                <span aria-hidden="true">{"\u{1F4CD}"}</span>{" "}
                {place?.name || user.primaryPlaceId}
              </span>
              <span aria-hidden="true">{"\u{00B7}"}</span>
              <span>Dabei seit {user.joinedAt}</span>
            </div>
          </div>
        </div>
        <div className="grid grid-cols-2 gap-2.5 mt-5 pt-4 border-t border-stone-100">
          <Link
            href="/kanaele"
            className="flex items-center justify-center gap-2 bg-[#f4f6f1] border border-stone-200/90 rounded-xl py-2.5 px-3 text-sm font-semibold text-[#16221c] hover:border-emerald-700/40"
          >
            <span aria-hidden="true">{"\u{1F4E2}"}</span>
            <span>Meine Kanäle</span>
          </Link>
          <Link
            href="/nachrichten"
            className="flex items-center justify-center gap-2 bg-[#f4f6f1] border border-stone-200/90 rounded-xl py-2.5 px-3 text-sm font-semibold text-[#16221c] hover:border-emerald-700/40"
          >
            <span aria-hidden="true">{"\u{1F4AC}"}</span>
            <span>Nachrichten</span>
          </Link>
        </div>
      </div>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <h2 className="font-semibold text-[#16221c] mb-3">Datenschutz &amp; Reichweite</h2>
        <div className="divide-y divide-stone-100 text-sm">
          <div className="flex items-center justify-between py-2.5">
            <span className="text-gray-600">Anzeigename sichtbar für</span>
            <ReachBadge level="kommune" />
          </div>
          <div className="flex items-center justify-between py-2.5">
            <span className="text-gray-600">Kontaktaufnahme</span>
            <span className="text-emerald-800 font-semibold">Nur Direktnachricht</span>
          </div>
          <div className="flex items-center justify-between py-2.5">
            <span className="text-gray-600">Identität</span>
            <span className="text-[#16221c] font-medium">Pseudonym</span>
          </div>
          <div className="flex items-center justify-between py-2.5">
            <span className="text-gray-600">Standort</span>
            <span className="text-amber-800 font-medium">Nur Ortsteil</span>
          </div>
        </div>
      </div>

      <div className="bg-white border border-stone-200/90 rounded-2xl p-5 sm:p-6 shadow-[0_1px_2px_rgba(22,34,28,0.04)]">
        <h2 className="font-semibold text-[#16221c] mb-3">Meine Ressourcen</h2>
        {myResources.length === 0 ? (
          <p className="text-gray-600 text-sm leading-relaxed">
            Du hast noch keine eigenen Angebote eingetragen. Hier erscheinen Geräte,
            Können oder Hilfsangebote, die du mit deiner Nachbarschaft teilst.
          </p>
        ) : (
          <div className="space-y-2.5">
            {myResources.map((r) => (
              <Link
                key={r.id}
                href={`/ressourcen/${r.id}`}
                className="block border border-stone-200/80 rounded-xl p-3.5 hover:border-emerald-700/40"
              >
                <p className="font-semibold text-[#16221c] text-sm">{r.title}</p>
                <p className="text-xs text-gray-600 mt-0.5">{r.description}</p>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
