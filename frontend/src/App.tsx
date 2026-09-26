import { Sparkles, Compass, ShieldCheck } from 'lucide-react';

export function App() {
  return (
    <div className="min-h-screen bg-cosmic-950 text-slate-100 flex flex-col">
      <header className="border-b border-cosmic-700/60 bg-cosmic-900/80 backdrop-blur px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Compass className="w-7 h-7 text-cosmic-gold" />
          <div>
            <h1 className="text-lg font-semibold tracking-wide text-white">
              ASTRO-AI
            </h1>
            <p className="text-xs text-slate-400">
              Deterministic Vedic Astrology &amp; Traceable AI Reasoning Platform
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2 text-xs px-3 py-1.5 rounded-full bg-cosmic-800 border border-cosmic-gold/30 text-cosmic-gold">
          <ShieldCheck className="w-4 h-4" />
          <span>Spec: 1.0.0-BPHS-LAHIRI</span>
        </div>
      </header>

      <main className="flex-1 max-w-5xl mx-auto w-full px-6 py-12 flex flex-col justify-center items-center text-center">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-cosmic-800/90 border border-cosmic-700 text-cosmic-gold text-xs mb-6">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Zero-Hallucination Deterministic Calculation Architecture</span>
        </div>
        <h2 className="text-3xl sm:text-5xl font-bold tracking-tight text-white mb-4">
          Classical Jyotish Precision Meets Traceable AI Explainability
        </h2>
        <p className="text-slate-400 max-w-2xl text-base leading-relaxed">
          Powered by Swiss Ephemeris sidereal calculations, 16 Shodashavarga divisional charts,
          5-level Vimshottari Dashas, Shadbala, Bhava Bala, and a transparent Evidence &amp; Reasoning Engine.
        </p>
      </main>
    </div>
  );
}

export default App;
