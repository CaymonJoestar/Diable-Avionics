package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_graveLacEffect implements EveryFrameWeaponEffectPlugin {
    private int graveLac_Maxammo=9;
    private int getGraveLac_Redloadsize = 3;
    private boolean runOnce=false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {



        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}


        if(!runOnce){
            runOnce=true;

            if (weapon.getShip().getVariant().getHullMods().contains("magazines"))
            {
                weapon.getAmmoTracker().setMaxAmmo(graveLac_Maxammo*2);


                if(weapon.getShip().getVariant().getSMods().contains("magazines"))
                {
                    weapon.getAmmoTracker().setReloadSize(getGraveLac_Redloadsize*2);
                }
            }
            return;
        }

    }

//    public static final class GraveLacRangeModifier implements WeaponBaseRangeModifier {
//        public GraveLacRangeModifier() {
//        }
//
//        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
//            return 0.0F;
//        }
//
//        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
//            return 1.0F;
//        }
//
//        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
//
//            if(weapon.getShip().getVariant().getHullMods().contains("magazines")&&weapon.getId().equals("diableavionics_graveac"))
//            {
//                return -100f;
//            }
//            return 0;
//        }
//    }


}


