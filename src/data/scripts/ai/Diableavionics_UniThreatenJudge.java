package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import java.util.List;

public class Diableavionics_UniThreatenJudge {
    public Diableavionics_UniThreatenJudge(){

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

    public  static boolean  BeamweaponFiring(ShipAPI ship,float search_range){
        List<WeaponAPI> weapons;
        List<ShipAPI> nearship = AIUtils.getNearbyEnemies(ship,search_range);
        for(ShipAPI s : nearship){
            if(s.getShipTarget()!=ship) continue;
            weapons=s.getAllWeapons();
            for(WeaponAPI w:weapons){
                if(w.isBeam()&&w.isFiring()){
                    float beamDamage=w.getDamageType().getArmorMult() * w.getDamage().getDamage();
                    if(beamDamage>=900)
                    return true;
                }
            }
        }
        return false;
    }

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
