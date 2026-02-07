---
title: Fission System Index
---

# Fission System (Phoenix) — Index

This page is the “front door” for Phoenix’s fission system: what the parts are, what they do, and which knobs
to turn when you want to balance it.

It’s written to be a **map**, not a wall of text. Each section tells you *what to change* and *where to look next*.

---

## Mental model

A fission multiblock in Phoenix is basically:

1. **Fuel rods** determine the main “reactor is running” loop (fuel type, fuel pacing, base heat, spectrum bias, etc.)
2. **Moderators** modify scaling: parallels, EU boost, fuel discount, heat multipliers (depending on your interfaces)
3. **Coolers** provide *cooling capacity* (HU/t) and may require *coolant fluid conversion*
4. **Blanket rods** (breeders) are a side-process that consumes an input and produces weighted outputs while the reactor runs
5. **Configs** define the global behavior: heat curve, parallels, coolant rules, meltdown, and explosion strength

The base reactor code runs a **per-tick simulation**:
- If it should run (formed + rods + fuel + maybe coolant)
- It produces heat
- It consumes fuel
- It optionally consumes coolant
- It removes heat using cooling capacity (if allowed)
- It generates power (EU/t)
- It advances or clears a meltdown timer

---

## What kind of dev are you?

### KubeJS / content author (adding parts)
You mainly care about adding new block types:
- Fuel rods
- Moderators
- Coolers
- Blanket rods (breeders)

And occasionally tuning `PhoenixConfigs.fission` values.

➡️ Suggested workflow:
1. Add a new rod/cooler/moderator/blanket block type
2. Test a simple reactor build in-world
3. Adjust config values for pacing and risk/reward

### Java contributor (changing behavior)
You care about:
- the base reactor simulation logic
- breeder-specific transmutation rules
- I/O behavior (dummy recipes)
- tier/primary selection logic
- meltdown/explosion mechanics

---

## Key pages (docs you already have)

- `FissionWorkableElectricMultiblockMachine.md`  
  The base “reactor brain” tick loop: heat, coolant, fuel, EU, meltdown.

- `DynamicFissionReactorMachine.md`  
  A safer fuel-consumption variant (highest-tier rod selection + no remainder loss).

- `BreederWorkableElectricMultiblockMachine.md`  
  Adds blanket transmutation cycles while the reactor runs.

- **Blanket Rods** (your existing wiki page)  
  How blanket outputs are weighted and how spectrum bias affects them.

---

## Building blocks: what each block type is for

### Fuel Rods (driver)
Fuel rods are the “engine” of the reactor.

Typically defines:
- fuel input key (item id)
- durationTicks / amountPerCycle (how fuel is consumed)
- base heat production
- spectrum bias (if used by breeders)
- spent/depleted output item (optional)

**No fuel rods = no reactor.**

---

### Moderators (scaling + efficiency)
Moderators are multiplier blocks. They usually:
- increase parallels (through `getParallelBonus()`)
- increase EU output (via `getEUBoost()`)
- reduce fuel consumption (via `getFuelDiscount()`)
- may affect heat multiplier or spectrum bias (depending on your interface usage)

Think of them as: **more output, better efficiency, more scaling**.

---

### Coolers (cooling capacity + coolant conversion)
Coolers contribute cooling capacity (`HU/t`) and may require consuming coolant.

Important config switch:
- If `coolingRequiresCoolant = true`, then cooling is only applied when coolant is available/consumed.
- If `coolantUsageAdditive = false`, only the **primary cooler** determines coolant requirement.
- If `coolantUsageAdditive = true`, **all coolers add up** their coolant requirements.

---

### Blanket Rods (breeding side-process)
Blankets are only meaningful in breeder machines.

They:
- consume a blanket input (item/fluid key)
- produce weighted outputs (items/fluids)
- can be processed primary-only or additive across all blankets (`blanketUsageAdditive`)

Blanket outputs are “roulette weighted” and can be shifted by spectrum bias:
- adjustedWeight = baseWeight * exp(bias * instability * k)

---

### Nuke (separate feature)
The nuke block is not “reactor meltdown” — it’s its own world effect with its own config section.

---

## Config reference: PhoenixConfigs.fission

Below is the *practical* interpretation of each config group.

### Nuke controls

```java
public boolean nukeEnabled = true;
public int nukeCubeRadius = 16;
public int nukeCubeRadiusCap = 48;
public int nukeFuseTicks = 120;
public int nukeBatchPerTick = 8000;
public boolean nukeSkipBlockEntities = true;
public boolean nukeSkipUnloadedChunks = true;
public boolean nukeReplaceWithFire = false;
```

Use these to tune:
- how large the wipe area is (`nukeCubeRadius`, capped by `nukeCubeRadiusCap`)
- how quickly it processes blocks (`nukeBatchPerTick`)
- whether it avoids machines/chests (`nukeSkipBlockEntities`)
- whether it avoids chunk forcing (`nukeSkipUnloadedChunks`)
- if it leaves fire behind (`nukeReplaceWithFire`)

---

### Heat model

```java
public double baseHeatPerTick = 0.0;
public double burnBonusMaxPercent = 60.0;
public double burnBonusRampSeconds = 1200.0;
public double maxSafeHeat = 10000.0;
public double minHeat = 0.0;
public double maxHeatClamp = 250000.0;
public boolean passiveCooling = true;
public double idleHeatLoss = 1.0;
```

What to tune:
- **How fast heat rises**: `baseHeatPerTick` + rod heat + moderator effects
- **How punishing overheating is**: `maxSafeHeat` (meltdown starts above this)
- **How long “good running” is rewarded**: burn bonus settings  
  (affects power + breeder output)
- **How much heat decays while idle**: `passiveCooling` + `idleHeatLoss`
- **Safety clamp**: `maxHeatClamp` prevents numeric runaway but still triggers meltdown

---

### Parallels model

```java
public int parallelsPerFuelRod = 1;
public double heatPerParallel = 2000.0;
public int maxParallels = 64;
```

Parallels are your “scaling multiplier”:
- more rods → more parallels
- more heat can optionally increase parallels (`heatPerParallel`)
- moderators can also add parallels (from block types)
- `maxParallels` hard caps it

What to tune:
- **build scale**: `parallelsPerFuelRod`
- **high-heat reward**: `heatPerParallel` (smaller = more bonus parallels from heat)

---

### Power model

```java
public double euPerHeatUnit = 0.5;
public long maxGeneratedEUt = 0;
public long minGeneratedEUt = 8;
public double powerCurveExponent = 2.0;
public double powerStartFraction = 0.0;
```

How it “feels”:
- `euPerHeatUnit` is the baseline.
- `powerCurveExponent` controls “risk/reward”: higher = more reward near max safe heat.
- `powerStartFraction` lets you require a minimum heat before power generation begins.
- `minGeneratedEUt` avoids “0 EU/t while running”.
- `maxGeneratedEUt` caps output (0 or less = no cap).

---

### Fuel consumption

```java
public boolean fuelUsageScalesWithParallels = true;
public boolean fuelUsageScalesWithRodCount = true;
public boolean blanketUsageAdditive = true;
public int maxFuelDiscountPercent = 90;
public int maxEUBoostPercent = 500;
```

Interpretation:
- fuel per cycle can scale with rod count and/or parallels
- moderators can reduce fuel usage, but the discount is clamped
- moderators can boost EU, but the boost is clamped
- blanket additivity decides whether *every blanket* runs each cycle, or only the primary

---

### Coolant model

```java
public boolean coolingRequiresCoolant = true;
public boolean coolantUsageAdditive = false;
```

This is the “coolant realism” switch:
- If cooling requires coolant, missing coolant means **cooling doesn’t remove heat**.
- Additive coolant usage decides if you pay coolant for:
  - only the primary cooler (false), or
  - the sum of all coolers (true)

---

### Tier restrictions

```java
public boolean restrictFuelRodTier = true;
public boolean restrictCoolerTier = true;
public int requiredFuelRodTier = -1;
public int requiredCoolerTier = -1;
```

Use these to enforce progression rules:
- restrict mixing tiers
- optionally require an exact tier
- set `required*Tier = -1` to disable the exact requirement

---

### Meltdown behavior

```java
public double baseGraceSeconds = 60.0;
public double minGraceSeconds = 10.0;
public double excessHeatSeverity = 1.0;
public boolean clearTimerWhenSafe = true;
```

Meltdown is **timer-based**:
- when you exceed `maxSafeHeat`, a grace timer begins
- the hotter you are, the faster the grace shrinks (severity curve)
- falling back under safe heat can clear the timer (if enabled)

---

### Explosion behavior

```java
public boolean destructiveExplosion = true;
public double explosionPowerPerFuelRod = 1.5;
public float baseExplosionPower = 10.0f;
public int maxDestructiveRadius = 6;
```

- `destructiveExplosion = true`: “wipe blocks to air/fire” style blast
- explosion power scales with rods (and in code, also cooler/mod tiers)
- destructive wipe radius is capped

---

## Balancing checklist (practical)

When you add new parts or tune configs, test with a few “standard builds”:

1. **Bare minimum reactor**  
   - 1 fuel rod, no coolers/mods  
   - Check: does it run? how much EU/t? how fast does heat climb?

2. **Stable cooled reactor**  
   - add a realistic cooler set + coolant input  
   - Check: can it hold heat below safe? is coolant cost reasonable?

3. **Greedy high-output reactor**  
   - add moderators + more rods  
   - Check: does it produce big EU/t but flirt with meltdown?

4. **Breeder reactor (if applicable)**  
   - add blankets  
   - Check: do outputs scale with parallels/burn bonus as intended?

Things that usually need tuning first:
- `maxSafeHeat` (sets the “safe operating window”)
- `euPerHeatUnit` (baseline output)
- `powerCurveExponent` (how rewarding risk is)
- fuel scaling toggles (do you want large builds to eat lots more fuel?)
- `coolingRequiresCoolant` + `coolantUsageAdditive` (how punishing coolant logistics are)

---

## Where to go next

If you’re adding content:
- Start with your block type docs (fuel rods / coolers / moderators / blankets)
- Then tune configs after testing

If you’re changing mechanics:
- Start in `FissionWorkableElectricMultiblockMachine`
- Then see whether your feature belongs in `DynamicFissionReactorMachine`
- For breeding, work in `BreederWorkableElectricMultiblockMachine`

---
