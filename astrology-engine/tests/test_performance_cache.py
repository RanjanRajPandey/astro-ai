"""
Unit tests for Phase 24 Astronomical Calculation Memoization and LRU Caching.
"""

import time
from astronomy.ephemeris import (
    _calculate_raw_sidereal_bodies_cached,
    _calculate_sidereal_ascendant_cached,
    calculate_raw_sidereal_bodies,
    calculate_sidereal_ascendant,
    get_ayanamsha_value,
)


def test_ephemeris_caching_and_hit_ratio():
    # Clear caches to ensure clean baseline
    get_ayanamsha_value.cache_clear()
    _calculate_sidereal_ascendant_cached.cache_clear()
    _calculate_raw_sidereal_bodies_cached.cache_clear()

    jd = 2453239.920138889  # 2004-08-22 18:05:00 UTC

    # 1. First call (cache miss)
    t0 = time.perf_counter()
    b1 = calculate_raw_sidereal_bodies(jd, "LAHIRI", "MEAN_NODE")
    d1 = time.perf_counter() - t0

    # 2. Second call (cache hit)
    t1 = time.perf_counter()
    b2 = calculate_raw_sidereal_bodies(jd, "LAHIRI", "MEAN_NODE")
    d2 = time.perf_counter() - t1

    assert b1.keys() == b2.keys()
    assert abs(b1["Sun"].longitude - b2["Sun"].longitude) < 1e-9
    assert abs(b1["Saturn"].longitude - b2["Saturn"].longitude) < 1e-9

    cache_info = _calculate_raw_sidereal_bodies_cached.cache_info()
    assert cache_info.hits >= 1
    assert cache_info.misses >= 1

    # 3. Ascendant cache hit verification
    asc1, cusps1 = calculate_sidereal_ascendant(jd, 26.7402, 83.8886, "LAHIRI")
    asc2, cusps2 = calculate_sidereal_ascendant(jd, 26.7402, 83.8886, "LAHIRI")
    assert abs(asc1 - asc2) < 1e-9
    assert cusps1 == cusps2
    assert _calculate_sidereal_ascendant_cached.cache_info().hits >= 1

    # 4. Ayanamsha cache hit verification
    ayan1 = get_ayanamsha_value(jd, "LAHIRI")
    ayan2 = get_ayanamsha_value(jd, "LAHIRI")
    assert abs(ayan1 - ayan2) < 1e-9
    assert get_ayanamsha_value.cache_info().hits >= 1
