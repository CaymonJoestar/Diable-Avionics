package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;

import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import data.scripts.util.Diableavionics_graphicLibEffects;
import org.magiclib.util.MagicAnim;

import java.awt.*;

public class Diableavionics_nocturne_targetingEffect implements BeamEffectPlugin {
    private final float TICK = 0.05f;
    private final IntervalUtil chargeInterval = new IntervalUtil(TICK, TICK);
    boolean init = false;
    private float timer = 0;
    private float DEM_TARGETING_TIME = 1.5f;
    private float effectlevel = 0f;
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

            if (!init) {

                chargeInterval.advance(amount);
//                effectlevel=MagicAnim.smooth((timer*TICK)/DEM_TARGETING_TIME);
                Vector2f Glow_location= MathUtils.getPoint(beam.getFrom(),20f, VectorUtils.getFacing(beam.getTo()));

                Diableavionics_graphicLibEffects.customLight(beam.getFrom(), beam.getSource(),
                        20f,
                        10f,
                        new Color(160,100,150,160),
                        1.0f,
                        2f,
                        1.0f
                );

                init = true;

                if(chargeInterval.intervalElapsed()){
                    timer+=1;

//                    Diableavionics_graphicLibEffects.customLight(beam.getFrom(), beam.getSource(),
//                            effectlevel*30f,
//                            effectlevel*30f,
//                            new Color(160,100,150,200),
//                            0,
//                            0,
//                            0.5f
//                    );
//                    for(int i=0; i<5; i++){
//                        engine.addNebulaParticle(
//                                location,
//                                MathUtils.getPoint(location, MathUtils.getRandomNumberInRange(15*i, 30*i),  beam.getWeapon().getCurrAngle()),
//                                MathUtils.getRandomNumberInRange(2, 6),
//                                MathUtils.getRandomNumberInRange(1.5f, 2f),
//                                0.1f,
//                                0.3f,
//                                MathUtils.getRandomNumberInRange(1*TICK, 3*TICK),
//                                Color.magenta);
//                    }

//                    if(timer==40){ init = true; }
                }

            }
                return;
    }
}
