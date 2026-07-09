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
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Nachrichten</h1>
      <p className="text-gray-500 mb-6">
        Direktnachrichten mit anderen Bürgerinnen und Bürgern.
      </p>

      <div className="grid md:grid-cols-[280px_1fr] gap-4">
        {/* Conversation list */}
        <div
          className="bg-white border border-gray-200 rounded-xl overflow-hidden"
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
                className={`w-full text-left p-3 border-b border-gray-50 transition-colors ${
                  isActive
                    ? "bg-emerald-50"
                    : "hover:bg-gray-50"
                }`}
              >
                <div className="flex items-center gap-2">
                  <span className="text-lg">
                    {account?.avatarEmoji || "\u{1F464}"}
                  </span>
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {account?.displayName || "Unbekannt"}
                    </p>
                    <p className="text-xs text-gray-500 truncate">{c.subject}</p>
                  </div>
                </div>
              </button>
            );
          })}
        </div>

        {/* Messages */}
        <div
          id={conversationPanelId}
          className="bg-white border border-gray-200 rounded-xl p-4"
          role="region"
          aria-label={conv ? `Nachrichten zu ${conv.subject}` : "Nachrichten"}
        >
          {conv && (
            <div className="border-b border-gray-100 pb-2 mb-4">
              <p className="font-medium text-gray-900 text-sm">{conv.subject}</p>
            </div>
          )}
          <div className="space-y-3">
            {msgs.map((msg) => {
              const sender = getAccount(msg.senderAccountId);
              const isMe = msg.senderAccountId === "demo-user";
              return (
                <div
                  key={msg.id}
                  className={`flex ${isMe ? "justify-end" : "justify-start"}`}
                >
                  <div
                    className={`max-w-[80%] rounded-xl px-3 py-2 ${
                      isMe
                        ? "bg-emerald-100 text-emerald-900"
                        : "bg-gray-100 text-gray-900"
                    }`}
                  >
                    <p className="text-xs font-medium mb-0.5">
                      {sender?.avatarEmoji} {sender?.displayName}
                    </p>
                    <p className="text-sm">{msg.body}</p>
                    <p className="text-[10px] text-gray-400 mt-1">
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
          <div className="mt-4 flex gap-2">
            <input
              type="text"
              placeholder="Nachricht schreiben... (Demo)"
              disabled
              aria-label="Nachricht schreiben (Demo deaktiviert)"
              className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-400"
            />
            <button
              disabled
              type="button"
              aria-label="Nachricht senden (im Demo-Modus deaktiviert)"
              title="Nachricht senden (Demo deaktiviert)"
              className="bg-emerald-200 text-emerald-600 px-4 py-2 rounded-lg text-sm cursor-not-allowed"
            >
              Senden
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
