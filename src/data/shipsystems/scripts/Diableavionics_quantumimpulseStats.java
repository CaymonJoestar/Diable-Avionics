package data.shipsystems.scripts;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.DAModPlugin;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


import static com.sun.webpane.platform.ConfigManager.log;
import static data.scripts.util.Diableavionics_stringsManager.txt;
//import data.scripts.weapons.Diableavionics_derechoEffect;

public class Diableavionics_quantumimpulseStats extends BaseShipSystemScript {

    private final float ANIM = 0.05f, RANGE = 2000, RIMTICK = 0.5f;
    private final IntervalUtil timer = new IntervalUtil(0.1f, 0.1f);
    private final IntervalUtil sparkle = new IntervalUtil(0.05f, 0.15f);
    //    private final float rangeMult=0;
    private final String zapSprite = "zap_0";
    private final int zapFrames = 8;
    private final String TXT1 = txt("quantum1");
    private final String TXT2 = txt("quantum2");
    private boolean runOnce = false;
    private ShipAPI theShip;
    private ShipSystemAPI theSystem;
    private float distubringRange;
    private float ecmRate;
    private float ecmBounsRange;
    private List<MissileAPI> locked = new ArrayList<>(), vulnerable = new ArrayList<>();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {

        CombatEngineAPI engine = Global.getCombatEngine();

        if (!runOnce) {
            runOnce = true;
            if (stats.getEntity() instanceof ShipAPI) {
                theShip = (ShipAPI) stats.getEntity();
            }
            theSystem = theShip.getSystem();
            locked.clear();
            vulnerable.clear();
        }

        if (engine.isPaused()) {
            return;
        }

        if (engine != null) {
            float amount = 0.0F;
            if (!engine.isPaused()) {
                amount = engine.getElapsedInLastFrame();
            }

            if (theSystem.isOn()) {
                timer.advance(amount);
                ecmRate=stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).flatBonus;
                ecmBounsRange=ecmRate*50.0f;  // every 1% ecm strength gain 50su and 1% disturbing strength
                if(ecmBounsRange >= 1500)       //max ecmrange bonus 1500su
                    ecmBounsRange=1500.0F;

                distubringRange=RANGE * theShip.getMutableStats().getSystemRangeBonus().getBonusMult() * theSystem.getEffectLevel()+ecmBounsRange;
                if (timer.intervalElapsed()) {
                    List<MissileAPI> missiles = AIUtils.getNearbyEnemyMissiles(theShip,distubringRange);
                    for (MissileAPI m : missiles) {
                        //leave missiles imune to flares alone
                        if (m.getWeaponSpec() != null && m.getWeaponSpec().getAIHints().contains("IGNORES_FLARES"))
                            continue;

                        if (!locked.contains(m) && !DAModPlugin.DERECHO_IMMUNE.contains(m.getProjectileSpecId())) {
                            locked.add(m);
                            if (!DAModPlugin.DERECHO_RESIST.contains(m.getProjectileSpecId())) {
                                vulnerable.add(m);
                            }
                        }
                    }
                }
            }
            lockMissiles(engine, amount);

        }

    }

    private void lockMissiles(com.fs.starfarer.api.combat.CombatEngineAPI engine, float amount) {

        boolean sparkling = false;
        sparkle.advance(amount);
        if (sparkle.intervalElapsed()) {
            sparkling = true;
        }

        if (!locked.isEmpty()) {
            for (Iterator<MissileAPI> iter = locked.iterator(); iter.hasNext(); ) {
                MissileAPI m = iter.next();
                if (m.isFading() || m.didDamage() || !engine.isEntityInPlay(m)) {
                    iter.remove();
                    if (vulnerable.contains(m)) {
                        vulnerable.remove(m);
                    }
                } else {

                    if(Math.random()>0.5)
                        m.giveCommand(ShipCommand.TURN_RIGHT);

                    //tart`s original derecho effect ↓
                    //  m.setAngularVelocity(0);
                    if (sparkling) {
                        //flameout
                        if (Math.random() > 0.96-ecmRate*0.01f && vulnerable.contains(m)) {
                            if (m.getEngineController().isFlamedOut()) {
                                m.setArmingTime(m.getFlightTime() + 0.2f);
                            } else {
                                m.flameOut();
                            }
                        }

                        //zaps
                        if (Math.random() > 0.75 && MagicRender.screenCheck(0.1f, m.getLocation())) {
                            int chooser = new Random().nextInt(zapFrames - 1) + 1;
                            float rand = 0.5f * (float) Math.random() + 0.5f;

                            MagicRender.objectspace(
                                    Global.getSettings().getSprite("fx", zapSprite + chooser),
                                    m,
                                    new Vector2f(),
                                    new Vector2f(),
                                    new Vector2f(48 * rand, 48 * rand),
                                    new Vector2f((float) Math.random() * 20, (float) Math.random() * 20),
                                    (float) Math.random() * 360,
                                    (float) (Math.random() - 0.5f) * 10,
                                    false,
                                    new Color(255, 175, 255),
                                    true,
                                    0,
                                    0.1f + (float) Math.random() * 0.1f,
                                    0.1f,
                                    false
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData(TXT1+TXT2+(int)ecmBounsRange+"/"+(int)ecmRate+"%" , false);
        }
        return null;
    }
}