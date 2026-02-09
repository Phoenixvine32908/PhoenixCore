---
title: Fission System Index
---

# So, you seek knowledge on the internals of fission? I shall oblige.

## The Basics

## Gameplay loop
    
- Build reactor multiblock
        - Build in coolers, moderators, and fuel rods. Breeder rods if in the breeder reactor.
    - Provide coolant
    - Provide fuel
    - Starts up runtime if these are met, 
        - consumes fuel by amount/speed of use defined in block
        - outputs depleted fuel also defined in block
        - consumes coolant by amount/speed of use defined in block
        - outputs hot coolant also defined in block
    - Gives eu based on heat produced per tick
    - Heat is defined by moderators and fuel rods
    - Max heat is a config value, if exceeded starts meltdown timer
    - If multi is broken while in meltdown, or timer hits 0, boom!
    - Breeder reactors use breeder rods for breeding, check out the corresponding page for further explanation
  

## The importance of the component blocks

- Coolers
- Blanket Rods
- Fuel Rods
- Moderators

## The reactor machine class types

- FissionWorkableElectricMultiblockMachine
- DynamicFissionReactorMachine
- BreederWorkableElectricMultiblockMachine

## Kubejs dev path

## Java dev path

## Fission configs

## Meltdown configs

## Explosion configs

## Nuke

## Important wiki pages

## Summary

- Pretty much, it's just: Fuels/Coolants are checked as a prereq for reactorTick. 
- If you don't have coolers/fuel rods in the multi, or you don't have the fuel/coolant they need reactor won't run.
- If there is output space for hot coolants/depleted fuels, you shall have them. However, reactor will run without outputs.
- Same applies to breeder reactors, maybe up for change?
- Moderators are the fine knob on heat, allowing you to control the use. There will probably be more of these than fuel rods. These are not nessecary to run any reactor.
- Fuel rods control the fuel used and the rate/amount they are used. They also apply an additive heat production.
- Coolers define the coolant used, also rate/amount ofc. They apply an additive coolant power.
- Blanket rods define the neutron target, and the pool of output materials. They only work in the breeder multi class, but said class will run without them.
- There are a lot of config values, so that hopefully helps with some of the design choices people might disagree with me on.
- Ofc, we have jade provider, that shows heat, eu, amount of moderators/fuel rods/coolers, total cooling power available, and parallels. 
- Machine ui is pretty similar.
- Rewards players who want to reprocess the hot coolants/depleted fuels. The way this is all designed allows a lot of modularity. 
- How powerful a reactor is, is defined by the actual multiblock size (placement of the reactor componets) as well as the stats of the blocks themselves.
- Everything is possible to do through kjs. 
- Any future lines will be more reprocessing of the materials obtained. I wonder how players will handle the optional depleted fuels and the hot coolants.
- Also nuke: nuke is fun, nuke is boom, we should have nuke.
- Also yeah, the explosion size (if outside of ftbchunks/the config for real explosions is on) is dependent on the tier of fuel rods and the moderators.
