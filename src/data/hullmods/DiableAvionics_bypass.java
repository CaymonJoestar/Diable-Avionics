package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class DiableAvionics_bypass extends BaseHullMod {

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

			stats.getDynamic().getMod(Stats.FORCE_ALLOW_CONVERTED_HANGAR).modifyFlat(id, 1f);
	}

	
}









