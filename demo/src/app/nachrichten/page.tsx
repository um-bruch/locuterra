"use client";

import { useState } from "react";
import { conversations, getMessagesForConversation } from "@/data/messages";
import { getAccount } from "@/data/accounts";

export default function NachrichtenPage() {
  const [activeConv, setActiveConv] = useState(conversations[0]?.id || "");
  const msgs = getMessagesForConversation(activeConv);
  const conv = conversations.find((c) => c.id === activeConv);
  const conversationPanelId = "conversation-panel";

  return (
    <div className="space-y-5">
      <header>
        <p className="text-xs font-semibold uppercase tracking-wider text-emerald-800 mb-1">
          Direktaustausch
        </p>
        <h1 className="text-2xl font-bold text-[#16221c] tracking-tight">
          Nachrichten
        </h1>
        <p className="text-sm text-gray-600 mt-1">
          Direktnachrichten mit Menschen aus deiner Nachbarschaft.
        </p>
      </header>

      <div className="grid md:grid-cols-[270px_1fr] gap-4">
        {/* Conversation list */}
        <div
          className="bg-white border border-stone-200/90 rounded-2xl overflow-hidden shadow-[0_1px_2px_rgba(22,34,28,0.04)]"
          role="list"
          aria-label="Konversationen"
        >
          {conversations.map((c) => {
            const other = c.participantIds.find((id) => id !== "demo-user");
            const account = other ? getAccount(other) : null;
            const isActive = c.id === activeConv;
            const conversationLabel = `${account?.displayName || "Unbekannt"}: ${c.subject}`;
            return (
              <button
                key={c.id}
                onClick={() => setActiveConv(c.id)}
                type="button"
                aria-controls={conversationPanelId}
                aria-pressed={isActive}
                aria-label={conversationLabel}
                title={conversationLabel}
                className={`w-full text-left p-3.5 border-b border-stone-100 last:border-b-0 transition-colors ${
                  isActive
                    ? "bg-[#e8f3ec]"
                    : "hover:bg-stone-50"
                }`}
              >
                <div className="flex items-center gap-3">
                  <span
                    className="w-10 h-10 rounded-full bg-[#edf3ee] flex items-center justify-center text-lg shrink-0"
                    aria-hidden="true"
                  >
                    {account?.avatarEmoji || "\u{1F464}"}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-semibold text-[#16221c] truncate">
                      {account?.displayName || "Unbekannt"}
                    </p>
                    <p className="text-xs text-gray-600 truncate mt-0.5">
                      {c.subject}
                    </p>
                  </div>
                </div>
              </button>
            );
          })}
        </div>

        {/* Messages */}
        <div
          id={conversationPanelId}
          className="bg-white border border-stone-200/90 rounded-2xl p-4 sm:p-5 shadow-[0_1px_2px_rgba(22,34,28,0.04)] flex flex-col"
          role="region"
          aria-label={conv ? `Nachrichten zu ${conv.subject}` : "Nachrichten"}
        >
          {conv && (
            <div className="border-b border-stone-100 pb-3 mb-4">
              <p className="font-semibold text-[#16221c] text-sm">{conv.subject}</p>
            </div>
          )}
          <div className="space-y-3 flex-1">
            {msgs.map((msg) => {
              const sender = getAccount(msg.senderAccountId);
              const isMe = msg.senderAccountId === "demo-user";
              return (
                <div
                  key={msg.id}
                  className={`flex ${isMe ? "justify-end" : "justify-start"}`}
                >
                  <div
                    className={`max-w-[85%] rounded-2xl px-3.5 py-2.5 ${
                      isMe
                        ? "bg-[#e2f2e8] text-emerald-950 rounded-br-sm"
                        : "bg-stone-100 text-[#16221c] rounded-bl-sm"
                    }`}
                  >
                    <p className="text-xs font-semibold mb-0.5">
                      <span aria-hidden="true">{sender?.avatarEmoji}</span>{" "}
                      {sender?.displayName}
                    </p>
                    <p className="text-sm leading-relaxed">{msg.body}</p>
                    <p className="text-[11px] text-gray-600 mt-1">
                      {new Date(msg.sentAt).toLocaleString("de-DE", {
                        day: "2-digit",
                        month: "2-digit",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
          <div className="mt-4 pt-3 border-t border-stone-100 flex gap-2">
            <input
              type="text"
              placeholder="Nachricht schreiben... (Demo)"
              disabled
              aria-label="Nachricht schreiben (Demo deaktiviert)"
              className="flex-1 border border-stone-200 rounded-xl px-3.5 py-2.5 text-sm bg-gray-50 text-gray-400"
            />
            <button
              disabled
              type="button"
              aria-label="Nachricht senden (im Demo-Modus deaktiviert)"
              title="Nachricht senden (Demo deaktiviert)"
              className="bg-emerald-200 text-emerald-600 px-4 py-2.5 rounded-xl text-sm font-semibold cursor-not-allowed"
            >
              Senden
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
