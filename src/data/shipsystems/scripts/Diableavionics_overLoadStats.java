


package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;


public class Diableavionics_overLoadStats extends BaseShipSystemScript {
    public static final float DAMAGE_BONUS_PERCENT = 50f;
    public static final float MOBLIE_BONUS_PERCENT = 50f;
    public static final float EXTRA_DAMAGE_TAKEN_PERCENT = 100f;
    private boolean system_used=false;
    private ShipAPI ship;
    private Map<ShipEngineControllerAPI.ShipEngineAPI, Float> trailIDMap = new HashMap<>();
    private SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "beamRough2Core");
    private float trailid=0f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        ship.setJitter( ship,
                Color.RED,
                0.4f*effectLevel,
                3,
                4+5f*effectLevel,
                7+10f*effectLevel
        );

            system_used=true;

            float angle = Misc.getAngleInDegrees(new Vector2f(ship.getVelocity()));
            float opacity = ship.getVelocity().length() / ship.getMaxSpeed();
            float startSize = 12f;
            float endSize = 5f;
            float duration = 3f;
            float opacityMult = 0.85f;

            WeaponAPI head = null;
            if(trailid==0f){
                trailid=MagicTrailPlugin.getUniqueID();
            }
            for(WeaponAPI w:ship.getAllWeapons()){
                if(w.getId().contains("Head")){
                    head=w;
                }
            }
            if(head==null){
                throw new RuntimeException("Cant find wanzer`s head, is this system used on wanzer?");
            }


            MagicTrailPlugin.addTrailMemberSimple(ship,
                    trailid,
                    trailSprite,
                    head.getLocation(),
                    0f,
                    angle,
                    startSize,
                    endSize,
                    Color.RED,
                    opacity * opacityMult,
                    0f,
                    0f,
                    duration,
                    true
                    );


        float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
        stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);

        stats.getTurnAcceleration().modifyPercent(id,MOBLIE_BONUS_PERCENT);
        stats.getAcceleration().modifyPercent(id,MOBLIE_BONUS_PERCENT);
        stats.getDeceleration().modifyPercent(id,MOBLIE_BONUS_PERCENT);
        stats.getMaxSpeed().modifyPercent(id,MOBLIE_BONUS_PERCENT*0.5f);
        //float damageTakenPercent = EXTRA_DAMAGE_TAKEN_PERCENT * effectLevel;
//		stats.getArmorDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getHullDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getShieldDamageTakenMult().modifyPercent(id, damageTakenPercent);
        //stats.getWeaponDamageTakenMult().modifyPercent(id, damageTakenPercent);
        //stats.getEngineDamageTakenMult().modifyPercent(id, damageTakenPercent);

        //stats.getBeamWeaponFluxCostMult().modifyMult(id, 10f);
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getMaxSpeed().unmodify();
        stats.getDeceleration().unmodify();
        stats.getAcceleration().unmodify();
        stats.getTurnAcceleration().unmodify();
//		stats.getEnergyWeaponRangeBonus().unmodify(id);
//		stats.getArmorDamageTakenMult().unmodify(id);
//		stats.getHullDamageTakenMult().unmodify(id);
//		stats.getShieldDamageTakenMult().unmodify(id);
//		stats.getWeaponDamageTakenMult().unmodify(id);
//		stats.getEngineDamageTakenMult().unmodify(id);

        if (system_used){
            system_used=false;
            ship.giveCommand(ShipCommand.VENT_FLUX,0,0);
        }


    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
        float damageTakenPercent = EXTRA_DAMAGE_TAKEN_PERCENT * effectLevel;
        if (index == 0) {
            return new StatusData("+" + (int) bonusPercent + "% 能量武器伤害" , false);
        } else if (index == 1) {
            //return new StatusData("+" + (int) damageTakenPercent + "% weapon/engine damage taken", false);
            return null;
        } else if (index == 2) {
            //return new StatusData("shield damage taken +" + (int) damageTakenPercent + "%", true);
            return null;
        }
        return null;
    }
}
