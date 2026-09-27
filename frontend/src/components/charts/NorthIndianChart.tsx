import type { GrahaName, HouseDetail, PlanetPosition } from '../../types/astrology';
import { PLANET_ABBR, PLANET_COLORS, getSignIndexForHouse } from '../../utils/chartMath';

interface NorthIndianChartProps {
  ascendantSignIndex: number;
  planets: PlanetPosition[];
  houses: HouseDetail[];
  selectedHouse: number | null;
  selectedPlanet: GrahaName | null;
  onSelectHouse: (houseNumber: number) => void;
  onSelectPlanet: (planet: GrahaName) => void;
}

interface HouseGeometry {
  houseNumber: number;
  points: string;
  signCoord: { x: number; y: number };
  centerCoord: { x: number; y: number };
}

/**
 * Exact geometric polygons for the 12 houses of a traditional North Indian Diamond Kundli
 * inside a 500x500 coordinate system (Counter-clockwise from Top-Center House 1).
 */
const NORTH_INDIAN_GEOMETRY: HouseGeometry[] = [
  {
    houseNumber: 1,
    points: '250,10 130,130 250,250 370,130',
    signCoord: { x: 250, y: 222 },
    centerCoord: { x: 250, y: 120 },
  },
  {
    houseNumber: 2,
    points: '10,10 250,10 130,130',
    signCoord: { x: 130, y: 105 },
    centerCoord: { x: 130, y: 50 },
  },
  {
    houseNumber: 3,
    points: '10,10 130,130 10,250',
    signCoord: { x: 103, y: 134 },
    centerCoord: { x: 52, y: 130 },
  },
  {
    houseNumber: 4,
    points: '130,130 10,250 130,370 250,250',
    signCoord: { x: 222, y: 254 },
    centerCoord: { x: 125, y: 250 },
  },
  {
    houseNumber: 5,
    points: '10,250 130,370 10,490',
    signCoord: { x: 103, y: 374 },
    centerCoord: { x: 52, y: 370 },
  },
  {
    houseNumber: 6,
    points: '10,490 130,370 250,490',
    signCoord: { x: 130, y: 400 },
    centerCoord: { x: 130, y: 455 },
  },
  {
    houseNumber: 7,
    points: '250,250 130,370 250,490 370,370',
    signCoord: { x: 250, y: 285 },
    centerCoord: { x: 250, y: 385 },
  },
  {
    houseNumber: 8,
    points: '370,370 250,490 490,490',
    signCoord: { x: 370, y: 400 },
    centerCoord: { x: 370, y: 455 },
  },
  {
    houseNumber: 9,
    points: '490,250 370,370 490,490',
    signCoord: { x: 397, y: 374 },
    centerCoord: { x: 448, y: 370 },
  },
  {
    houseNumber: 10,
    points: '250,250 370,370 490,250 370,130',
    signCoord: { x: 278, y: 254 },
    centerCoord: { x: 375, y: 250 },
  },
  {
    houseNumber: 11,
    points: '490,10 370,130 490,250',
    signCoord: { x: 397, y: 134 },
    centerCoord: { x: 448, y: 130 },
  },
  {
    houseNumber: 12,
    points: '250,10 370,130 490,10',
    signCoord: { x: 370, y: 105 },
    centerCoord: { x: 370, y: 50 },
  },
];

export function NorthIndianChart({
  ascendantSignIndex,
  planets,
  selectedHouse,
  selectedPlanet,
  onSelectHouse,
  onSelectPlanet,
}: NorthIndianChartProps) {
  return (
    <div className="w-full max-w-[520px] mx-auto select-none">
      <svg
        viewBox="0 0 500 500"
        className="w-full h-auto rounded-xl bg-cosmic-900/90 border border-cosmic-gold/30 shadow-2xl"
        role="img"
        aria-label="North Indian Vedic Kundli Chart"
      >
        <defs>
          <linearGradient id="goldStroke" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#D4AF37" stopOpacity="0.85" />
            <stop offset="100%" stopColor="#F59E0B" stopOpacity="0.55" />
          </linearGradient>
        </defs>

        {NORTH_INDIAN_GEOMETRY.map((geo) => {
          const signIdx = getSignIndexForHouse(ascendantSignIndex, geo.houseNumber);
          const occupants = planets.filter((p) => p.house === geo.houseNumber);
          const isSelected = selectedHouse === geo.houseNumber;

          return (
            <g key={geo.houseNumber}>
              <polygon
                points={geo.points}
                onClick={() => onSelectHouse(geo.houseNumber)}
                className="cursor-pointer transition-colors duration-200"
                fill={
                  isSelected
                    ? 'rgba(212, 175, 55, 0.18)'
                    : geo.houseNumber === 1
                    ? 'rgba(212, 175, 55, 0.05)'
                    : 'rgba(13, 17, 36, 0.65)'
                }
                stroke="url(#goldStroke)"
                strokeWidth={isSelected ? 2.4 : 1.4}
              />

              {/* Rashi Sign Number (1..12) */}
              <text
                x={geo.signCoord.x}
                y={geo.signCoord.y}
                textAnchor="middle"
                fill="#D4AF37"
                fontSize="12"
                fontWeight="700"
                className="pointer-events-none opacity-90"
              >
                {signIdx}
              </text>

              {/* Lagna Label in House 1 */}
              {geo.houseNumber === 1 && (
                <text
                  x={250}
                  y={42}
                  textAnchor="middle"
                  fill="#D4AF37"
                  fontSize="10"
                  fontWeight="600"
                  letterSpacing="1.5"
                  className="pointer-events-none opacity-75"
                >
                  LAGNA
                </text>
              )}

              {/* Planet Badges in this House */}
              {occupants.map((planetObj, idx) => {
                const count = occupants.length;
                const cols = count > 2 ? 2 : 1;
                const col = idx % cols;
                const row = Math.floor(idx / cols);
                const totalRows = Math.ceil(count / cols);
                const offsetX = cols === 2 ? (col === 0 ? -24 : 24) : 0;
                const offsetY = (row - (totalRows - 1) / 2) * 20;
                const px = geo.centerCoord.x + offsetX;
                const py = geo.centerCoord.y + offsetY;
                const isPlanetSelected = selectedPlanet === planetObj.planet;

                const abbr = PLANET_ABBR[planetObj.planet];
                const degInt = Math.floor(planetObj.degreeInSign);
                const statusSuffix = `${planetObj.retrograde && planetObj.planet !== 'Rahu' && planetObj.planet !== 'Ketu' ? 'R' : ''}${planetObj.combust ? 'C' : ''}${planetObj.exalted ? '↑' : planetObj.debilitated ? '↓' : ''}`;

                return (
                  <g
                    key={planetObj.planet}
                    transform={`translate(${px}, ${py})`}
                    onClick={(e) => {
                      e.stopPropagation();
                      onSelectPlanet(planetObj.planet);
                    }}
                    className="cursor-pointer group"
                  >
                    <rect
                      x={-22}
                      y={-10}
                      width={44}
                      height={19}
                      rx={5}
                      fill={isPlanetSelected ? 'rgba(212, 175, 55, 0.32)' : 'rgba(7, 9, 19, 0.88)'}
                      stroke={isPlanetSelected ? '#FBBF24' : PLANET_COLORS[planetObj.planet]}
                      strokeWidth={isPlanetSelected ? 1.8 : 1}
                    />
                    <text
                      x={0}
                      y={3.5}
                      textAnchor="middle"
                      fill={PLANET_COLORS[planetObj.planet]}
                      fontSize="10.5"
                      fontWeight="700"
                    >
                      {abbr}
                      {statusSuffix ? `(${statusSuffix})` : ''} {degInt}°
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
