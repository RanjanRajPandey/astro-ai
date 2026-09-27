import type { GrahaName, PlanetPosition } from '../../types/astrology';
import {
  PLANET_ABBR,
  PLANET_COLORS,
  SIGN_NAMES_BY_INDEX,
  getHouseNumberForSign,
} from '../../utils/chartMath';

interface SouthIndianChartProps {
  ascendantSignIndex: number;
  vargaTitle: string;
  planets: PlanetPosition[];
  selectedHouse: number | null;
  selectedPlanet: GrahaName | null;
  onSelectHouse: (houseNumber: number) => void;
  onSelectPlanet: (planet: GrahaName) => void;
}

/**
 * Fixed clockwise 4x4 grid coordinates for signs 1 (Aries) through 12 (Pisces)
 * in the traditional South Indian Kundli layout:
 * Row 0: Pisces(12), Aries(1), Taurus(2), Gemini(3)
 * Row 1: Aquarius(11), [Center], [Center], Cancer(4)
 * Row 2: Capricorn(10), [Center], [Center], Leo(5)
 * Row 3: Sagittarius(9), Scorpio(8), Libra(7), Virgo(6)
 */
const SOUTH_SIGN_CELLS: Array<{ signIndex: number; col: number; row: number }> = [
  { signIndex: 12, col: 0, row: 0 },
  { signIndex: 1, col: 1, row: 0 },
  { signIndex: 2, col: 2, row: 0 },
  { signIndex: 3, col: 3, row: 0 },
  { signIndex: 4, col: 3, row: 1 },
  { signIndex: 5, col: 3, row: 2 },
  { signIndex: 6, col: 3, row: 3 },
  { signIndex: 7, col: 2, row: 3 },
  { signIndex: 8, col: 1, row: 3 },
  { signIndex: 9, col: 0, row: 3 },
  { signIndex: 10, col: 0, row: 2 },
  { signIndex: 11, col: 0, row: 1 },
];

export function SouthIndianChart({
  ascendantSignIndex,
  vargaTitle,
  planets,
  selectedHouse,
  selectedPlanet,
  onSelectHouse,
  onSelectPlanet,
}: SouthIndianChartProps) {
  const cellSize = 120;
  const pad = 10;

  return (
    <div className="w-full max-w-[520px] mx-auto select-none">
      <svg
        viewBox="0 0 500 500"
        className="w-full h-auto rounded-xl bg-cosmic-900/90 border border-cosmic-gold/30 shadow-2xl"
        role="img"
        aria-label="South Indian Vedic Kundli Chart"
      >
        {/* Center 2x2 Chamber */}
        <rect
          x={pad + cellSize}
          y={pad + cellSize}
          width={cellSize * 2}
          height={cellSize * 2}
          fill="rgba(7, 9, 19, 0.75)"
          stroke="#D4AF37"
          strokeWidth={1.5}
        />
        <text
          x={250}
          y={242}
          textAnchor="middle"
          fill="#D4AF37"
          fontSize="15"
          fontWeight="700"
          letterSpacing="1"
        >
          {vargaTitle}
        </text>
        <text
          x={250}
          y={264}
          textAnchor="middle"
          fill="#94A3B8"
          fontSize="11"
        >
          South Indian Rashi Layout
        </text>

        {SOUTH_SIGN_CELLS.map((cell) => {
          const x = pad + cell.col * cellSize;
          const y = pad + cell.row * cellSize;
          const houseNum = getHouseNumberForSign(ascendantSignIndex, cell.signIndex);
          const isLagna = cell.signIndex === ascendantSignIndex;
          const isSelected = selectedHouse === houseNum;
          const signName = SIGN_NAMES_BY_INDEX[cell.signIndex];
          const occupants = planets.filter((p) => p.signIndex === cell.signIndex);

          return (
            <g key={cell.signIndex}>
              <rect
                x={x}
                y={y}
                width={cellSize}
                height={cellSize}
                onClick={() => onSelectHouse(houseNum)}
                className="cursor-pointer transition-colors duration-200"
                fill={
                  isSelected
                    ? 'rgba(212, 175, 55, 0.18)'
                    : isLagna
                    ? 'rgba(212, 175, 55, 0.07)'
                    : 'rgba(13, 17, 36, 0.65)'
                }
                stroke="#D4AF37"
                strokeWidth={isSelected ? 2.4 : 1.3}
              />

              {/* Traditional double diagonal mark in top-left corner for Ascendant (Lagna) */}
              {isLagna && (
                <>
                  <line
                    x1={x}
                    y1={y + 26}
                    x2={x + 26}
                    y2={y}
                    stroke="#FBBF24"
                    strokeWidth={2}
                  />
                  <line
                    x1={x}
                    y1={y + 32}
                    x2={x + 32}
                    y2={y}
                    stroke="#FBBF24"
                    strokeWidth={1.2}
                  />
                </>
              )}

              {/* Sign Label & House Number */}
              <text
                x={x + 8}
                y={y + 16}
                fill="#94A3B8"
                fontSize="9.5"
                fontWeight="600"
                className="pointer-events-none"
              >
                {signName.slice(0, 3).toUpperCase()} ({cell.signIndex})
              </text>
              <text
                x={x + cellSize - 8}
                y={y + 16}
                textAnchor="end"
                fill="#D4AF37"
                fontSize="10"
                fontWeight="700"
                className="pointer-events-none"
              >
                {isLagna ? 'ASC / H1' : `H${houseNum}`}
              </text>

              {/* Occupant Planet Badges */}
              {occupants.map((planetObj, idx) => {
                const col = idx % 2;
                const row = Math.floor(idx / 2);
                const px = x + 34 + col * 52;
                const py = y + 42 + row * 24;
                const isPlanetSelected = selectedPlanet === planetObj.planet;
                const abbr = PLANET_ABBR[planetObj.planet];
                const degInt = Math.floor(planetObj.degreeInSign);

                return (
                  <g
                    key={planetObj.planet}
                    transform={`translate(${px}, ${py})`}
                    onClick={(e) => {
                      e.stopPropagation();
                      onSelectPlanet(planetObj.planet);
                    }}
                    className="cursor-pointer"
                  >
                    <rect
                      x={-23}
                      y={-10}
                      width={46}
                      height={19}
                      rx={4}
                      fill={isPlanetSelected ? 'rgba(212, 175, 55, 0.32)' : 'rgba(7, 9, 19, 0.9)'}
                      stroke={isPlanetSelected ? '#FBBF24' : PLANET_COLORS[planetObj.planet]}
                      strokeWidth={isPlanetSelected ? 1.8 : 1}
                    />
                    <text
                      x={0}
                      y={3.5}
                      textAnchor="middle"
                      fill={PLANET_COLORS[planetObj.planet]}
                      fontSize="10"
                      fontWeight="700"
                    >
                      {abbr} {degInt}°
                    </text>
                  </g>
                );
              })}
            </g>
          );
        })}
      </svg>
    </div>
  );
}
