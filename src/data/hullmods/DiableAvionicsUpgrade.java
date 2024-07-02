package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicAutoTrails;
import org.magiclib.plugins.MagicTrailPlugin;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsUpgrade extends BaseHullMod {

    private static Map mag = new HashMap();
    static {
        mag.put(HullSize.FRIGATE, 25f);
        mag.put(HullSize.DESTROYER, 15f);
        mag.put(HullSize.CRUISER, 10f);
        mag.put(HullSize.CAPITAL_SHIP, 10f);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().modifyFlat(id, (Float) mag.get(hullSize));
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return !isForModSpec;
    }

//    //更多的描述拓展
//    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
//        int MAXLIMIT = 0;
//        CombatEngineAPI engine = Global.getCombatEngine();
//
//        tooltip.addSectionHeading("根据舰船系统的不同，额外获得以下增益:", Alignment.TMID, 4f);
//        if(ship.getSystem().getId().equals("diableavionics_drift"))
//            tooltip.addPara("额外添加的时流线圈强化了系统的效力，时流持续时间增加100%(+0.1s)" , new Color(106, 168, 79, 255), 4f);
//
//    }


    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new AvionicsTracker(ship));
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "25";
        }
        if (index == 1) {
            return "15";
        }
        if (index == 2) {
            return "10";
        }
        if (index == 3) {
            return "5";
        }
        if (index == 4) {
            return "7%";
        }


        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getHullSpec().getHullId().startsWith("diableavionics_")
                && !ship.getVariant().hasHullMod(DampenedMounts.MODULAR_MOUNT_ID)
                && !ship.getVariant().getPermaMods().contains(DampenedMounts.MODULAR_MOUNT_ID)
                && !ship.getVariant().getSMods().contains(DampenedMounts.MODULAR_MOUNT_ID);
    }

    private static final Color ENGINE_COLOR = new Color(190, 220, 255);
    private static final Color CONTRAIL_COLOR = new Color(255, 100, 100);
    public class AvionicsTracker implements AdvanceableListener {
        private IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.05f);
        private Map<ShipEngineControllerAPI.ShipEngineAPI, Float> trailIDMap = new HashMap<>();
        private SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "beamRough2Core");

        private final ShipAPI ship;
        private final IntervalUtil interval = new IntervalUtil(1f, 1f);
        private float lastFluxCap = 0f;
        private float engineEffectLevel = 0f;
        private boolean avionicsBoost = false;

        public AvionicsTracker(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            interval.advance(amount);
            if (interval.intervalElapsed()) {
                interval.setInterval(1f, 1f);

                float newFlux = ship.getCurrFlux() / ship.getMaxFlux();
                if (avionicsBoost) {
                    if (newFlux - lastFluxCap >= 0.07f) {
                        interval.setInterval(5f, 5f);

                        avionicsBoost = false;
                        ship.getMutableStats().getZeroFluxMinimumFluxLevel().unmodify("diableavionics_advancedAvionics");
                    }
                } else {
                    if (newFlux - lastFluxCap < 0.07f) {
                        avionicsBoost = true;
                        // set to two, meaning boost is always on
                        ship.getMutableStats().getZeroFluxMinimumFluxLevel().modifyFlat("diableavionics_advancedAvionics", 2f);
                    }
                }

                lastFluxCap = newFlux;
            }

            if (avionicsBoost) {
                engineEffectLevel += amount;

                effectInterval.advance(amount);
                if (effectInterval.intervalElapsed()) {
                    float angle = Misc.getAngleInDegrees(new Vector2f(ship.getVelocity()));
                    float opacity = ship.getVelocity().length() / ship.getMaxSpeed();
                    float startSize = 5f;
                    float endSize = 5f;
                    float duration = 2f;
                    float opacityMult = 1f;

                    for (ShipEngineControllerAPI.ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                        if (!trailIDMap.containsKey(engine)) {
                            trailIDMap.put(engine,  MagicTrailPlugin.getUniqueID());
                        }

                        MagicTrailPlugin.addTrailMemberSimple(
                                ship,
                                trailIDMap.get(engine),
                                trailSprite,
                                engine.getLocation(),
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
                    }
                }
            } else {
                engineEffectLevel -= amount;
            }

            engineEffectLevel = MathUtils.clamp(engineEffectLevel, 0f, 1f);
            ship.getEngineController().fadeToOtherColor(this, ENGINE_COLOR, new Color(0, 0, 0, 0), engineEffectLevel, 0.33f);
        }
    }
}
