package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.Diableavionics_UniThreatenJudge;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;
import java.awt.*;
import java.util.*;
import java.awt.Color;
import java.util.List;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_arcemitter extends MagicSubsystem{

    float ARCRANGE = 600f;
    List<CombatEntityAPI> Targets = new ArrayList<>();
    private final IntervalUtil tick = new IntervalUtil(0.5f,0.5f);
    private final IntervalUtil arctick = new IntervalUtil(0.25f,0.25f);
    public Diableavionics_arcemitter(ShipAPI ship) {
        super(ship);
    }


    @Override
    public float getBaseInDuration() {
        return 0.3f;
    }

    @Override
    public float getBaseOutDuration() {
        return 0.2f;
    }

    @Override
    public float getBaseActiveDuration() {
        return 4.5f;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 10f;
    }

    @Override
    public boolean shouldActivateAI(float amount) {
        float score = 0f;

        if (!ship.getFluxTracker().isOverloadedOrVenting()) {


            List<MissileAPI> potentialMissile = AIUtils.getNearbyEnemyMissiles(ship,ARCRANGE);
            for(MissileAPI m: potentialMissile){
                if(inFacingArc(ship,m)){
                    score+=m.getDamageAmount()/100f*(1+ship.getFluxLevel()*2);
                }
            }

            List<ShipAPI> potentialEnemy = AIUtils.getNearbyEnemies(ship,600);

            for(ShipAPI s: potentialEnemy){

                if(inFacingArc(ship,s)){
                   score+=Diableavionics_UniThreatenJudge.getThreatenScore(s);
                }
            }

            return score >= 10f;
        }

        return false;
    }


    @Override
    public void onActivate(){
        Targets.clear();
        Global.getSoundPlayer().playSound("system_emp_emitter_activate",1.0f,1.0f,ship.getLocation(),ship.getVelocity());
        return;
    }

    public void advance(float amount, boolean isPaused){

        if (isPaused) return;

        if(state == State.ACTIVE){

            Global.getSoundPlayer().playLoop("system_emp_emitter_loop",ship,1.0f,1.0f,ship.getLocation(),ship.getVelocity());

        }


        if (state == State.IN || state == State.ACTIVE || state == State.OUT) {

            arctick.advance(amount);
            tick.advance(amount);
            if(tick.intervalElapsed()){

                //clear Targets list
                Targets.clear();


                //find ship targets and add to list
                for (ShipAPI s:AIUtils.getNearbyEnemies(ship,ARCRANGE)){

                    if(inFacingArc(ship,s)){
                        Targets.add(s);
                    }
                }


                //and missiles
                for(MissileAPI m: AIUtils.getNearbyEnemyMissiles(ship,ARCRANGE)){

                    if(m.isDecoyFlare()||m.getDamageAmount()<=50f||m.isFading()) {continue;}

                    if(inFacingArc(ship,m)){
                        Targets.add(m);
                    }

                }
            }

            // ready for spawn arc
            if(arctick.intervalElapsed()){

                // if there is no targets spawn 3 arc to random point in 1 arc-tick
                if(Targets.isEmpty()) {

                    for (int k=0;k<3;k++){

                        Vector2f randompoint = MathUtils.getPoint(ship.getLocation(),600*MathUtils.getRandomNumberInRange(0.25f,0.9f),
                                ship.getFacing()+MathUtils.getRandomNumberInRange(-30,30));

                        Global.getCombatEngine().spawnEmpArcVisual(ship.getLocation(), ship, randompoint,
                                null, MathUtils.getRandomNumberInRange(2.5f,4f), Color.RED, Color.WHITE);
                        Global.getSoundPlayer().playSound("system_emp_emitter_impact",1.0f,1.0f,ship.getLocation(),ship.getVelocity());

                    }

                    return;
                }else{

                    // random pick 3 targets, after arc spawn , remove them from target list.
                    int picktargetindex = MathUtils.getRandomNumberInRange(0,Targets.size()-1);
                    CombatEntityAPI i= Targets.get(picktargetindex);

                    Global.getCombatEngine().spawnEmpArcVisual(ship.getLocation(), ship, i.getLocation(),
                            i, MathUtils.getRandomNumberInRange(2.5f,4f), Color.RED, Color.WHITE);

                    if(i instanceof ShipAPI){
                        ShipAPI enemyship = (ShipAPI) i;
                        float pierceChance = enemyship.getHardFluxLevel() - 0.1f;
                        pierceChance *= enemyship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                        boolean piercedShield = (float) Math.random() < pierceChance;

                        if (piercedShield) {
                            Global.getCombatEngine().spawnEmpArcPierceShields(
                                    ship,
                                    enemyship.getLocation(),
                                    enemyship,
                                    enemyship,
                                    DamageType.FRAGMENTATION,
                                    150f,
                                    300f,
                                    600f,
                                    "tachyon_lance_emp_impact",
                                    3f,
                                    Color.RED,
                                    Color.WHITE
                            );
                        }else{
                            Global.getCombatEngine().spawnEmpArc(
                                    ship,
                                    enemyship.getLocation(),
                                    enemyship,
                                    enemyship,
                                    DamageType.FRAGMENTATION,
                                    150f,
                                    300f,
                                    600f,
                                    "tachyon_lance_emp_impact",
                                    3f,
                                    Color.RED,
                                    Color.WHITE
                            );
                        }
                    }else{
                        Global.getCombatEngine().spawnEmpArcPierceShields(
                                ship,
                                i.getLocation(),
                                i,
                                i,
                                DamageType.FRAGMENTATION,
                                150f,
                                300f,
                                600f,
                                "tachyon_lance_emp_impact",
                                3f,
                                Color.RED,
                                Color.WHITE
                        );
                    }
                    Global.getSoundPlayer().playSound("system_emp_emitter_impact",0.7f,0.85f,ship.getLocation(),ship.getVelocity());
                    // remeber to remove picked target
                    Targets.remove(picktargetindex);
                }


            }

        }

    }

    @Override
    public void onFinished(){


    }

    @Override
    public String getDisplayText() {
        return txt("subsystem_arc_01");
    }


    // Judge if target is in the +30°or -30° arc of the ship`s facing
    private boolean inFacingArc(ShipAPI ship,CombatEntityAPI target) {


        float angle = VectorUtils.getAngle(ship.getLocation(),target.getLocation())-ship.getFacing();

        if(angle>=-45f&&angle<=45f){
            return true;
        }
        return false;
    }


}


