package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.WeaponUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lazywizard.lazylib.VectorUtils;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Diableavionics_UniThreatenJudge {

//    public static Map personal_weighting = new HashMap();
//    static {
//        personal_weighting.put(ShipAPI.HullSize.FIGHTER, 0f);
//        personal_weighting.put(ShipAPI.HullSize.FRIGATE, 25f);
//        personal_weighting.put(ShipAPI.HullSize.DESTROYER, 20f);
//        personal_weighting.put(ShipAPI.HullSize.CRUISER, 15f);
//        personal_weighting.put(ShipAPI.HullSize.CAPITAL_SHIP, 10f);
//    }


    private static final Map<ShipAPI.HullSize, Integer> SHIP_THREAT_SCORE = new EnumMap<>(ShipAPI.HullSize.class);

    static {
        SHIP_THREAT_SCORE.put(ShipAPI.HullSize.DEFAULT, 0);
        SHIP_THREAT_SCORE.put(ShipAPI.HullSize.FIGHTER, 1);
        SHIP_THREAT_SCORE.put(ShipAPI.HullSize.FRIGATE, 3);
        SHIP_THREAT_SCORE.put(ShipAPI.HullSize.DESTROYER,5);
        SHIP_THREAT_SCORE.put(ShipAPI.HullSize.CRUISER,7);
        SHIP_THREAT_SCORE.put(ShipAPI.HullSize.CAPITAL_SHIP,10);
    }

    public Diableavionics_UniThreatenJudge(){

    }


    public static float getThreatenScore(ShipAPI target){

        return SHIP_THREAT_SCORE.get(target.getHullSize());
    }

    public static boolean isTreatenedbyProjectile(ShipAPI ship,float search_range){

        List<DamagingProjectileAPI> near = CombatUtils.getProjectilesWithinRange(ship.getLocation(),search_range);
        List<MissileAPI> near_missiles = CombatUtils.getMissilesWithinRange(ship.getLocation(),search_range);
        if (!near.isEmpty()) {
            float threatPoint = 0f;

            for (MissileAPI m : near_missiles) {
                if (m.getOwner() != ship.getOwner()) {
                    if (!m.didDamage() && !m.isFading()) {

                        float speed = m.getVelocity().length();
                        float dist = Misc.getDistance(ship.getLocation(), m.getLocation()) - ship.getCollisionRadius();
                        //先不考虑弹丸的飞行方向，距离/速度小于2秒认为有必要计算威胁度 （视需要提升火降低秒数或者直接移除这个判定）
                        if (dist < speed * 2) {
                            //弹丸命中飞船所需要的攻击角度
                            float angle = (float) Math.abs(Math.toDegrees(Math.acos(ship.getCollisionRadius() / dist)));
                            //弹丸速度的方向与飞船连线的夹角
                            float facing = Math.abs(normalizeAngle(VectorUtils.getFacing(m.getVelocity()) - VectorUtils.getAngle(m.getLocation(), ship.getLocation())));
                            if (facing < angle) {
                                float damage = m.getDamageType().getArmorMult() * m.getDamageAmount() + m.getEmpAmount();
                                //用伤害*距离系数判断威胁的大小，叠加到总威胁点数上
                                threatPoint += damage * (1+(search_range - dist) / search_range);
                            }
                        }
                    }
                }
            }


            for (DamagingProjectileAPI p : near) {
                if (p.getOwner() != ship.getOwner()) {
                    if (!p.didDamage() && !p.isFading()) {

                        float speed = p.getVelocity().length();
                        float dist = Misc.getDistance(ship.getLocation(), p.getLocation()) - ship.getCollisionRadius();
                        //先不考虑弹丸的飞行方向，距离/速度小于2秒认为有必要计算威胁度 （视需要提升火降低秒数或者直接移除这个判定）
                        if (dist < speed * 2) {

                            //弹丸命中飞船所需要的攻击角度
                            float angle = (float) Math.abs(Math.toDegrees(Math.acos(ship.getCollisionRadius() / dist)));
                            //弹丸速度的方向与飞船连线的夹角
                            float facing = Math.abs(normalizeAngle(VectorUtils.getFacing(p.getVelocity()) - VectorUtils.getAngle(p.getLocation(), ship.getLocation())));
                            if (facing < angle) {

                                float damage = p.getDamageType().getArmorMult() * p.getDamageAmount() + p.getEmpAmount();
                                //用伤害*距离系数判断威胁的大小，叠加到总威胁点数上
                                threatPoint += damage * (1+(search_range - dist) / search_range);
                            }
                        }
                    }
                }
            }
            return threatPoint >= 750;
        }
        return false;
    }

    public static boolean  BeamweaponFiring(ShipAPI ship, float search_range){
        List<WeaponAPI> weapons;
        List<ShipAPI> nearship = AIUtils.getNearbyEnemies(ship,search_range);
        ShipAPI Primary_threat = ship.getShipTarget();
        Boolean beamthreat = false;
        if(Primary_threat!=null)
        {
            if(IsAimedByBeam(ship,Primary_threat))
                beamthreat = true;
        }

        for(ShipAPI enemy : nearship){

            if(enemy.getShipTarget()!=ship&& !ship.isFighter())
                continue;

            if(IsAimedByBeam(ship,enemy))
                beamthreat = true;
        }
        return beamthreat;
    }

      public  static boolean IsAimedByBeam(ShipAPI ship, ShipAPI enemy){

        List<WeaponAPI> weapons;
        Vector2f AsumeAimline = null;
         if(enemy!=null)
         {
             weapons=enemy.getAllWeapons();
             for(WeaponAPI w:weapons){
                 if(w.isBeam()&&!w.isDecorative()&&!w.isDisabled()){
                     float beamDamage= w.getDamage().getDamage();
                     if(beamDamage>=ship.getHullSpec().getHitpoints()*0.25f || beamDamage>= 500f){
                         if(WeaponUtils.isWithinArc(ship,w))
                         {
                             AsumeAimline = new Vector2f(ship.getLocation().getX()-w.getLocation().getX(),ship.getLocation().getY()-w.getLocation().getY());

                             float Currangle=w.getCurrAngle();

                             float AsumeAimlineAngle=VectorUtils.getFacing(AsumeAimline);


                             if(Math.abs(AsumeAimlineAngle-w.getCurrAngle())<=5)
                                 return true;
                         }
                     }
                 }
                        continue;
             }
         }
         return false;
    }

//    public static float OfficerPersonalityWeighting(ShipAPI ship){
//
//
//            if (ship.getCaptain().isAICore()) return 1.0f;
//            else if(ship.getCaptain().isDefault()){
//
//
//            }
//
//            return 0f;
//    }


    private static float normalizeAngle(float ang) {
        while ((ang > 180f || ang < -180f)) {
            ang = normalize(ang);
        }
        return ang;
    }

    private static float normalize(float ang) {
        if (ang > 180f) {
            ang = ang - 360f;
        } else if (ang < -180f) {
            ang = ang + 360f;
        }
        return ang;
    }

}
