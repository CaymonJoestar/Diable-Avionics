package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

import static data.scripts.util.Diableavionics_stringsManager.txt;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiableAvionicsSniperkit extends BaseHullMod {

    private float REDUCED_RANGE =0.25f;
    private float SMOD_REDUCED_RANGE=0.75f;

    public static Map mag = new HashMap();
    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 0f);
        mag.put(ShipAPI.HullSize.FRIGATE, 10f);
        mag.put(ShipAPI.HullSize.DESTROYER, 20f);
        mag.put(ShipAPI.HullSize.CRUISER, 40f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 60f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        boolean sMod = isSMod(stats);

        for(int i=0; i<(int)stats.getNumFighterBays().getBaseValue();i++) {
            if(stats.getVariant().getWing(i)==null){
                continue;
            }
            if(stats.getVariant().getWingId(i).equals("diableavionics_warlust_wing")){
                FighterWingSpecAPI wanzer = stats.getVariant().getWing(i);
               if(sMod){
                   wanzer.setRange(wanzer.getRange()*SMOD_REDUCED_RANGE);
               }else{
                   wanzer.setRange(wanzer.getRange()*REDUCED_RANGE);
               }
            }
        }

    }


    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {

        if (fighter.getWing() != null && fighter.getWing().getSpec() != null) {
            if(fighter.getWing().getWingId().equals("diableavionics_warlust_wing"))
            {

                fighter.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(id,(Float)mag.get(ship.getHullSize()));
//                List<WeaponAPI> Allweapon= fighter.getWingLeader().getAllWeapons();
//                for(WeaponAPI w:Allweapon){
//                    Global.getLogger(this.getClass()).info(w.getRange());
//                }         used by debug

                // fighter.addTag(Tags.WING_STAY_IN_FRONT_OF_SHIP);
            }
        }
    }
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {

        int bays = (int) ship.getMutableStats().getNumFighterBays().getModifiedValue();
        return bays > 0 && !ship.getVariant().getHullMods().contains("defensive_targeting_array");

    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + ((Float) mag.get(ShipAPI.HullSize.FRIGATE)).intValue() + "%";
        if (index == 1) return "" + ((Float) mag.get(ShipAPI.HullSize.DESTROYER)).intValue() + "%";
        if (index == 2) return "" + ((Float) mag.get(ShipAPI.HullSize.CRUISER)).intValue() + "%";
        if (index == 3) return "" + ((Float) mag.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue() + "%";
        if (index == 4) return "" + (int)((1-REDUCED_RANGE)*100)+ "%";
        return null;
    }



    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int)((1-SMOD_REDUCED_RANGE)*100) + "%";
        if (index == 1) return "" + (int)((1-REDUCED_RANGE)*100) + "%";
        return null;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.hasLaunchBays()) {
            return txt("hm_sniperkit_01");
        }
        if (ship.getVariant().getHullMods().contains("defensive_targeting_array")) {
            return txt("hm_sniperkit_02");
        }
        return null;
    }
}
