import React, { useState } from 'react';
import {
  Compass,
  Search,
  BookOpen,
  Layers,
  Sparkles,
  CheckSquare,
} from 'lucide-react';
import type {
  AnalysisFrameworkDefinition,
  QuestionClassificationResponse,
} from '../../types/astrology';
import { PLANET_COLORS } from '../../utils/chartMath';

interface FrameworkExplorerSectionProps {
  frameworkData: QuestionClassificationResponse | null;
  onClassifyQuestion: (questionText: string) => void;
}

const SAMPLE_QUESTIONS = [
  'When will I get a job promotion and leadership authority?',
  'When will I get married and how is my spouse compatibility in D9?',
  'How are my financial wealth, stock investments, and Dhana Yogas?',
  'Can I buy a new house and luxury vehicle soon?',
  'Will I relocate abroad or settle in a foreign country?',
  'How is my higher education, research, and spiritual purpose?',
];

export const FrameworkExplorerSection: React.FC<FrameworkExplorerSectionProps> = ({
  frameworkData,
  onClassifyQuestion,
}) => {
  const [questionInput, setQuestionInput] = useState<string>(
    frameworkData?.questionText || SAMPLE_QUESTIONS[0],
  );
  const [selectedCategoryCode, setSelectedCategoryCode] = useState<string | null>(null);

  if (!frameworkData) return null;

  const activeCategoryCode = selectedCategoryCode || frameworkData.primaryCategory;
  const activeFramework: AnalysisFrameworkDefinition =
    frameworkData.allFrameworks.find((f) => f.categoryCode === activeCategoryCode) ||
    frameworkData.activeFramework;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!questionInput.trim()) return;
    setSelectedCategoryCode(null);
    onClassifyQuestion(questionInput.trim());
  };

  const handleSampleClick = (q: string) => {
    setQuestionInput(q);
    setSelectedCategoryCode(null);
    onClassifyQuestion(q);
  };

  return (
    <section className="bg-slate-900/70 border border-slate-800/80 rounded-2xl p-5 sm:p-6 shadow-xl space-y-6">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-5 border-b border-slate-800/80">
        <div>
          <div className="flex items-center gap-2.5">
            <Compass className="w-5 h-5 text-amber-400" />
            <h2 className="text-lg font-semibold text-white tracking-tight">
              Question Classifier &amp; Classical Vedic Analysis Frameworks
            </h2>
            <span className="px-2.5 py-0.5 text-xs font-medium rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/30">
              {frameworkData.allFrameworks.length} BPHS Consultation Blueprints
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Deterministically routes any consultation question to the exact BPHS / Phaladeepika
            Houses, Shodashavarga Divisional Charts, Karakas, Special Lagnas, and Checklist Rules.
          </p>
        </div>

        <div className="flex items-center gap-2 text-xs">
          <span className="px-3 py-1.5 rounded-xl bg-emerald-950/50 border border-emerald-700/50 text-emerald-300 font-semibold">
            Confidence: {(frameworkData.confidenceScore * 100).toFixed(0)}%
          </span>
          <span className="px-3 py-1.5 rounded-xl bg-amber-950/50 border border-amber-700/50 text-amber-300 font-semibold">
            Routed: {frameworkData.primaryCategory.replace(/_/g, ' ')}
          </span>
        </div>
      </div>

      {/* Interactive Question Classifier Bar */}
      <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-4 space-y-3">
        <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-2.5">
          <div className="flex-1 flex items-center gap-2.5 bg-slate-900 border border-slate-700/80 rounded-xl px-3.5 py-2">
            <Search className="w-4 h-4 text-amber-400 shrink-0" />
            <input
              type="text"
              value={questionInput}
              onChange={(e) => setQuestionInput(e.target.value)}
              placeholder="Ask any astrological question to classify its BPHS analysis framework..."
              aria-label="Consultation Question Classifier Input"
              className="w-full bg-transparent text-xs text-white placeholder-slate-500 focus:outline-none"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2 rounded-xl bg-amber-500 text-slate-950 hover:bg-amber-400 font-bold text-xs transition shrink-0"
          >
            Classify &amp; Load Framework
          </button>
        </form>

        {/* Sample Question Quick-Pills */}
        <div className="flex flex-wrap items-center gap-1.5">
          <span className="text-[11px] text-slate-400 mr-1">Try sample questions:</span>
          {SAMPLE_QUESTIONS.map((sq, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => handleSampleClick(sq)}
              className="px-2.5 py-1 rounded-lg bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-800 text-[11px] transition"
            >
              {sq}
            </button>
          ))}
        </div>
      </div>

      {/* 9-Domain Framework Selector Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {frameworkData.allFrameworks.map((fw) => {
          const isSelected = fw.categoryCode === activeFramework.categoryCode;
          const isPrimaryClassified = fw.categoryCode === frameworkData.primaryCategory;
          return (
            <button
              key={fw.categoryCode}
              type="button"
              onClick={() => setSelectedCategoryCode(fw.categoryCode)}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold border transition shrink-0 flex items-center gap-1.5 ${
                isSelected
                  ? 'bg-amber-500/20 text-amber-300 border-amber-500/60 shadow-sm'
                  : 'bg-slate-950/70 text-slate-400 border-slate-800 hover:text-slate-200'
              }`}
            >
              <span>{fw.title.split(',')[0]}</span>
              {isPrimaryClassified && (
                <span className="w-2 h-2 rounded-full bg-emerald-400" title="Classified Primary Domain" />
              )}
            </button>
          );
        })}
      </div>

      {/* Active Framework Blueprint Split View */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 items-start">
        {/* Left 5 Cols: Multi-Chart Astrological Factors (Houses, Vargas, Karakas, Yogas) */}
        <div className="lg:col-span-5 bg-slate-950/80 border border-slate-800 rounded-2xl p-5 space-y-4">
          <div className="border-b border-slate-800 pb-3">
            <span className="text-[10px] font-mono uppercase tracking-wider text-amber-400">
              {activeFramework.categoryCode}
            </span>
            <h3 className="text-base font-bold text-white mt-0.5">{activeFramework.title}</h3>
            <p className="text-xs text-slate-400 italic">{activeFramework.sanskritTitle}</p>
            <p className="text-xs text-slate-300 mt-2 leading-relaxed">
              {activeFramework.description}
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3 text-xs">
            <div className="bg-slate-900/70 border border-slate-800 rounded-xl p-3">
              <div className="text-[10px] uppercase tracking-wider text-amber-400 font-semibold mb-1.5">
                Primary Bhavas (Houses)
              </div>
              <div className="flex flex-wrap gap-1.5">
                {activeFramework.primaryHouses.map((h) => (
                  <span
                    key={h}
                    className="px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-500/40 font-mono font-bold"
                  >
                    House {h}
                  </span>
                ))}
              </div>
            </div>

            <div className="bg-slate-900/70 border border-slate-800 rounded-xl p-3">
              <div className="text-[10px] uppercase tracking-wider text-slate-400 font-semibold mb-1.5">
                Supporting Bhavas
              </div>
              <div className="flex flex-wrap gap-1.5">
                {activeFramework.secondaryHouses.map((h) => (
                  <span
                    key={h}
                    className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700 font-mono"
                  >
                    H{h}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* Required Divisional Vargas & Naisargika Karakas */}
          <div className="grid grid-cols-2 gap-3 text-xs">
            <div className="bg-slate-900/70 border border-slate-800 rounded-xl p-3">
              <div className="text-[10px] uppercase tracking-wider text-purple-300 font-semibold mb-1.5 flex items-center gap-1">
                <Layers className="w-3 h-3" />
                <span>Required Vargas</span>
              </div>
              <div className="flex flex-wrap gap-1.5">
                {activeFramework.requiredVargas.map((v) => (
                  <span
                    key={v}
                    className="px-2 py-0.5 rounded bg-purple-950/60 text-purple-200 border border-purple-700/50 font-mono font-semibold"
                  >
                    {v}
                  </span>
                ))}
              </div>
            </div>

            <div className="bg-slate-900/70 border border-slate-800 rounded-xl p-3">
              <div className="text-[10px] uppercase tracking-wider text-emerald-300 font-semibold mb-1.5 flex items-center gap-1">
                <Sparkles className="w-3 h-3" />
                <span>Naisargika Karakas</span>
              </div>
              <div className="flex flex-wrap gap-1.5">
                {activeFramework.naisargikaKarakas.map((k) => (
                  <span
                    key={k}
                    className="px-2 py-0.5 rounded bg-slate-950 border border-slate-700 font-semibold"
                    style={{ color: PLANET_COLORS[k] || '#fbbf24' }}
                  >
                    {k}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* Special Lagnas & Key Yogas */}
          <div className="bg-slate-900/70 border border-slate-800 rounded-xl p-3 text-xs space-y-2">
            <div>
              <span className="text-[10px] uppercase tracking-wider text-slate-400 font-semibold block mb-1">
                Special Lagnas &amp; Classical Yogas Inspected
              </span>
              <div className="flex flex-wrap gap-1.5">
                {activeFramework.specialLagnas.map((sl) => (
                  <span
                    key={sl}
                    className="px-2 py-0.5 rounded bg-sky-950/60 text-sky-300 border border-sky-700/50 text-[11px]"
                  >
                    {sl}
                  </span>
                ))}
                {activeFramework.keyYogasToCheck.map((yg) => (
                  <span
                    key={yg}
                    className="px-2 py-0.5 rounded bg-slate-800 text-amber-200 border border-slate-700 text-[10px] font-mono"
                  >
                    {yg}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Right 7 Cols: Deterministic Shastra Evaluation Checklist Rules */}
        <div className="lg:col-span-7 bg-slate-950/80 border border-slate-800 rounded-2xl p-5 space-y-3.5">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <div className="flex items-center gap-2">
              <CheckSquare className="w-4 h-4 text-amber-400" />
              <h3 className="text-sm font-bold text-white">
                Classical Evaluation Checklist Rules (Executed by Evidence &amp; Reasoning Engines)
              </h3>
            </div>
            <span className="text-[11px] font-mono text-slate-400">
              {activeFramework.checklistRules.length} Weighted Rules
            </span>
          </div>

          <div className="space-y-2.5">
            {activeFramework.checklistRules.map((rule) => (
              <div
                key={rule.ruleCode}
                className="p-3.5 rounded-xl bg-slate-900/80 border border-slate-800/90 space-y-1.5"
              >
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-500/40 font-mono text-[10px] font-bold">
                      {rule.ruleCode}
                    </span>
                    <span className="text-xs font-semibold text-slate-200">
                      {rule.factorCategory.replace(/_/g, ' ')}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 text-[11px]">
                    <span className="inline-flex items-center gap-1 text-slate-400">
                      <BookOpen className="w-3 h-3 text-amber-400" />
                      {rule.classicalReference}
                    </span>
                    <span className="px-2 py-0.5 rounded bg-slate-950 text-emerald-300 border border-slate-800 font-mono">
                      Weight: {(rule.weight * 100).toFixed(0)}%
                    </span>
                  </div>
                </div>
                <p className="text-xs text-slate-300 leading-relaxed">{rule.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};
