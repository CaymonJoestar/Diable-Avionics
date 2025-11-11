package data.shipsystems.scripts;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.util.ArrayList;
import java.util.List;

import static data.scripts.util.Diableavionics_stringsManager.txt;
public class Diableavionics_longRangeStats extends BaseShipSystemScript {

    private final float RANGE_BOOST=2000, SPEED_DAMPENING=0.5f;
    public static final float DAMAGE_BONUS_PERCENT = 20f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(effectLevel>0){
            float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
            stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
            stats.getBallisticWeaponDamageMult().modifyPercent(id, bonusPercent);
            stats.getBallisticWeaponRangeBonus().modifyPercent(id, 25f);
            stats.getEnergyWeaponRangeBonus().modifyPercent(id, 25f);
            stats.getFighterWingRange().modifyFlat(id, RANGE_BOOST*effectLevel);
            stats.getMaxSpeed().modifyMult(id, effectLevel*SPEED_DAMPENING);
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) {
                    continue;
                }
                MutableShipStatsAPI fStats = fighter.getMutableStats();
                fStats.getMaxSpeed().modifyPercent(id, 25f);
                fStats.getAcceleration().modifyPercent(id, 25f);
                fStats.getDeceleration().modifyPercent(id, 25f);
                fStats.getMaxTurnRate().modifyPercent(id, 25f);
                fStats.getTurnAcceleration().modifyPercent(id, 25f);
            }
        }
    }
    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) continue;
            if (ship.getWing() == null) continue;
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }
        return result;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        stats.getFighterWingRange().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getBallisticWeaponDamageMult().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getMaxSpeed().unmodify(id);

            fStats.getAcceleration().unmodify(id);
            fStats.getDeceleration().unmodify(id);
            fStats.getTurnAcceleration().unmodify(id);
            fStats.getMaxTurnRate().unmodify(id);
        }
    }	

    private final String PLUS = txt("+");
    private final String TXT1 = txt("range1");
    private final String TXT2 = txt("range2");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
        if (index == 0) {
            return new StatusData(PLUS+(int)(RANGE_BOOST * effectLevel)+TXT1, false);
        }
        if (index == 1) {
            return new StatusData(  "-50% Max speed", true);
        }
        if (index == 2) {
            return new StatusData("+" + (int) bonusPercent + "% energy/ballistic weapon damage" , false);
        }
        if (index == 3) {
            return new StatusData("+"  + "25% energy/ballistic weapon range" , false);
        }
        return null;
    }
}