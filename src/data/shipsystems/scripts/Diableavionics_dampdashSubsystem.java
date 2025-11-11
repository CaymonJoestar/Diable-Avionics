package data.shipsystems.scripts;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.Diableavionics_UniThreatenJudge;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;
import java.awt.*;
import java.util.Random;
import java.awt.Color;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;

public class Diableavionics_dampdashSubsystem extends MagicSubsystem {

    private final Integer TURN_ACC_BUFF = 1000;
    private final Integer TURN_RATE_BUFF = 400;
    private final Integer ACCEL_BUFF = 200;
    private final Integer DECCEL_BUFF = 300;
    private final Integer SPEED_BUFF = 200;
    private final Float DAMAGE_RESISTANCE = 0.2f;
    private int BASE_MAX_CHARGES = 3;
    private final IntervalUtil tick = new IntervalUtil(0.05f,0.05f);
    private static final Color engineColor = new Color(0x00FF80);
    private float degress =0f;
    private boolean Need_dodge = false;
    private boolean Need_attack= false;
    public Diableavionics_dampdashSubsystem(ShipAPI ship) {
        super(ship);
    }

    @Override
    public float getBaseInDuration() {
        return 0.2f;
    }

    @Override
    public float getBaseActiveDuration() {
        return 0.3f;
    }

    @Override
    public float getBaseOutDuration() {
        return 0.1f;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 3f;
    }

    @Override
    public int getMaxCharges() {
        return 3;
    }

    @Override
    public float getBaseChargeRechargeDuration() {
        return 5f;
    }


    @Override
    public boolean shouldActivateAI(float amount) {
        ShipAPI target = ship.getShipTarget();



        if(Diableavionics_UniThreatenJudge.isTreatenedbyProjectile(ship,300) )
        {
            Need_dodge=true;
            return true;
        }

        if(Diableavionics_UniThreatenJudge.BeamweaponFiring(ship,2000)){
            Need_dodge=true;
            return true;
        }


        if (target != null&& charges==BASE_MAX_CHARGES) {
            float score = 0f;

            if (target.getFluxTracker().isOverloadedOrVenting()) {
                score += 6f;
            } else {
                score += target.getFluxLevel() * 6f;
            }

            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());

//            float avgRange = aiData.getAverageWeaponRange(false);
            score += Math.min(dist/200f, 4f);

            if(score > 8f){
                Need_attack=true;
                return true;
            }

        }

        return false;
    }

    @Override
    public void onActivate() {

        if(ship.getShipTarget()!=null && Need_dodge==true)
        {
            // dash from left or right side to dodge
            int a=MathUtils.getRandomNumberInRange(45,90);
            int b=MathUtils.getRandomNumberInRange(0,1);
            if(b==0) b=-1;
            float test_a =VectorUtils.getFacing(ship.getShipTarget().getLocation());
            degress= (float) (ship.getFacing()+(float)a*b);

//            //if force speed parallel to the direction of speed，reverse it
//            float speedangle = VectorUtils.getFacing(ship.getVelocity());
//            if(Math.abs(speedangle-degress)<=20f){
//                degress=degress-180f;
//            }
            Need_dodge=false;
        }else if(ship.getShipTarget()!=null && Need_attack==true){

            // float test = VectorUtils.getAngle(ship.getLocation(),ship.getShipTarget().getLocation());
            degress=  (VectorUtils.getAngle(ship.getLocation(),ship.getShipTarget().getLocation())+ MathUtils.getRandomNumberInRange(-20f,20f));
            Need_attack=false;
        }

        else degress= (float)(VectorUtils.getFacing(ship.getVelocity()));  //no need to dodge,or attack

    }

    public void advance(float amount, boolean isPaused) {
        if (isPaused) return;



        if (state == State.IN || state == State.ACTIVE || state == State.OUT) {
            ship.getEngineController().fadeToOtherColor(this, engineColor, new Color(0, 0, 0, 0), getEffectLevel(), 0.67f);
            ship.getEngineController().extendFlame(this, 2f * getEffectLevel(), 0f * getEffectLevel(), 0f * getEffectLevel());

            Global.getCombatEngine().addNebulaParticle(ship.getLocation(),
                    MathUtils.getPoint(ship.getVelocity(),10,180),
                    MathUtils.getRandomNumberInRange(10, 20),
                    MathUtils.getRandomNumberInRange(1.5f, 2f),
                    0.3f,
                    0.6f,
                    MathUtils.getRandomNumberInRange(0.3f, 0.6f),
                    new Color(60,255,150,160));

        }


        if(state == State.IN){
            //give a force-based dash,helping dogding fire
            //this mean for 60 mass warlust ,it will get 100% force
            //for 75 mass rave, about 86%
            //for 50 mass frost about 111%
            float force_multplier = 100f/(ship.getMass()+40f);

            CombatUtils.applyForce(ship,degress,200*force_multplier);
        }

            if(state == State.ACTIVE){

                //check if there is overspeed,
                if(ship.getVelocity().length()>=900){
                    //ship.getVelocity().scale(0.6f);
                    ship.giveCommand(ShipCommand.DECELERATE,null,0);
                }
            }




            if(state == State.IN || state == State.ACTIVE){



                //visual trail
                if(!Global.getCombatEngine().isPaused()){
                    tick.advance(Global.getCombatEngine().getElapsedInLastFrame());
                    if(tick.intervalElapsed()){
                        if(ship!=null){
                            ship.addAfterimage(new Color(60,255,150,128), 0, 0,
                                    -ship.getVelocity().x, -ship.getVelocity().y, 1f, 0f, 0.2f, getEffectLevel(), false, true, false);
                        }
                    }
                }
            }



            switch (state) {
                case IN:
                    //play soundeffect
                    Global.getSoundPlayer().playSound("diableavionics_damperwave",1.2f,0.8f,ship.getLocation(),ship.getVelocity());

                    //mobility boost
                    stats.getMaxSpeed().modifyPercent(getDisplayText(), getEffectLevel()*SPEED_BUFF);
                    stats.getAcceleration().modifyPercent(getDisplayText(), ACCEL_BUFF);
                    stats.getDeceleration().modifyPercent(getDisplayText(), DECCEL_BUFF);

                    stats.getMaxTurnRate().modifyPercent(getDisplayText(), getEffectLevel()*TURN_RATE_BUFF);
                    stats.getTurnAcceleration().modifyPercent(getDisplayText(), TURN_ACC_BUFF);

                    //damage reduction
                    stats.getArmorDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE);
                    stats.getHullDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE);
                    stats.getEmpDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE);
                    break;

                case ACTIVE:
                    //mobility boost
                    stats.getMaxSpeed().modifyPercent(getDisplayText(), SPEED_BUFF);
                    stats.getAcceleration().modifyPercent(getDisplayText(), 0);
                    stats.getDeceleration().modifyPercent(getDisplayText(), 0);
                    stats.getAcceleration().modifyMult(getDisplayText(), 0);
                    stats.getDeceleration().modifyMult(getDisplayText(), 0);

                    stats.getMaxTurnRate().modifyPercent(getDisplayText(), TURN_RATE_BUFF);
                    stats.getTurnAcceleration().modifyPercent(getDisplayText(), 0);
                    //damage reduction
                    stats.getArmorDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE);
                    stats.getHullDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE);
                    stats.getEmpDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE);
                    break;

                case OUT:


                    //mobility boost
                    stats.getMaxSpeed().modifyPercent(getDisplayText(), getEffectLevel()*SPEED_BUFF);
                    stats.getAcceleration().modifyPercent(getDisplayText(), 0);
                    stats.getDeceleration().modifyPercent(getDisplayText(), 0);
                    stats.getAcceleration().modifyMult(getDisplayText(), 1-getEffectLevel());
                    stats.getDeceleration().modifyMult(getDisplayText(), 1-getEffectLevel());

                    stats.getMaxTurnRate().modifyPercent(getDisplayText(), getEffectLevel()*TURN_RATE_BUFF);
                    stats.getTurnAcceleration().modifyPercent(getDisplayText(), 0);

                    //damage reduction
                    stats.getArmorDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE+(1-DAMAGE_RESISTANCE)*(1-getEffectLevel()));
                    stats.getHullDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE+(1-DAMAGE_RESISTANCE)*(1-getEffectLevel()));
                    stats.getEmpDamageTakenMult().modifyMult(getDisplayText(), DAMAGE_RESISTANCE+(1-DAMAGE_RESISTANCE)*(1-getEffectLevel()));
                    break;

        }



    }

    @Override
    public void onFinished() {
        stats.getMaxTurnRate().unmodify(getDisplayText());
        stats.getTurnAcceleration().unmodify(getDisplayText());

        stats.getMaxSpeed().unmodify(getDisplayText());
        stats.getAcceleration().unmodify(getDisplayText());
        stats.getDeceleration().unmodify(getDisplayText());

        stats.getArmorDamageTakenMult().unmodify(getDisplayText());
        stats.getHullDamageTakenMult().unmodify(getDisplayText());
        stats.getEmpDamageTakenMult().unmodify(getDisplayText());
    }

    @Override
    public String getDisplayText() {
        return "Dampwave dash";
    }


}
