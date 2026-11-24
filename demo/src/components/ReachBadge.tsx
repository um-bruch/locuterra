import { ReachLevel } from "@/data/types";

const reachLabels: Record<ReachLevel, string> = {
  privat: "Privat",
  gruppe: "Gruppe",
  ort: "Ort",
  dorf: "Ortsteil",
  kommune: "Kommune",
  region: "Region",
  land: "Land",
  national: "National",
  transnational: "Transnational",
};

const reachColors: Record<ReachLevel, string> = {
  privat: "bg-stone-100 text-stone-700 border-stone-200",
  gruppe: "bg-sky-50 text-sky-800 border-sky-200/80",
  ort: "bg-teal-50 text-teal-800 border-teal-200/80",
  dorf: "bg-emerald-50 text-emerald-800 border-emerald-200/80",
  kommune: "bg-emerald-100/80 text-emerald-900 border-emerald-300/70",
  region: "bg-amber-50 text-amber-800 border-amber-200/80",
  land: "bg-orange-50 text-orange-800 border-orange-200/80",
  national: "bg-rose-50 text-rose-800 border-rose-200/80",
  transnational: "bg-violet-50 text-violet-800 border-violet-200/80",
};

export default function ReachBadge({ level }: { level: ReachLevel }) {
  return (
    <span
      className={`inline-flex items-center text-[11px] font-semibold px-2.5 py-0.5 rounded-full border ${reachColors[level]}`}
    >
      {reachLabels[level]}
    </span>
  );
}
