package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class diable_strikecraft extends BaseHullMod {
    public diable_strikecraft() {
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        boolean haveGettag = Global.getSettings().getModManager().isModEnabled("armaa");
        if (haveGettag) {
            ship.getVariant().addPermaMod("strikeCraft");
            ship.getVariant().addPermaMod("armaa_strikeCraftFrig");
            ship.getHullSpec().setTravelDriveId("armaa_traveldrive");
        }

    }
}
