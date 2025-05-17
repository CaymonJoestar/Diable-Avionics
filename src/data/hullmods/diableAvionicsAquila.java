package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class diableAvionicsAquila extends BaseHullMod {
        public diableAvionicsAquila() {
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        boolean haveEIS = Global.getSettings().getModManager().isModEnabled("timid_xiv");
        if (haveEIS) {
            ship.getVariant().addPermaMod("eis_aquila");
        }

    }
}

