import React, { useState } from 'react';
import {
  X,
  Shield,
  Trash2,
  UserCheck,
  AlertTriangle,
  Lock,
  RefreshCw,
} from 'lucide-react';
import type { AuthUser } from '../../types/astrology';
import { anonymizeAccount, deleteAccount } from '../../services/api';

interface PrivacySettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentUser: AuthUser | null;
  onUserUpdated: (user: AuthUser | null) => void;
}

export const PrivacySettingsModal: React.FC<PrivacySettingsModalProps> = ({
  isOpen,
  onClose,
  currentUser,
  onUserUpdated,
}) => {
  const [loadingAction, setLoadingAction] = useState<string | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen || !currentUser) return null;

  const handleAnonymize = async () => {
    if (!window.confirm('Anonymize your account? Your name will be masked as a pseudonymous identifier.')) return;
    setLoadingAction('anonymize');
    setError(null);
    try {
      const updated = await anonymizeAccount();
      onUserUpdated(updated);
      setStatusMessage('Your account identity has been masked successfully.');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to anonymize account.');
    } finally {
      setLoadingAction(null);
    }
  };

  const handleDeleteAccount = async () => {
    if (!window.confirm('PERMANENT ACTION: Are you sure you want to delete your account and all associated astrological profiles and chat histories? This cannot be undone.')) return;
    setLoadingAction('delete');
    setError(null);
    try {
      await deleteAccount();
      onUserUpdated(null);
      onClose();
      window.location.reload();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to delete account.');
      setLoadingAction(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md shadow-2xl p-6 relative space-y-5">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-100 transition-colors p-1"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3">
          <div className="p-3 rounded-xl bg-purple-500/20 border border-purple-500/30 text-purple-400">
            <Shield className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-slate-100">Privacy &amp; Data Protection</h3>
            <p className="text-xs text-slate-400">GDPR &amp; DPDP Compliant Data Rights</p>
          </div>
        </div>

        {statusMessage && (
          <div className="p-3 rounded-xl bg-emerald-950/60 border border-emerald-800 text-emerald-200 text-xs flex items-center gap-2">
            <UserCheck className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>{statusMessage}</span>
          </div>
        )}

        {error && (
          <div className="p-3 rounded-xl bg-red-950/60 border border-red-800 text-red-200 text-xs flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-red-400 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <div className="space-y-3 bg-slate-950/60 border border-slate-800/80 rounded-xl p-4 text-xs">
          <div className="flex justify-between items-center text-slate-400">
            <span>Account Email:</span>
            <span className="font-mono text-slate-200">{currentUser.email}</span>
          </div>
          <div className="flex justify-between items-center text-slate-400">
            <span>Native Name:</span>
            <span className="font-medium text-slate-200">{currentUser.full_name}</span>
          </div>
          <div className="flex justify-between items-center text-slate-400">
            <span>Role:</span>
            <span className="font-mono text-purple-300">{currentUser.role}</span>
          </div>
        </div>

        <div className="space-y-3 pt-2">
          {/* Anonymize Identity */}
          <button
            type="button"
            onClick={handleAnonymize}
            disabled={loadingAction !== null}
            className="w-full px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 disabled:opacity-50 text-slate-200 text-xs font-semibold border border-slate-700 flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            {loadingAction === 'anonymize' ? (
              <RefreshCw className="w-4 h-4 text-purple-400 animate-spin" />
            ) : (
              <Lock className="w-4 h-4 text-purple-400" />
            )}
            <span>Anonymize Native Identity (Pseudonymize)</span>
          </button>

          {/* Delete Account & Purge Data */}
          <button
            type="button"
            onClick={handleDeleteAccount}
            disabled={loadingAction !== null}
            className="w-full px-4 py-2.5 rounded-xl bg-red-950/60 hover:bg-red-900/60 disabled:opacity-50 text-red-200 text-xs font-semibold border border-red-800/80 flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            {loadingAction === 'delete' ? (
              <RefreshCw className="w-4 h-4 text-red-400 animate-spin" />
            ) : (
              <Trash2 className="w-4 h-4 text-red-400" />
            )}
            <span>Delete Account &amp; Purge All Birth Data</span>
          </button>
        </div>

        <p className="text-[11px] text-slate-500 text-center">
          All birth coordinates and consultation traces are strictly safeguarded under zero-retention calculation invariants.
        </p>
      </div>
    </div>
  );
};
